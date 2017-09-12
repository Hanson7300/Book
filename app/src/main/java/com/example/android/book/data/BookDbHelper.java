package com.example.android.book.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BookDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "store.db";
    private static final int DATA_BASE_VERSION = 1;

    //构造函数
    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATA_BASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_TABLE =  "CREATE TABLE " + BookContract.BookEntry.TABLE_NAME + " ("
                + BookContract.BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookContract.BookEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL, "
                + BookContract.BookEntry.COLUMN_BOOK_AUTHOR + " TEXT, "
                + BookContract.BookEntry.COLUMN_BOOK_PRESS + " TEXT, "
                + BookContract.BookEntry.COLUMN_BOOK_PRICE+" REAL NOT NULL, "
                + BookContract.BookEntry.COLUMN_BOOK_SALES+" INTEGER NOT NULL, "
                + BookContract.BookEntry.COLUMN_BOOK_AMOUNT + " INTEGER NOT NULL, "
                + BookContract.BookEntry.COLUMN_IMAGE_URI +" TEXT );";

        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
