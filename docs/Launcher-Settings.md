Tweaks and customizations can be made from within the Launcher Settings page.

These settings let you change wallpapers, change colors and fonts, enable monochrome app icons, change the app drawer layout, and much more.

In the following documentation, 'app drawer' will be used to refer to the 'All Apps' and 'Favorite Apps' views.


# Appearance

### Choose a wallpaper</summary>
Lets you change the wallpaper using a photos app, file explorer, or native wallpaper setting app.
</details>

> ### Font

Set the font used within the app settings. This setting does not affect the date/time [home screen font](https://github.com/wassupluke/Launcher/wiki/Tweaks-and-Customizations/_edit#font-1).

**type:**&nbsp;`dropdown`

**options:**&nbsp;`Hack`,`System default`,`Sans serif`,`Serif`,`Monospace`,`Serif monospace`

> ### Text Shadow

**type:**&nbsp;`toggle`

> ### Background (app list and setting)

**type:**&nbsp;`dropdown`

**type:**&nbsp;`Transparent`,`Dim`,`Blur`,`Solid`

> ### Monochrome app icons

Remove coloring from all app icons. Can help decrease visual stimulus when enabled.

**type:**&nbsp;`toggle`


# Date & Time

> ### Font

Set the home screen font for date and time. This setting does not affect the [app settings font](https://github.com/wassupluke/Launcher/wiki/Tweaks-and-Customizations/_edit#font).

**type:**&nbsp;`dropdown`

**options:**&nbsp;`Hack`,`System default`,`Sans serif`,`Serif`,`Monospace`,`Serif monospace`

> ### Color [`[bug]`](https://github.com/jrpie/launcher/issues/151)

Set the color for the home screen date and time.

Accepts a HEX color code (consisting of a '#' followed by three sets of two alphanumeric (letters and numbers) characters. A fourth set of two alphanumeric characters may be added to set the transparency of the color.

[Color wheel picker](https://rgbacolorpicker.com/color-wheel-picker)

**type:**&nbsp;`HEX`,`RGBA`

> ### Use localized date format

Adapt the display of dates and times to the specific conventions of a particular locale or region. Different locales use different date orders (e.g., MM/DD/YYYY in the US, DD/MM/YYYY in Europe).

**type:**&nbsp;`toggle`

> ### Show time

Show the current time on the home screen.

**type:**&nbsp;`toggle`

> ### Show seconds

Show the current time down to the second on the home screen.

**type:**&nbsp;`toggle`

> ### Show date

Show the current date on the home screen.

**type:**&nbsp;`toggle`

> ### Flip date and time

Place the current time above the current date on the home screen.

**type:**&nbsp;`toggle`


# Functionality

> ### Launch search results

Launches any app that matches user keyboard input when no other apps match.

As you type inside the app drawer, the app narrows down the list of apps shown based on the app title matching your text input. For example, if you type, `a`, the app list narrows to any apps with a title containing the letter `a`. Continuing the example, if you then follow your `a` with the letter `m`, the list now shows only apps containing the letter combination `am` in that order. If the only app matching this combination was, for example, `Amazon`, simply typing `am` in the app drawer would immediately launch the `Amazon` app for you.

This feature becomes more powerful when combined with [renaming](https://github.com/wassupluke/Launcher/wiki/Launcher-Settings/_edit#additional-settings) apps, effectively letting you define custom app names that could be considered 'aliases' or shortcuts. For instance, if you wanted to "bind" the keyboard input `gh` to open your `GitHub` app, you could rename `GitHub` to `GitHub gh`, `gh GitHub`, or simply `gh`. Assuming no other installed apps have the `gh` combination of letters in them, opening the app drawer and typing `gh` would immediately launch your `GitHub` app.

Press space to temporarily disable this feature and allow text entry without prematurely launching an app. Useful when combined with the [Search the web](https://github.com/wassupluke/Launcher/wiki/Launcher-Settings/_edit#search-the-web) feature.

**type:**&nbsp;`toggle`

> ### Search the web

Press return/enter while searching the app list to launch a web search.

**type:**&nbsp;`toggle`

> ### Start keyboard for search

Automatically open the keyboard when the app drawer is opened.

**type:**&nbsp;`toggle`

> ### Double swipe actions

Enable double swipe (two finger) actions as bindable gestures in launcher settings. Does not erase gesture bindings if accidentally turned off.

**type:**&nbsp;`toggle`

> ### Edge swipe actions

Enable edge swipe (near edges of screen) actions as bindable gestures in launcher settings. Does not erase gesture bindings if accidentally turned off.

**type:**&nbsp;`toggle`

> ### Edge width

Change how large a margin is used for detecting edge gestures. Shows the edge margin preview when using the slider.

**type:**&nbsp;`slider`

> ### Choose method for locking the screen

There are two methods to lock the screen and unfortunately both have downsides.
1. **`Device Admin`**
  - Doesn't work with unlocking by fingerprint or face recognition.
2. **`Accessibility Service`**
  - Requires excessive privileges.
  - μLauncher will use those privileges *only* for locking the screen.
  - As a rule of thumb, it is [not recommended](https://android.stackexchange.com/questions/248171/is-it-safe-to-give-accessibility-permission-to-an-app) to grant access to accessibility services on a random app. Always review the [source code](https://github.com/jrpie/Launcher) before granting accessibility permissions so you familiarize yourself with what the code might do.
  - On some devices, the start-up PIN will no longer be used for encrypting data after activating an accessibility service.
    - This can be [reactivated](https://issuetracker.google.com/issues/37010136#comment36) afterwards.

**type:**&nbsp;`text buttons`

**options:**&nbsp;`USE DEVICE ADMIN`,`USE ACCESSIBILITY SERVICE`

# Apps

> ### Hidden apps

Open an app drawer containing only hidden apps.

> ### Don't show apps that are bound to a gesture in the app list

Remove certain apps from the app drawer if they are already accessible via a gesture.

Reduces redundancy and tidies up app drawer.

**type:**&nbsp;`toggle`

> ### Hide paused apps

Remove paused apps from the app drawer.

**type:**&nbsp;`toggle`

> ### Hide private space from app list

Remove private space from app drawer.

**type:**&nbsp;`toggle`

> ### Layout of app list

Change how the apps are displayed when accessing the app drawer. By `Default`, all apps in the drawer will show in a vertically-scrolled list with their app icon and title. `Text` removes the app icons, shows only app titles in the drawer as a vertically-scrolled list. `Grid` shows apps with their app icon and title in a grid layout.

**type:**&nbsp;`dropdown`

**options:**&nbsp;`Default`,`Text`,`Grid`

> ### Reverse the app list

Enable Z-A sorting of apps in the app drawer. Useful for keeping apps within easier reach from the keyboard.

**type:**&nbsp;`toggle`


# Display

> ### Rotate screen

**type:**&nbsp;`toggle`

> ### Keep screen on

**type:**&nbsp;`toggle`

> ### Hide status bar

Remove the top status bar from the home screen.

**type:**&nbsp;`toggle`

> ### Hide navigation bar

Remove the navigation bar from the home screen. Enabling this setting may make it difficult to use the device if gestures are not setup properly.

**type:**&nbsp;`toggle`


# Additional Settings

> ### App Drawer Long Press on App

Access additional per-app details and settings. To use, open the app drawer and long press on any app.

**type:**&nbsp;`dropdown`

**options:**&nbsp;`App Info`,`Add to favorites`,`Hide`,`Rename`,`Uninstall`