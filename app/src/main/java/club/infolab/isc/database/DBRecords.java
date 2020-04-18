package club.infolab.isc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

public class DBRecords {
    public static final String DATABASE_NAME = "tests_record.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "tableTests";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_DATE = "Date";
    public static final String COLUMN_IS_LOADED = "IsLoaded";
    public static final String COLUMN_JSON = "Json";

    public static final int NUM_COLUMN_ID = 0;
    public static final int NUM_COLUMN_NAME = 1;
    public static final int NUM_COLUMN_DATE = 2;
    public static final int NUM_COLUMN_IS_LOADED = 3;
    public static final int NUM_COLUMN_JSON = 4;

    private SQLiteDatabase mDataBase;

    public DBRecords(Context context){
        SQLHelper sqlHelper = new SQLHelper(context);
        mDataBase = sqlHelper.getWritableDatabase();
    }

    public long getCountRows() {
        long count = DatabaseUtils.queryNumEntries(mDataBase, TABLE_NAME);
        return count;
    }

    public long insert(String testName, String date, int isLoaded, String json) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, testName);
        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_IS_LOADED, isLoaded);
        cv.put(COLUMN_JSON, json);
        return mDataBase.insert(TABLE_NAME, null, cv);
    }

    public int update(Record record) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, record.getName());
        cv.put(COLUMN_DATE, record.getDate());
        cv.put(COLUMN_IS_LOADED, record.getIsLoaded());
        cv.put(COLUMN_JSON, record.getJson());
        return mDataBase.update(TABLE_NAME, cv, COLUMN_ID + " = ?",new String[] { String.valueOf(record.getId())});
    }

    public void deleteAll() {
        mDataBase.delete(TABLE_NAME, null, null);
    }

    public void delete(long id) {
        mDataBase.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[] { String.valueOf(id) });
    }

    public Record select(long id) {
        Cursor mCursor = mDataBase.query(TABLE_NAME, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        mCursor.moveToFirst();
        String testName = mCursor.getString(NUM_COLUMN_NAME);
        String date = mCursor.getString(NUM_COLUMN_DATE);
        int isLoaded = mCursor.getInt(NUM_COLUMN_IS_LOADED);
        String json = mCursor.getString(NUM_COLUMN_JSON);
        return new Record(id, testName, date, isLoaded, json);
    }

    public ArrayList<Record> selectAll() {
        Cursor mCursor = mDataBase.query(TABLE_NAME, null,
                null, null, null, null, null);

        ArrayList<Record> arr = new ArrayList<Record>();
        mCursor.moveToFirst();
        if (!mCursor.isAfterLast()) {
            do {
                long id = mCursor.getLong(NUM_COLUMN_ID);
                String testName = mCursor.getString(NUM_COLUMN_NAME);
                String date = mCursor.getString(NUM_COLUMN_DATE);
                int isLoaded = mCursor.getInt(NUM_COLUMN_IS_LOADED);
                String json = mCursor.getString(NUM_COLUMN_JSON);
                arr.add(new Record(id, testName, date, isLoaded, json));
            } while (mCursor.moveToNext());
        }
        return arr;
    }
}
