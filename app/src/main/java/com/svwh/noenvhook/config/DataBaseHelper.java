package com.svwh.noenvhook.config;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.io.File;

/**
 * Opens the shared config database from a caller-provided external directory.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "killer_hook.db";
    private static final int DATABASE_VERSION = 1;

    public DataBaseHelper(String databaseDir, @Nullable Context context) {
        super(new DatabaseContext(context, databaseDir), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Config database is created and managed by the host app.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Config database migrations are owned by the host app.
    }

    private static final class DatabaseContext extends ContextWrapper {

        private final String databaseDir;

        private DatabaseContext(Context base, String databaseDir) {
            super(base);
            this.databaseDir = databaseDir;
        }

        @Override
        public File getDatabasePath(String name) {
            return new File(databaseDir, name);
        }
    }
}
