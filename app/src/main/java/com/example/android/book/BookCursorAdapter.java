package com.example.android.book;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.book.data.BookContract;


public class BookCursorAdapter extends CursorAdapter {
    //构造函数
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //找到TextView和ImageView
        TextView name = (TextView) view.findViewById(R.id.name_text_view);
        TextView author = (TextView) view.findViewById(R.id.author_text_view);
        TextView press = (TextView) view.findViewById(R.id.press_text_view);
        TextView amount = (TextView) view.findViewById(R.id.amount_text_view);
        TextView price = (TextView) view.findViewById(R.id.price_text_view);
        ImageView image = (ImageView) view.findViewById(R.id.image_main);

        //从Cursor读取数据并填充
        String nameString = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_BOOK_NAME));
        String authorString = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_BOOK_AUTHOR));
        String pressString = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_BOOK_PRESS));
        Integer amountInt = cursor.getInt(cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_BOOK_AMOUNT));
        Float priceFloat = cursor.getFloat(cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_BOOK_PRICE));

        String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_IMAGE_URI));

        //这里可以在author,press,sales为空时写入默认值
        //但是除了DummyDAta之外都要通过编辑器编辑(保存前编辑器也会进行检查),所以可以省去
        name.setText(nameString);
        author.setText(authorString);
        press.setText(pressString);
        amount.setText(String.valueOf(amountInt));

        if (image != null) {
            //图片Uri有两种,默认封面来自drawable,从图库中选择的来自SD卡,方法不同
            if (imageUri.contains("resource://")) {
                image.setImageURI(Uri.parse(imageUri));
            } else {
                image.setImageBitmap(BitmapFactory.decodeFile(imageUri));
            }
        } else {
            image.setImageURI(Uri.parse(BookContract.BookEntry.NO_IMAGE_URI));
        }

        //价格前添加$符号
        price.setText("$ " + String.valueOf(priceFloat));
    }
}
