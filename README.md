# Focus Launcher

**be your phone's master - not the other way around**

Focus is a minimal Android home-screen launcher built with Java and the Android framework. It gives you a deliberately small home screen, a user-controlled list of permitted apps, searchable app access, and multiple pages for organising apps.

## What's included

- First-run welcome screen with Focus branding and the project quote.
- First-run app selection with an explicit final confirmation. Apps not selected are hidden from Focus.
- The permitted-app list can be changed later in **Settings & tweaks**.
- Six home-screen tiles per page (2 columns × 3 rows).
- Add pages with the `+` control; tap the page title to rename or delete a page.
- Tap an empty `+` tile to add an app. Long-press an occupied tile to replace or remove it.
- Search opens a searchable list of permitted apps. Selecting an app launches it.
- Settings & tweaks: permitted apps, grayscale icons, show/hide clock, show/hide date, 12/24-hour clock display, Android date/time settings, and default-home settings.

The date/time screen opens Android's system settings. Android does not allow an ordinary launcher to silently change the device's system clock.

## Build an APK with GitHub Actions

1. Push this project to your GitHub repository.
2. Open the repository's **Actions** tab and select the **Build APK** workflow.
3. Wait for the workflow to finish, open the run, and download the `focus-debug-apk` artifact.
4. Extract the artifact ZIP and install `app-debug.apk` on your phone. If Android asks, allow installation from the app you used to open the APK.
5. Press Home and select **Focus** as your default home app.

For each code update, push the changed project files to GitHub. The workflow will build a fresh APK.

## Build locally

Open this folder in Android Studio, allow Gradle sync, then choose **Build → Build APK(s)**. The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Git update workflow

If you downloaded an updated project ZIP, extract it and copy the contents of the `focus-launcher-main` folder into your existing repository folder (do not copy the outer ZIP folder as a nested project). Then run:

```bash
git status
git add .
git commit -m "Improve Focus launcher setup, search and settings"
git push origin main
```

If Git reports a conflict, stop and resolve the conflict before committing. Do not delete your `.git` directory; it contains the connection to your existing GitHub repository.

## Main source files

| File | Purpose |
|---|---|
| `HomeActivity.java` | Welcome screen, home screen, pages, tile actions, settings |
| `AppPickerActivity.java` | First-run app selection, app search, app selection for tiles |
| `AppRepository.java` | Lists, labels, icons and launches installed apps |
| `Prefs.java` | Saves the allowlist, pages, tiles and display preferences |
| `Ui.java` | System-bar and keyboard inset handling |
| `res/layout/activity_welcome.xml` | First-run Focus landing screen |
| `res/layout/activity_home.xml` | Main launcher screen |
| `res/layout/activity_picker.xml` | Search and app selection screen |

## Important note

The permitted-app list is a **Focus launcher allowlist**, not an Android-wide app lock. Other launchers, Android Settings, notifications, and system surfaces may still expose installed apps.
