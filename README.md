# Focus launcher

A minimal Android launcher: big tiles, a first-run app allowlist, and multiple user-organised pages. Search only includes apps explicitly permitted during setup.
Java, no third-party libraries.

## Get the APK without Android Studio (GitHub builds it)
1. Create a new repo on GitHub, then from this folder:
   ```
   git init
   git add .
   git commit -m "Focus launcher"
   git branch -M main
   git remote add origin https://github.com/<you>/<repo>.git
   git push -u origin main
   ```
2. On GitHub open the **Actions** tab -> the **Build APK** run (about 3-5 minutes). Wait for the green tick.
3. Open the finished run -> **Artifacts** -> download `focus-debug-apk` (a zip with `app-debug.apk` inside).
4. Get the APK onto the phone (Drive, email, USB). Unzip it there if needed, then tap the APK.
   Allow "Install unknown apps" for the app you opened it from. If Play Protect warns, choose *Install anyway*.
5. Press the **Home** button. Android asks which home app to use -> choose **Focus** -> **Always**.
   Or long-press the clock inside the app -> *Set as default home app*.

Every code change = push again, wait for the build, download, reinstall.

## Run it with Android Studio (faster for tinkering)
1. Android Studio -> **Open** -> pick this `FocusLauncher` folder. Let Gradle sync (it downloads Gradle 8.9 on its own).
   If Studio offers to upgrade the Android Gradle Plugin, accepting is fine.
2. Turn on USB debugging on the phone, plug it in, pick it in the device dropdown, press Run.
   To just make an APK file: **Build -> Build APK(s)** (it lands in `app/build/outputs/apk/debug/`).
3. Press the **Home** button and choose **Focus** -> **Always**.

To go back to your old launcher: Settings -> Apps -> Default apps -> Home app.

## Using it
On the first launch, select the apps you want Focus to expose and tap **Continue**. The home screen, search, and tile picker only use this approved list. Long-press the clock and choose **Manage permitted apps** to change it later. This is a launcher-level allowlist, not an Android-wide app lock: other launchers and Android Settings can still show installed apps.

| Gesture | What happens |
|---|---|
| Tap a tile | Opens the app |
| Long-press a tile | Change app / Remove from home |
| Tap a `+` tile | Choose an app for it |
| Tap **Search permitted apps** | Search only apps approved during setup |
| Tap `‹` / `›` | Move between your pages |
| Tap the page name | Rename or delete the current page |
| Tap `+` beside page name | Create another page |
| Long-press the clock | Grayscale icons, manage permitted apps, set default home app |

## Where to tinker
| Want to change | Where |
|---|---|
| Number of tiles per row / rows | `COLUMNS`, `ROWS` at the top of `HomeActivity.java` |
| Colors | `res/values/colors.xml` |
| Tile shape / corner radius | `res/drawable/tile_bg.xml` |
| Icon size / label size | `res/layout/item_tile.xml` |
| Clock look and formats | `res/layout/activity_home.xml` |
| Which apps are auto-filled on first run | `AppRepository.guessDefaultApps()` |

## Files
- `HomeActivity.java` - home screen, tile grid, menus
- `AppPickerActivity.java` - first-run allowlist setup and allowlist-only search/pick-for-tile
- `AppRepository.java` - everything that talks to PackageManager
- `Prefs.java` - saved tiles and settings
- `Ui.java` - window-inset padding (status bar / keyboard) and dp helper
