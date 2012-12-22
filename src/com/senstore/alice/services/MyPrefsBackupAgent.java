package com.senstore.alice.services;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.util.Log;

public class MyPrefsBackupAgent extends BackupAgentHelper {
    // The name of the SharedPreferences file
    public static final String PREFS = "com.senstore.alice.harvard_preferences";

    // A key to uniquely identify the set of backup data
    static final String PREFS_BACKUP_KEY = "MyPrefs";

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
    	Log.i("Alice","Backup created");
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS);
        addHelper(PREFS_BACKUP_KEY, helper);
    }
}