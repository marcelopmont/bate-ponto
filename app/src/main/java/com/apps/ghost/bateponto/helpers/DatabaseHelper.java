package com.apps.ghost.bateponto.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.apps.ghost.bateponto.models.ClockIn;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Marcelo on 02/08/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "BatePontoBase";

    /* Register clock in */
    private static final String TABLE_CLOCKIN = "clockin";

    private static final String KEY_CLOCKIN_ID = "id";
    private static final String KEY_CLOCKIN_START = "start";
    private static final String KEY_CLOCKIN_DURATION = "duration";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PATIENTS_TABLE = "CREATE TABLE " + TABLE_CLOCKIN + "("
                + KEY_CLOCKIN_ID + " INTEGER PRIMARY KEY," + KEY_CLOCKIN_START + " INTEGER,"
                + KEY_CLOCKIN_DURATION + " REAL)";
        db.execSQL(CREATE_PATIENTS_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLOCKIN);

        onCreate(db);
    }

    /*
    *       Clockin table operations
     */
    public void addClockIn(ClockIn clockIn) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_CLOCKIN_START, clockIn.getStartTime().getTime());
        contentValues.put(KEY_CLOCKIN_DURATION, clockIn.getDuration());

        db.insert(TABLE_CLOCKIN, null, contentValues);
        db.close();
    }

    public List<ClockIn> getClockInList(){
        List<ClockIn> clockInList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_CLOCKIN;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                ClockIn clockIn = new ClockIn();
                clockIn.setId(cursor.getInt(0));
                long dateInMillis = cursor.getLong(1);
                clockIn.setStartTime(new Date(dateInMillis));
                clockIn.setDuration(cursor.getDouble(2));

                clockInList.add(clockIn);
            } while (cursor.moveToNext());
        }

        return clockInList;
    }

}
