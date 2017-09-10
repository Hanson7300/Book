package com.example.android.book.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Hansson on 2017/9/7.
 */

public final class BookContract {

    //确保没有人能创建实例
    private BookContract(){}

    public static final class BookEntry implements BaseColumns{

        public static final String CONTENT_AUTHORITY="com.example.android.books";
        public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);
        public static final String BOOK_PATH = "books";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,BOOK_PATH);

        public static final String TABLE_NAME ="books";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_BOOK_NAME = "name";
        public static final String COLUMN_BOOK_AUTHOR= "author";
        public static final String COLUMN_BOOK_PRESS ="press";
        public static final String COLUMN_BOOK_AMOUNT= "amount";
        public static final String COLUMN_BOOK_PRICE = "price";
        public static final String COLUMN_BOOK_SALES = "sales";

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + BOOK_PATH;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + BOOK_PATH;
    }
}
