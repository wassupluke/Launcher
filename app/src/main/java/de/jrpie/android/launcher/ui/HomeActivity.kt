package de.jrpie.android.launcher.ui

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.actions.LauncherAction
import de.jrpie.android.launcher.databinding.HomeBinding
import de.jrpie.android.launcher.loadApps
import de.jrpie.android.launcher.openTutorial
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.preferences.migratePreferencesToNewVersion
import de.jrpie.android.launcher.preferences.resetPreferences
import de.jrpie.android.launcher.ui.tutorial.TutorialActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.fixedRateTimer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan


/**
 * [HomeActivity] is the actual application Launcher,
 * what makes this application special / unique.
 *
 * In this activity we display the date and time,
 * and we listen for actions like tapping, swiping or button presses.
 *
 * As it also is the first thing that is started when someone opens Launcher,
 * it also contains some logic related to the overall application:
 * - Setting global variables (preferences etc.)
 * - Opening the [TutorialActivity] on new installations
 */
class HomeActivity : UIObject, AppCompatActivity(),
    GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private lateinit var binding: HomeBinding

    private var sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, prefKey ->
            if (prefKey?.startsWith("clock.") == true ||
                prefKey?.startsWith("display.") == true
            ) {
                recreate()
            }
        }

    private var edgeWidth = 0.15f

    private var bufferedPointerCount = 1 // how many fingers on screen
    private var pointerBufferTimer = Timer()

    private lateinit var mDetector: GestureDetectorCompat

    // timers
    private var clockTimer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()

        // Try to restore old preferences
        migratePreferencesToNewVersion(this)

        // First time opening the app: set defaults and start tutorial
        if (!LauncherPreferences.internal().started()) {
            resetPreferences(this)

            LauncherPreferences.internal().started(true)
            openTutorial(this)
        }

        // Preload apps to speed up the Apps Recycler
        AsyncTask.execute { loadApps(packageManager, applicationContext) }

        // Initialise layout
        binding = HomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle back key / gesture on Android 13+, cf. onKeyDown()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_OVERLAY
            ) {
                handleBack()
            }
        }

    }

    override fun onStart() {
        super<AppCompatActivity>.onStart()

        mDetector = GestureDetectorCompat(this, this)
        mDetector.setOnDoubleTapListener(this)

        super<UIObject>.onStart()

        LauncherPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(sharedPreferencesListener)

    }

    private fun updateClock() {
        clockTimer.cancel()
        val locale = Locale.getDefault()
        val dateVisible = LauncherPreferences.clock().dateVisible()
        val timeVisible = LauncherPreferences.clock().timeVisible()

        var dateFMT = "yyyy-MM-dd"
        var timeFMT = "HH:mm"
        val period = 100L
        if (LauncherPreferences.clock().showSeconds()) {
            timeFMT += ":ss"
        }
        /*
         I thought about adding an option to show microseconds as well ( timeFMT += ".SSS" ).
         However setting period ot 1L (or even 10L) causes high CPU load,
         so that doesn't seem to be a good idea.
         */
        if (LauncherPreferences.clock().localized()) {
            dateFMT = android.text.format.DateFormat.getBestDateTimePattern(locale, dateFMT)
            timeFMT = android.text.format.DateFormat.getBestDateTimePattern(locale, timeFMT)
        }

        var upperFormat = SimpleDateFormat(dateFMT, locale)
        var lowerFormat = SimpleDateFormat(timeFMT, locale)
        var upperVisible = dateVisible
        var lowerVisible = timeVisible

        if (LauncherPreferences.clock().flipDateTime()) {
            upperFormat = lowerFormat.also { lowerFormat = upperFormat }
            upperVisible = lowerVisible.also { lowerVisible = upperVisible }
        }

        binding.homeUpperView.isVisible = upperVisible
        binding.homeLowerView.isVisible = lowerVisible

        binding.homeUpperView.setTextColor(LauncherPreferences.clock().color())
        binding.homeLowerView.setTextColor(LauncherPreferences.clock().color())


        clockTimer = fixedRateTimer("clockTimer", true, 0L, period) {
            this@HomeActivity.runOnUiThread {
                if (lowerVisible) {
                    val t = lowerFormat.format(Date())
                    if (binding.homeLowerView.text != t)
                        binding.homeLowerView.text = t
                }
                if (upperVisible) {
                    val d = upperFormat.format(Date())
                    if (binding.homeUpperView.text != d)
                        binding.homeUpperView.text = d
                }
            }
        }
    }

    override fun getTheme(): Resources.Theme {
        val mTheme = modifyTheme(super.getTheme())
        mTheme.applyStyle(R.style.backgroundWallpaper, true)
        LauncherPreferences.clock().font().applyToTheme(mTheme)
        LauncherPreferences.theme().colorTheme().applyToTheme(
            mTheme,
            LauncherPreferences.theme().textShadow()
        )

        return mTheme
    }

    override fun onResume() {
        super.onResume()

        edgeWidth = LauncherPreferences.enabled_gestures().edgeSwipeEdgeWidth() / 100f

        updateClock()
    }

    override fun onPause() {
        super.onPause()
        clockTimer.cancel()
    }

    override fun onDestroy() {
        LauncherPreferences.getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
        super.onDestroy()
    }

    @SuppressLint("GestureBackNavigation")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                // Only used pre Android 13, cf. onBackInvokedDispatcher
                handleBack()
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (Action.forGesture(Gesture.VOLUME_UP) == LauncherAction.VOLUME_UP) {
                    // Let the OS handle the key event. This works better with some custom ROMs
                    // and apps like Samsung Sound Assistant.
                    return false
                }
                Gesture.VOLUME_UP(this)
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (Action.forGesture(Gesture.VOLUME_DOWN) == LauncherAction.VOLUME_DOWN) {
                    // see above
                    return false
                }
                Gesture.VOLUME_DOWN(this)
            }
        }
        return true
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, dX: Float, dY: Float): Boolean {

        if (e1 == null) return false


        val displayMetrics: DisplayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        val diffX = e1.x - e2.x
        val diffY = e1.y - e2.y

        val doubleActions = LauncherPreferences.enabled_gestures().doubleSwipe()
        val edgeActions = LauncherPreferences.enabled_gestures().edgeSwipe()

        val threshold = ViewConfiguration.get(this).scaledTouchSlop
        val angularThreshold = tan(Math.PI / 6)

        var gesture = if (angularThreshold * abs(diffX) > abs(diffY)) { // horizontal swipe
            if (diffX > threshold)
                Gesture.SWIPE_LEFT
            else if (diffX < -threshold)
                Gesture.SWIPE_RIGHT
            else null
        } else if (angularThreshold * abs(diffY) > abs(diffX)) { // vertical swipe
            // Only open if the swipe was not from the phones top edge
            // TODO: replace 100px by sensible dp value (e.g. twice the height of the status bar)
            if (diffY < -threshold && e1.y > 100)
                Gesture.SWIPE_DOWN
            else if (diffY > threshold)
                Gesture.SWIPE_UP
            else null
        } else null

        if (doubleActions && bufferedPointerCount > 1) {
            gesture = gesture?.let(Gesture::getDoubleVariant)
        }

        if (edgeActions) {
            if (max(e1.x, e2.x) < edgeWidth * width) {
                gesture = gesture?.getEdgeVariant(Gesture.Edge.LEFT)
            } else if (min(e1.x, e2.x) > (1 - edgeWidth) * width) {
                gesture = gesture?.getEdgeVariant(Gesture.Edge.RIGHT)
            }

            if (max(e1.y, e2.y) < edgeWidth * height) {
                gesture = gesture?.getEdgeVariant(Gesture.Edge.TOP)
            } else if (min(e1.y, e2.y) > (1 - edgeWidth) * height) {
                gesture = gesture?.getEdgeVariant(Gesture.Edge.BOTTOM)
            }
        }
        gesture?.invoke(this)

        return true
    }

    override fun onLongPress(event: MotionEvent) {
        Gesture.LONG_CLICK(this)
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        Gesture.DOUBLE_CLICK(this)
        return false
    }

    // Tooltip
    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {

        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        // Buffer / Debounce the pointer count
        if (event.pointerCount > bufferedPointerCount) {
            bufferedPointerCount = event.pointerCount
            pointerBufferTimer = fixedRateTimer("pointerBufferTimer", true, 300, 1000) {
                bufferedPointerCount = 1
                this.cancel() // a non-recurring timer
            }
        }

        return if (mDetector.onTouchEvent(event)) {
            false
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun setOnClicks() {

        binding.homeUpperView.setOnClickListener {
            if (LauncherPreferences.clock().flipDateTime()) {
                Gesture.TIME(this)
            } else {
                Gesture.DATE(this)
            }
        }

        binding.homeLowerView.setOnClickListener {
            if (LauncherPreferences.clock().flipDateTime()) {
                Gesture.DATE(this)
            } else {
                Gesture.TIME(this)
            }
        }
    }


    private fun handleBack() {
        LauncherAction.CHOOSE.launch(this)
    }

    override fun isHomeScreen(): Boolean {
        return true
    }


    /* TODO: Remove those. For now they are necessary
     *  because this inherits from GestureDetector.OnGestureListener */
    override fun onDoubleTapEvent(event: MotionEvent): Boolean { return false }
    override fun onDown(event: MotionEvent): Boolean { return false }
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dX: Float, dY: Float): Boolean { return false }
    override fun onShowPress(event: MotionEvent) {}
    override fun onSingleTapUp(event: MotionEvent): Boolean { return false }



}
