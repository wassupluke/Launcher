<!-- Shields from shields.io -->
<!--[![][shield-release]][latest-release] -->
[![][shield-license]][license]

<!-- ENGLISH README -->

# <a name="en"></a> Launcher

This is a fork of [finnmglas's app Launcher][original-repo].

## Notable changes:

* Edge gestures: There is a setting to allow distinguishing swiping at the edges of the screen from swiping in the center.

### Visual
* This app uses the system wallpaper instead of a custom solution.
* The font has been changed to [Hack][hack-font].
* Font Awesome Icons were replaced by Material icons.
* The gear button on the home screen was removed. Instead pressing back opens the list of applications and the app settings are accessible from there.


### Search
* The search algorithm was modified to prefer matches at the beginning of the app name, i.e. when searching for `"te"`, `"termux"` is sorted before `"notes"`.
* The search bar was moved to the bottom of the screen.

### Technical
* Small improvements to the gesture detection.
* Different apps set as default.
* Package name was changed to `de.jrpie.android.launcher` to avoid clashing with the original app.
* Dropped support for API < 21 (i.e. pre Lollypop)
* Some refactoring
---
---
  [hack-font]: https://sourcefoundry.org/hack/
  [original-repo]: https://github.com/finnmglas/Launcher


<!-- Download links / stores -->

  [store-googleplay]: https://play.google.com/store/apps/details?id=de.jrpie.android.launcher
  [store-googleplay-badgecampain]: https://play.google.com/store/apps/details?id=de.jrpie.android.launcher&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1
  [store-fdroid]: https://f-droid.org/packages/de.jrpie.android.launcher/

<!-- Shields and Badges -->

  [shield-release]: https://img.shields.io/github/v/release/jrpie/Launcher?style=flat
  [shield-contribute]: https://img.shields.io/badge/contributions-welcome-007ec6.svg?style=flat
  [shield-license]: https://img.shields.io/badge/license-MIT-007ec6?style=flat

  [shield-gh-watch]: https://img.shields.io/github/watchers/jrpie/Launcher?label=Watch&style=social
  [shield-gh-star]: https://img.shields.io/github/stars/jrpie/Launcher?label=Star&style=social
  [shield-gh-fork]: https://img.shields.io/github/forks/jrpie/Launcher?label=Fork&style=social



<!-- Helpful resources -->

  [license]: https://github.com/jrpie/Launcher/blob/master/LICENSE
