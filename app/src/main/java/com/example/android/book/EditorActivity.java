package com.example.android.book;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.book.data.BookContract;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private static int RESULT_LOAD_IMAGE = 1;
    private Uri openUri;
    private String mImageUri = null;

    //0代表数据无效,等待用户重新输入,1表示有效,返回MainActivity
    private static int INFORMATION_VALID_TAG;

    private EditText mNameEditText;
    private EditText mAuthorEditText;
    private EditText mPressEditText;
    private EditText mPriceEditText;
    private EditText mAmountEditText;
    private EditText mSalesEditText;
    private Button sell_button;

    private ImageView mImageView;

    //定义OnTouchListener 行为,如果用户有点击操作,记录在全局变量中
    private boolean mBookHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    private static final int BOOK_BOOK = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //渲染布局
        setContentView(R.layout.acticvity_editor);

        mNameEditText = (EditText) findViewById(R.id.name_editText);
        mAuthorEditText = (EditText) findViewById(R.id.author_editText);
        mPressEditText = (EditText) findViewById(R.id.press_editText);
        mPriceEditText = (EditText) findViewById(R.id.price_editText);
        mAmountEditText = (EditText) findViewById(R.id.amount_editText);
        mSalesEditText = (EditText) findViewById(R.id.sales_editText);
        sell_button = (Button) findViewById(R.id.sell_one);
        mImageView = (ImageView) findViewById(R.id.image_editor);

        //接受传入Uri,后台加载数据
        openUri = getIntent().getData();
        if (openUri != null) {

            //提示点击列表项的uri id
            long id = ContentUris.parseId(openUri);
            Toast.makeText(this, getString(R.string.open_book_with_uri) + " " + id, Toast.LENGTH_SHORT).show();

            //根据有无Uri改变标题
            setTitle(getString(R.string.edit_book));

            //初始化后台加载
            getSupportLoaderManager().initLoader(BOOK_BOOK, null, this);

        } else {
            setTitle(getString(R.string.add_a_book));
            mImageView.setImageURI(Uri.parse(BookContract.BookEntry.SELECT_IMAGE));
        }

        addListener();
    }

    private void addListener() {
        //设置点击监听器
        mNameEditText.setOnTouchListener(mTouchListener);
        mAuthorEditText.setOnTouchListener(mTouchListener);
        mPressEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mAmountEditText.setOnTouchListener(mTouchListener);
        mSalesEditText.setOnTouchListener(mTouchListener);

        mImageView.setOnTouchListener(mTouchListener);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        sell_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sellOne();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.image_editor);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            Toast.makeText(this, "Photo uri in gallery :" + picturePath, Toast.LENGTH_SHORT).show();
            mImageUri = picturePath;
        }

    }

    //渲染菜单栏
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meun_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteCurrentBook = menu.findItem(R.id.menu_delete_current_book);
        //如果传入Uri为空,隐藏菜单栏删除按钮
        if (openUri == null) {
            deleteCurrentBook.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send_email:
                sendEmail();
                return true;
            case R.id.menu_delete_current_book:
                showDeleteConfirmationDialog();
                return true;
            case R.id.menu_save:
                saveBook();
                if (INFORMATION_VALID_TAG == 1) {
                    finish();
                }
                return true;
            case android.R.id.home:
                // If the book hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveBook() {
        //输入检查,虽然BookProvider也有检查,但这里可以在本页面提示用户
        //如果忘记getText,将得不到用户输入
        String nameStringInput = mNameEditText.getText().toString().trim();
        String authorStringInput = mAuthorEditText.getText().toString().trim();
        String pressStringInput = mPressEditText.getText().toString().trim();
        String priceStringInput = mPriceEditText.getText().toString().trim();
        String amountStringInput = mAmountEditText.getText().toString().trim();
        String salesStringInput = mSalesEditText.getText().toString().trim();

        //去掉priceEditText 的$符号,如果用户输入的末位是小数点,也去掉
        String[] priceWith_sign = priceStringInput.split(" ");
        String purePrice = priceWith_sign[0];
        if (purePrice.contains(".")) {
            if (purePrice.indexOf(".") == purePrice.length()) {
                purePrice.substring(0, purePrice.length() - 1);
            }
        }

        //关键项任何一项为空,提示用户
        if (TextUtils.isEmpty(nameStringInput)
                || TextUtils.isEmpty(purePrice)
                || TextUtils.isEmpty(amountStringInput)) {
            Toast.makeText(this, R.string.information_not_valid, Toast.LENGTH_SHORT).show();
            //用户输入无效,留在当前页面
            INFORMATION_VALID_TAG = 0;
            return;
        }

        //可以为空的项目,填入默认值
        if (TextUtils.isEmpty(authorStringInput)) {
            authorStringInput = getString(R.string.unknown_author);
        }
        if (TextUtils.isEmpty(pressStringInput)) {
            pressStringInput = getString(R.string.unknown_press);
        }
        if (TextUtils.isEmpty(salesStringInput)) {
            salesStringInput = "0";
        }
        if (mImageUri == null) {
            mImageUri = "android.resource://com.example.android.book/drawable/ic_no_image";
        }

        ContentValues saveValues = new ContentValues();
        saveValues.put(BookContract.BookEntry.COLUMN_BOOK_NAME, nameStringInput);
        saveValues.put(BookContract.BookEntry.COLUMN_BOOK_AUTHOR, authorStringInput);
        saveValues.put(BookContract.BookEntry.COLUMN_BOOK_PRESS, pressStringInput);
        saveValues.put(BookContract.BookEntry.COLUMN_BOOK_PRICE, Float.valueOf(purePrice));
        saveValues.put(BookContract.BookEntry.COLUMN_BOOK_AMOUNT, Integer.valueOf(amountStringInput));
        saveValues.put(BookContract.BookEntry.COLUMN_BOOK_SALES, Integer.valueOf(salesStringInput));
        saveValues.put(BookContract.BookEntry.COLUMN_IMAGE_URI, mImageUri);

        //根据有无Uri判断使用insert还是update
        if (openUri == null) {
            //insert
            Uri insertedRowUri = getContentResolver().insert(BookContract.BookEntry.CONTENT_URI, saveValues);
            if (insertedRowUri == null) {
                Toast.makeText(this, R.string.error_with_saving_book, Toast.LENGTH_SHORT).show();
            } else {
                //成功insert,截取Uri末位id
                Long insertedRowId = ContentUris.parseId(insertedRowUri);
                Toast.makeText(this, getString(R.string.book_inserted_with_id)
                        + String.valueOf(insertedRowId), Toast.LENGTH_SHORT).show();
            }
        } else {
            //点击列表项产生的Uri一定是books/#结尾,update方法里带有uri match方法
            Integer updatedRowId = getContentResolver().update(openUri, saveValues, null, null);
            if (updatedRowId == -1) {
                Toast.makeText(this, R.string.error_with_update_book, Toast.LENGTH_SHORT).show();
            } else {
                //成功update,弹出toast
                Toast.makeText(this, R.string.successfully_update, Toast.LENGTH_SHORT).show();
            }
        }
        //insert或者update调用成功,设置为可以返回mainActivity
        INFORMATION_VALID_TAG = 1;
    }

    private void deleteCurrentBook() {
        //数据库中存在,才能调用删除(只在点击列表项打开编辑器时有用)
        if (openUri != null) {
            long deletedRowId = getContentResolver().delete(openUri, null, null);
            if (deletedRowId != -1) {
                Toast.makeText(this, R.string.book_deleted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.error_delete_book, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void sendEmail() {
        String nameStringInput = mNameEditText.getText().toString().trim();
        String authorStringInput = mAuthorEditText.getText().toString().trim();
        String pressStringInput = mPressEditText.getText().toString().trim();
        String priceStringInput = mPriceEditText.getText().toString().trim();

        //可以为空的项目,填入默认值
        if (TextUtils.isEmpty(authorStringInput)) {
            authorStringInput = getString(R.string.unknown_author);
        }
        if (TextUtils.isEmpty(pressStringInput)) {
            pressStringInput = getString(R.string.unknown_press);
        }

        //建立email intent 不带附件
        Intent email = new Intent(Intent.ACTION_SENDTO);

        //确保只有email app 接受intent
        email.setData(Uri.parse("mailto:"));
        email.putExtra(Intent.EXTRA_EMAIL, getString(R.string.email_address));
        email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        email.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.we_want_more_books)
                        + "Name: " + nameStringInput + "\n"
                        + "Author: " + authorStringInput + "\n"
                        + "Press: " + pressStringInput + "\n"
                        + "Price " + priceStringInput + "\n");

        //确保有邮件应用
        if (getIntent().resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(email, getString(R.string.send_email)));
        }
    }

    private void sellOne() {
        //获取EditText的值
        String amountStringInput = mAmountEditText.getText().toString().trim();
        String salesStringInput = mSalesEditText.getText().toString().trim();

        //转换为intger,进行加减
        Integer amount = Integer.valueOf(amountStringInput);
        Integer sales = Integer.valueOf(salesStringInput);

        if (amount != 0) {
            amount = amount - 1;
            sales = sales + 1;
        } else {
            Toast.makeText(this, R.string.please_send_order, Toast.LENGTH_SHORT).show();
        }
        //写入EditText
        mAmountEditText.setText(String.valueOf(amount));
        mSalesEditText.setText(String.valueOf(sales));
    }

    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        // 创建AlertDialog.Builder 并设置显示的内容,设置点击监听
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_changes_or_quit);
        builder.setPositiveButton(R.string.discard_, discardButtonClickListener);

        // User clicked the "Keep editing" button, so dismiss the dialog
        // and continue editing the book.
        builder.setNegativeButton(R.string.keep_edit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog unSaveAlertDialog = builder.create();
        unSaveAlertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_this_or);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteCurrentBook();
                finish();
            }
        });

        // User clicked the "Cancel" button, so dismiss the dialog
        // and continue editing the book.
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog deleteAlertDialog = builder.create();
        deleteAlertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (openUri == null) {
            return null;
        }
        //后台查询点击行的数据,全部数据列
        String[] projection = {
                BookContract.BookEntry._ID,
                BookContract.BookEntry.COLUMN_BOOK_NAME,
                BookContract.BookEntry.COLUMN_BOOK_AUTHOR,
                BookContract.BookEntry.COLUMN_BOOK_PRESS,
                BookContract.BookEntry.COLUMN_BOOK_PRICE,
                BookContract.BookEntry.COLUMN_BOOK_AMOUNT,
                BookContract.BookEntry.COLUMN_BOOK_SALES,
                BookContract.BookEntry.COLUMN_IMAGE_URI
        };
        return new CursorLoader(this,
                //使用传入的Uri查询,如果写成CONTENT_URI,会查询整个表,并返回第一行的数据
                openUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //用cuosor填充EditText,不同于MainActivity使用CursorAdapter,而是直接填充
        //moveToFirst写成moveToNext是没有数据的...
        if (data.moveToFirst()) {

            //从cursor读取数据
            String nameString = data.getString(data.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_NAME));
            String authorString = data.getString(data.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_AUTHOR));
            String pressString = data.getString(data.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_PRESS));
            Float priceInt = data.getFloat(data.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_PRICE));
            Integer amountInt = data.getInt(data.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_AMOUNT));
            Integer salesInt = data.getInt(data.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_SALES));

            String imageUri = data.getString(data.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_IMAGE_URI));

            //如果是编辑器默认值,设置为空,方便编辑
            if (authorString.equals(getString(R.string.unknown_author))) {
                mAuthorEditText.setText("");
            } else {
                mAuthorEditText.setText(authorString);
            }
            if (pressString.equals(getString(R.string.unknown_press))) {
                mPressEditText.setText("");
            } else {
                mPressEditText.setText(pressString);
            }

            //数据写入EditText
            mNameEditText.setText(nameString);
            mPriceEditText.setText(String.valueOf(priceInt));
            mAmountEditText.setText(String.valueOf(amountInt));
            mSalesEditText.setText(String.valueOf(salesInt));


            //设置图片显示格式
            BitmapFactory.Options newOption = new BitmapFactory.Options();
            newOption.inPreferredConfig = Bitmap.Config.RGB_565;

            //图片写入TextView
            if (imageUri != null) {
                //图片Uri有两种,默认封面来自drawable,从图库中选择的来自SD卡,方法不同
                if (imageUri.contains("resource://")) {
                    mImageView.setImageURI(Uri.parse(imageUri));
                } else {
                    mImageView.setImageBitmap(BitmapFactory.decodeFile(imageUri,newOption));
                }
            } else {
                mImageView.setImageURI(Uri.parse(BookContract.BookEntry.SELECT_IMAGE));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //所有EditText设置为空
        mNameEditText.setText("");
        mAuthorEditText.setText("");
        mPressEditText.setText("");
        mPriceEditText.setText("");
        mAmountEditText.setText("");
        mSalesEditText.setText("");
        mImageView.setImageURI(Uri.parse(BookContract.BookEntry.NO_IMAGE_URI));
    }


}
