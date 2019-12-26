package com.example.lab4;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "databasename.db";
    private static final int SCHEMA = 15;

    public static final String TABLE_RSSModel = "RSS_model";
    public static final String COLUMN_ID_RSSModel = "_id";
    public static final String COLUMN_TITLE_RSSModel = "title";
    public static final String COLUMN_BODY_RSSModel = "body";
    public static final String COLUMN_LINK_RSSModel = "link";
    public static final String COLUMN_IMAGE_RSSModel = "image";

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, SCHEMA);

    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RSSModel + " (" + COLUMN_ID_RSSModel +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_TITLE_RSSModel + " TEXT, "
        + COLUMN_BODY_RSSModel + " TEXT, " + COLUMN_LINK_RSSModel  + " TEXT, " + COLUMN_IMAGE_RSSModel + " TEXT);");
        // db.execSQL("INSERT  INTO " + TABLE_RSSModel + " (" + COLUMN_TITLE_RSSModel + ", " + COLUMN_BODY_RSSModel + ", " + COLUMN_LINK_RSSModel + ") VALUES ('Simple title', 'Test note', '23.12.2019');" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RSSModel);
        onCreate(db);
    }
}
