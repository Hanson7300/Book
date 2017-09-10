package com.example.android.book.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class BookProvider extends ContentProvider {

    public static final String LOG_TAG = BookProvider.class.getSimpleName();
    private static final int WHOLE_TABLE = 100;
    private static final int SINGLE_BOOK = 200;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(BookContract.BookEntry.CONTENT_AUTHORITY, "books", WHOLE_TABLE);
        sUriMatcher.addURI(BookContract.BookEntry.CONTENT_AUTHORITY, "books/#", SINGLE_BOOK);
    }

    private BookDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new BookDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case WHOLE_TABLE:
                cursor = database.query(BookContract.BookEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SINGLE_BOOK:
                selection = BookContract.BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(BookContract.BookEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Can not query unknown URI");
        }
        //初始化通知监听
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case WHOLE_TABLE:
                return insertBook(uri, values);
            default:
                throw new IllegalArgumentException("Insertion not supported for " + uri);
        }
    }

    private Uri insertBook(Uri uri, ContentValues values) {

        //用户输入检测
        String name = values.getAsString(BookContract.BookEntry.COLUMN_BOOK_NAME);
        Integer amount = values.getAsInteger(BookContract.BookEntry.COLUMN_BOOK_AMOUNT);
        Float price = values.getAsFloat(BookContract.BookEntry.COLUMN_BOOK_PRICE);

        //输入为空时提示
        if (name == null) {
            throw new IllegalArgumentException("Book requires a name ");
        }
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("Book requires valid amount ");
        }
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Book requires valid price ");
        }

        //执行insert---------------这里不是Readable
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        Long insertedId = database.insert(BookContract.BookEntry.TABLE_NAME, null, values);
        if (insertedId == -1) {
            //如果insert失败,记录日志,提前返回null
            Log.e(LOG_TAG, "Fail to insert " + uri);
            return null;
        }
        //insert成功时发送通知
        getContext().getContentResolver().notifyChange(uri, null);
        //返回Uri
        return ContentUris.withAppendedId(uri, insertedId);
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues values,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case WHOLE_TABLE:
                return updateBook(uri, values, selection, selectionArgs);
            case SINGLE_BOOK:
                selection = BookContract.BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not support for " + uri);
        }
    }

    private int updateBook(Uri uri,
                           ContentValues values,
                           String selection,
                           String[] selectionArgs) {

        //用户输入检测,比insert更严格
        if(values.size() == 0){
            return 0;
        }

        if (values.containsKey(BookContract.BookEntry.COLUMN_BOOK_NAME)) {
            String name = values.getAsString(BookContract.BookEntry.COLUMN_BOOK_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Book requires a name ");
            }
        }
        if (values.containsKey(BookContract.BookEntry.COLUMN_BOOK_AMOUNT)) {
            Integer amount = values.getAsInteger(BookContract.BookEntry.COLUMN_BOOK_AMOUNT);
            if (amount == null || amount < 0) {
                throw new IllegalArgumentException("Book requires valid amount ");
            }
        }
        if (values.containsKey(BookContract.BookEntry.COLUMN_BOOK_PRICE)) {
            Float price = values.getAsFloat(BookContract.BookEntry.COLUMN_BOOK_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Book requires valid price ");
            }
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(BookContract.BookEntry.TABLE_NAME,
                values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case WHOLE_TABLE:
                rowsDeleted = database.delete(BookContract.BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SINGLE_BOOK:
                //selection 和selectionArgs 放到EditorActivity的deleteCurrentBook中也可以
                //删除特定行
                selection = BookContract.BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookContract.BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deleted is not support for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WHOLE_TABLE:
                return BookContract.BookEntry.CONTENT_LIST_TYPE;
            case SINGLE_BOOK:
                return BookContract.BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
