package club.infolab.isc.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import club.infolab.isc.database.DBRecords;

public class SQLHelper extends SQLiteOpenHelper {
    SQLHelper(Context context) {
        super(context, DBRecords.DATABASE_NAME, null, DBRecords.DATABASE_VERSION);
    }

    @Override
    public void onCreate (SQLiteDatabase db){
        String query = "CREATE TABLE " + DBRecords.TABLE_NAME + " (" +
                DBRecords.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DBRecords.COLUMN_NAME + " TEXT, " +
                DBRecords.COLUMN_DATE + " TEXT, " +
                DBRecords.COLUMN_IS_LOADED + " INTEGER, " +
                DBRecords.COLUMN_JSON + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + DBRecords.TABLE_NAME);
        onCreate(db);
    }
}
