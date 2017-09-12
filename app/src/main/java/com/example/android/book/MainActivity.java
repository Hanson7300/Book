package com.example.android.book;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.book.data.BookContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int BOOK_LOADER = 0;
    BookCursorAdapter mBookCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置toolbar作为AppBar
        Toolbar toolBar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,EditorActivity.class);
                startActivity(intent);
            }
        });

        //启动loader,后台加载数据库,显示book
        getSupportLoaderManager().initLoader(BOOK_LOADER,null,this);

        //绑定adapter,adapter是没有数据的
        ListView listView = (ListView)findViewById(R.id.list_view);
        mBookCursorAdapter = new BookCursorAdapter(this,null);
        listView.setAdapter(mBookCursorAdapter);

        //设置空视图
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        //点击打开编辑器,传入数字结尾的Uri
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editIntent  = new Intent(MainActivity.this,EditorActivity.class);
                //这里传入的是id不是position,传入position会导致各种奇葩问题...
                editIntent.setData(Uri.withAppendedPath(BookContract.BookEntry.CONTENT_URI,
                        String.valueOf(id)));
                startActivity(editIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_insert_dummy_book:
                insertDummyBook();
                return true;
            case R.id.action_delete_all_books:
                deleteAllBooks();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDummyBook(){

        ContentValues values = new ContentValues();
        values.put(BookContract.BookEntry.COLUMN_BOOK_NAME,getString(R.string.dummy_name));
        values.put(BookContract.BookEntry.COLUMN_BOOK_AUTHOR,getString(R.string.dummy_author));
        values.put(BookContract.BookEntry.COLUMN_BOOK_PRESS,getString(R.string.dummy_press));
        values.put(BookContract.BookEntry.COLUMN_BOOK_AMOUNT,100);
        values.put(BookContract.BookEntry.COLUMN_BOOK_PRICE,25.7);
        values.put(BookContract.BookEntry.COLUMN_BOOK_SALES,0);
        values.put(BookContract.BookEntry.COLUMN_IMAGE_URI,
                BookContract.BookEntry.DUMMY_COVER);

        Uri insertedRowUri = getContentResolver().insert(BookContract.BookEntry.CONTENT_URI,values);
        long insertedId = ContentUris.parseId(insertedRowUri);
        if(insertedRowUri!=null){
            //---------------------------------------------------------------------
            Toast.makeText(MainActivity.this,getString(R.string.inserted_with_id)+" "+ insertedId,Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAllBooks(){
        int deletedRows = getContentResolver().delete(BookContract.BookEntry.CONTENT_URI,null,null);
        Toast.makeText(this,getString(R.string.delete_)+ deletedRows+getString(R.string._books),Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //建立后台查询,只查询需要的列,(sales不需要)
        String[] projection  = {
                BookContract.BookEntry._ID,
                BookContract.BookEntry.COLUMN_BOOK_NAME,
                BookContract.BookEntry.COLUMN_BOOK_AUTHOR,
                BookContract.BookEntry.COLUMN_BOOK_PRESS,
                BookContract.BookEntry.COLUMN_BOOK_PRICE,
                BookContract.BookEntry.COLUMN_BOOK_AMOUNT,
                BookContract.BookEntry.COLUMN_IMAGE_URI,
        };
        return new CursorLoader(this,
                BookContract.BookEntry.CONTENT_URI,projection,null,null,null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //cursor数据写入adapter
        mBookCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //清空adapter数据
        mBookCursorAdapter.swapCursor(null);
    }
}
