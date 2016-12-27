package com.phonenumber.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.phonenumber.app.phonenumbers.db.PhoneNumberEntry;

import java.sql.SQLException;

public class DbHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = DbHelper.class.getName();

    private static final String DATABASE_NAME = "phone_numbers.db";
    private static final int DATABASE_VERSION = 14;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, PhoneNumberEntry.class);
        } catch (SQLException e) {
            Log.e(TAG, "Unable to create databases", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, PhoneNumberEntry.class, true);
        } catch (SQLException e) {
            Log.e(TAG, "Unable to upgrade databases", e);
        }
        onCreate(database, connectionSource);
    }

    public static boolean isUniqueConstraintViolation(Throwable e) {
        return e instanceof SQLiteConstraintException
                || e.getCause() instanceof SQLiteConstraintException
                || e.getCause().getCause() instanceof SQLiteConstraintException;
    }
}