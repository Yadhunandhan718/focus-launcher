package com.example.focuslauncher;

/** A launchable app: its package name and the label shown to the user. */
final class AppInfo {
    final String packageName;
    final String label;

    AppInfo(String packageName, String label) {
        this.packageName = packageName;
        this.label = label;
    }

    /** ArrayAdapter shows toString(), so the list rows display the label. */
    @Override
    public String toString() {
        return label;
    }
}
