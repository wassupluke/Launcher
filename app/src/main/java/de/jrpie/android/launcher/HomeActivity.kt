package de.jrpie.android.launcher

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import de.jrpie.android.launcher.BuildConfig.VERSION_NAME
import de.jrpie.android.launcher.list.other.LauncherAction
import de.jrpie.android.launcher.tutorial.TutorialActivity
import kotlinx.android.synthetic.main.home.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.math.abs

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
class HomeActivity: UIObject, AppCompatActivity(),
    GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private var bufferedPointerCount = 1 // how many fingers on screen
    private var pointerBufferTimer = Timer()

    private lateinit var mDetector: GestureDetectorCompat

    // timers
    private var clockTimer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = getPreferences(this)
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        loadSettings(this)

        // First time opening the app: show Tutorial, else: check versions
        if (!preferences.getBoolean(PREF_STARTED, false))
            startActivity(Intent(this, TutorialActivity::class.java))
        else when (preferences.getString(PREF_VERSION, "")) {
            // Check versions, make sure transitions between versions go well

            VERSION_NAME -> { /* the version installed and used previously are the same */ }
            "" -> { /* The version used before was pre- v1.3.0,
                        as version tracking started then */

                /*
                 * before, the dominant and vibrant color of the `finn` and `dark` theme
                 * were not stored anywhere. Now they have to be stored:
                 * -> we just reset them using newly implemented functions
                 */
                when (getSavedTheme(this)) {
                    "finn" -> resetToDefaultTheme(this)
                    "dark" -> resetToDarkTheme(this)
                }

                preferences.edit()
                    .putString(PREF_VERSION, VERSION_NAME) // save new version
                    .apply()

                // show the new tutorial
                startActivity(Intent(this, TutorialActivity::class.java))
            }
        }

        // Preload apps to speed up the Apps Recycler
        AsyncTask.execute { loadApps(packageManager) }

        // Initialise layout
        setContentView(R.layout.home)
    }

    override fun onStart(){
        super<AppCompatActivity>.onStart()

        mDetector = GestureDetectorCompat(this, this)
        mDetector.setOnDoubleTapListener(this)

        // for if the settings changed
        loadSettings(this)
        super<UIObject>.onStart()
    }

    override fun onResume() {
        super.onResume()

        // Applying the date / time format (changeable in settings)
        val dFormat = getPreferences(this).getInt(PREF_DATE_FORMAT, 0)
        val upperFMT = resources.getStringArray(R.array.settings_launcher_time_formats_upper)
        val lowerFMT = resources.getStringArray(R.array.settings_launcher_time_formats_lower)

        val dateFormat = SimpleDateFormat(upperFMT[dFormat], Locale.getDefault())
        val timeFormat = SimpleDateFormat(lowerFMT[dFormat], Locale.getDefault())

        clockTimer = fixedRateTimer("clockTimer", true, 0L, 100) {
            this@HomeActivity.runOnUiThread {
                val t = timeFormat.format(Date())
                if (home_lower_view.text != t)
                    home_lower_view.text = t

                val d = dateFormat.format(Date())
                if (home_upper_view.text != d)
                    home_upper_view.text = d
            }
        }
    }

    override fun onPause() {
        super.onPause()
        clockTimer.cancel()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> LauncherAction.CHOOSE.launch(this)
            KeyEvent.KEYCODE_VOLUME_UP -> Gesture.VOLUME_UP(this)
            KeyEvent.KEYCODE_VOLUME_DOWN -> Gesture.VOLUME_DOWN(this)
        }
        return true
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, dX: Float, dY: Float): Boolean {

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        val diffX = e1.x - e2.x
        val diffY = e1.y - e2.y

        val preferences = getPreferences(this)

        val doubleActions = preferences.getBoolean(PREF_DOUBLE_ACTIONS_ENABLED, false)

        // how distinguished the swipe has to be to launch something
        // strictness = opposite of sensitivity. TODO - May have to be adjusted
        val strictness = (4 / bufferedPointerCount) * ((100 - preferences.getInt(PREF_SLIDE_SENSITIVITY, 50)) / 50)

        var gesture = if(abs(diffX) > abs(diffY)) { // horizontal swipe
            if (diffX > width / 4 && abs(diffX) > strictness * abs(diffY))
                Gesture.SWIPE_LEFT
            else if (diffX < -width / 4 && abs(diffX) > strictness * abs(diffY))
                Gesture.SWIPE_RIGHT
            else null
        } else { // vertical swipe
            // Only open if the swipe was not from the phones top edge
            if (diffY < -height / 8 && abs(diffY) > strictness * abs(diffX) && e1.y > 100)
                Gesture.SWIPE_DOWN
            else if (diffY > height / 8 && abs(diffY) > strictness * abs(diffX))
                Gesture.SWIPE_UP
            else null
        }

        if (doubleActions && bufferedPointerCount > 1) {
            gesture = gesture?.let(Gesture::getDoubleVariant)
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

        return if (mDetector.onTouchEvent(event)) { false } else { super.onTouchEvent(event) }
    }

    override fun setOnClicks() {

        val preferences = getPreferences(this)
        home_upper_view.setOnClickListener {
            when (preferences.getInt(PREF_DATE_FORMAT, 0)) {
                0 -> Gesture.DATE(this)
                else -> Gesture.TIME(this)
            }
        }

        home_lower_view.setOnClickListener {
            when (preferences.getInt(PREF_DATE_FORMAT, 0)) {
                0 -> Gesture.TIME(this)
                else -> Gesture.DATE(this)
            }
        }
    }

    /* TODO: Remove those. For now they are necessary
     *  because this inherits from GestureDetector.OnGestureListener */
    override fun onDoubleTapEvent(event: MotionEvent): Boolean { return false }
    override fun onDown(event: MotionEvent): Boolean { return false }
    override fun onScroll(e1: MotionEvent, e2: MotionEvent, dX: Float, dY: Float): Boolean { return false }
    override fun onShowPress(event: MotionEvent) {}
    override fun onSingleTapUp(event: MotionEvent): Boolean { return false }
}
