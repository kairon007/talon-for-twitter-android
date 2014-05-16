package com.klinker.android.twitter.data.sq_lite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class QueuedDataSource {

    // provides access to the database
    public static QueuedDataSource dataSource = null;

    /*

    This is used so that we don't have to open and close the database on different threads or fragments
    every time. This will facilitate it between all of them to avoid Illegal State Exceptions.

     */
    public static QueuedDataSource getInstance(Context context) {

        // if the datasource isn't open or it the object is null
        if (dataSource == null ||
                dataSource.getDatabase() == null ||
                !dataSource.getDatabase().isOpen()) {

            dataSource = new QueuedDataSource(context); // create the database
            dataSource.open(); // open the database
        }

        return dataSource;
    }

    private SQLiteDatabase database;
    private QueuedSQLiteHelper dbHelper;
    public String[] allColumns = { QueuedSQLiteHelper.COLUMN_ID, QueuedSQLiteHelper.COLUMN_ACCOUNT,
            QueuedSQLiteHelper.COLUMN_TEXT, QueuedSQLiteHelper.COLUMN_TYPE,
            QueuedSQLiteHelper.COLUMN_TIME, QueuedSQLiteHelper.COLUMN_ALARM_ID };

    public QueuedDataSource(Context context) {
        dbHelper = new QueuedSQLiteHelper(context);
    }

    public void open() throws SQLException {

        try {
            database = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

    public void close() {
        try {
            dbHelper.close();
        } catch (Exception e) {

        }
        database = null;
        dataSource = null;
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    public QueuedSQLiteHelper getHelper() {
        return dbHelper;
    }

    public synchronized void createDraft(String message, int account) {
        ContentValues values = new ContentValues();

        values.put(QueuedSQLiteHelper.COLUMN_ACCOUNT, account);
        values.put(QueuedSQLiteHelper.COLUMN_TEXT, message);
        values.put(QueuedSQLiteHelper.COLUMN_TIME, 0l);
        values.put(QueuedSQLiteHelper.COLUMN_ALARM_ID, 0l);
        values.put(QueuedSQLiteHelper.COLUMN_TYPE, QueuedSQLiteHelper.TYPE_DRAFT);

        try {
            database.insert(QueuedSQLiteHelper.TABLE_QUEUED, null, values);
        } catch (Exception e) {
            open();
            database.insert(QueuedSQLiteHelper.TABLE_QUEUED, null, values);
        }
    }

    public synchronized void deleteDraft(String message) {

        try {
            database.delete(QueuedSQLiteHelper.TABLE_QUEUED,
                    QueuedSQLiteHelper.COLUMN_TEXT + " = ?",
                    new String[] {message});
        } catch (Exception e) {
            open();
            database.delete(QueuedSQLiteHelper.TABLE_QUEUED,
                    QueuedSQLiteHelper.COLUMN_TEXT + " = ?",
                    new String[] {message});
        }
    }

    public synchronized void createQueuedTweet(String message, int account) {
        ContentValues values = new ContentValues();

        values.put(QueuedSQLiteHelper.COLUMN_ACCOUNT, account);
        values.put(QueuedSQLiteHelper.COLUMN_TEXT, message);
        values.put(QueuedSQLiteHelper.COLUMN_TIME, 0l);
        values.put(QueuedSQLiteHelper.COLUMN_ALARM_ID, 0l);
        values.put(QueuedSQLiteHelper.COLUMN_TYPE, QueuedSQLiteHelper.TYPE_QUEUED_TWEET);

        try {
            database.insert(QueuedSQLiteHelper.TABLE_QUEUED, null, values);
        } catch (Exception e) {
            open();
            database.insert(QueuedSQLiteHelper.TABLE_QUEUED, null, values);
        }
    }

    public synchronized void deleteQueuedTweet(String message) {

        try {
            database.delete(QueuedSQLiteHelper.TABLE_QUEUED,
                    QueuedSQLiteHelper.COLUMN_TEXT + " = ?",
                    new String[] {message});
        } catch (Exception e) {
            open();
            database.delete(QueuedSQLiteHelper.TABLE_QUEUED,
                    QueuedSQLiteHelper.COLUMN_TEXT + " = ?",
                    new String[] {message});
        }
    }

    public synchronized void deleteAllDrafts() {

        try {
            database.delete(QueuedSQLiteHelper.TABLE_QUEUED,
                    QueuedSQLiteHelper.COLUMN_TYPE + " = " + QueuedSQLiteHelper.TYPE_DRAFT, null);
        } catch (Exception e) {
            open();
            database.delete(QueuedSQLiteHelper.TABLE_QUEUED,
                    QueuedSQLiteHelper.COLUMN_TYPE + " = " + QueuedSQLiteHelper.TYPE_DRAFT, null);
        }
    }

    public synchronized Cursor getDraftsCursor() {

        Cursor cursor;
        try {
            cursor = database.query(QueuedSQLiteHelper.TABLE_QUEUED,
                    allColumns, QueuedSQLiteHelper.COLUMN_TYPE + " = " + QueuedSQLiteHelper.TYPE_DRAFT, null, null, null, null);
        } catch (Exception e) {
            open();
            cursor = database.query(QueuedSQLiteHelper.TABLE_QUEUED,
                    allColumns, QueuedSQLiteHelper.COLUMN_TYPE + " = " + QueuedSQLiteHelper.TYPE_DRAFT, null, null, null, null);
        }

        return cursor;
    }

    public String[] getDrafts() {

        Cursor cursor = getDraftsCursor();

        ArrayList<String> drafts = new ArrayList<String>();

        if (cursor.moveToFirst()) {
            do {
                String draft = cursor.getString(cursor.getColumnIndex(QueuedSQLiteHelper.COLUMN_TEXT));
                if (!draft.equals("")) {
                    drafts.add(draft);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        String[] draftArr = new String[drafts.size()];
        for (int i = 0; i <draftArr.length; i++) {
            draftArr[i] = drafts.get(i);
        }
        return draftArr;
    }

    public synchronized Cursor getQueuedTweetsCursor(int account) {

        Cursor cursor;
        try {
            cursor = database.query(QueuedSQLiteHelper.TABLE_QUEUED,
                    allColumns,
                    QueuedSQLiteHelper.COLUMN_TYPE + " = " + QueuedSQLiteHelper.TYPE_QUEUED_TWEET +
                            " AND " + QueuedSQLiteHelper.COLUMN_ACCOUNT + " = ?",
                    new String[] {"" + account}, null, null, null);

        } catch (Exception e) {
            open();
            cursor = database.query(QueuedSQLiteHelper.TABLE_QUEUED,
                    allColumns,
                    QueuedSQLiteHelper.COLUMN_TYPE + " = " + QueuedSQLiteHelper.TYPE_QUEUED_TWEET +
                            " AND " + QueuedSQLiteHelper.COLUMN_ACCOUNT + " = ?",
                    new String[] {"" + account}, null, null, null);
        }

        return cursor;
    }

    public String[] getQueuedTweets(int account) {

        Cursor cursor = getQueuedTweetsCursor(account);

        ArrayList<String> queued = new ArrayList<String>();

        if (cursor.moveToFirst()) {
            do {
                String draft = cursor.getString(cursor.getColumnIndex(QueuedSQLiteHelper.COLUMN_TEXT));
                if (!draft.equals("")) {
                    queued.add(draft);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        String[] arr = new String[queued.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = queued.get(i);
        }
        return arr;
    }
}
