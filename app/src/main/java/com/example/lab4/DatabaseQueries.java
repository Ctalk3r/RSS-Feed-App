package com.example.lab4;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;


public class DatabaseQueries {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;


    public DatabaseQueries(Context context)
    {
        dbHelper = new DatabaseHelper(context);
    }

    public void open()
    {
        try{
            db = dbHelper.getWritableDatabase();
        }
        catch (SQLiteException ex)
        {
            db = dbHelper.getReadableDatabase();
        }
    }

    public void close()
    {
        dbHelper.close();
    }

    public RssFeedModel getRSSModels(int id)
    {
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_RSSModel + " WHERE _id = " + id + ";", null);

        if (cursor.moveToFirst())
        {
            long _id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID_RSSModel));
            String title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE_RSSModel));
            String body = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_BODY_RSSModel));
            String link = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LINK_RSSModel));
            String image = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_RSSModel));

            cursor.close();
            return new RssFeedModel(_id, title, body, link, image);
        }
        else
            return null;
    }


    public ArrayList<RssFeedModel> getRSSModels()
    {
        ArrayList<RssFeedModel> list = new ArrayList<>();

        Cursor cursor = db.query(DatabaseHelper.TABLE_RSSModel, null, null, null, null, null, null);
        if (cursor.moveToFirst())
        do
        {
            long id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID_RSSModel));
            String title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE_RSSModel));
            String body = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_BODY_RSSModel));
            String link = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LINK_RSSModel));
            String image = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_RSSModel));
            RssFeedModel rssFeedModel = new RssFeedModel(id, title, link, body, image);

            list.add(rssFeedModel);
        }
        while(cursor.moveToNext());


        cursor.close();
        return list;
    }


    public long insert(RssFeedModel rssFeedModel)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE_RSSModel, rssFeedModel.title);
        values.put(DatabaseHelper.COLUMN_BODY_RSSModel, rssFeedModel.description);
        values.put(DatabaseHelper.COLUMN_LINK_RSSModel, rssFeedModel.link);
        values.put(DatabaseHelper.COLUMN_IMAGE_RSSModel, rssFeedModel.image);
        return db.insert(DatabaseHelper.TABLE_RSSModel, "_", values);
    }

    public void removeRSSModel(long _id)
    {
        db.delete(DatabaseHelper.TABLE_RSSModel, "_id = ?", new String[] {Long.toString(_id)});
    }

    public void updateRSSModel(RssFeedModel rssFeedModel)
    {
        removeRSSModel(rssFeedModel.id);
        insert(rssFeedModel);
    }
}
