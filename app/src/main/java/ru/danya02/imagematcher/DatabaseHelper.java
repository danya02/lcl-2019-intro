package ru.danya02.imagematcher;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DatabaseHelper {
    private Context context;

    DatabaseHelper(Context context) {
        this.context = context;
    }

    private SQLiteDatabase dbase;

    void instantiateDatabase() {
        Log.i("dbHelper", "instantiateDatabase()");
        dbase = context.openOrCreateDatabase("pictureDb.sqlite", SQLiteDatabase.OPEN_READWRITE, null, new DatabaseErrorHandler() {
            @Override
            public void onCorruption(SQLiteDatabase dbObj) {
                Log.wtf("dbHelper", "Database corruption handler invoked?!");
            }
        });
        dbase.execSQL(context.getString(R.string.pragma_enable_foreignkey));
        dbase.execSQL("create table if not exists category (id integer primary key autoincrement);");
        dbase.execSQL("create table if not exists picture (id integer primary key autoincrement, name string unique, mycategory integer, foreign key(mycategory) references category(id) );");
        createPicStatement = dbase.compileStatement(context.getString(R.string.prepared_statement_insert_picture));
        createCategoryStatement = dbase.compileStatement("insert into category default values;");
        getPictureCategory = dbase.compileStatement("select mycategory from picture where id=?;");
        setPictureCategory = dbase.compileStatement("update picture set mycategory=? where id=?;");
    }

    public void disconnectDatabase() {
        Log.i("dbHelper", "disconnectDatabase()");
        dbase.close();

    }

    public int getCategoryCount() {
        Cursor cursor = dbase.rawQuery("SELECT * FROM category", null);
        int count = cursor.getCount();
        cursor.close();
        Log.i("dbHelper", String.format("getCategoryCount() -> %d", count));
        return count;
    }

    private SQLiteStatement createPicStatement;

    public long createPicture(String name) {
        Log.i("dbHelper", "createPicture()");
        createPicStatement.bindString(1, name);
        return createPicStatement.executeInsert();

    }

    private SQLiteStatement createCategoryStatement;
    private SQLiteStatement getPictureCategory;
    private SQLiteStatement setPictureCategory;

    public long createCategory(long[] ids) {
        Log.i("dbaseHelper", "createCategory(...)");
        dbase.beginTransaction();
        long mycat = createCategoryStatement.executeInsert();
        Log.d("dbaseHelper", "mycat: " + mycat);
        for (long id : ids) {

            {
                getPictureCategory.bindLong(1, id);
                long othercat = getPictureCategory.simpleQueryForLong();
                Log.d("dbaseHelper", "id" + id + " othercat:" + othercat);
                if (othercat != 0) {
                    dbase.endTransaction();
                    throw new IllegalArgumentException("ID " + id + " already is in cat " + othercat + " while we're adding it to tentative " + mycat);
                }
                setPictureCategory.bindLong(1, mycat);
                setPictureCategory.bindLong(2, id);
                setPictureCategory.executeUpdateDelete();
            }

        }
        dbase.setTransactionSuccessful();
        dbase.endTransaction();
        return mycat;

    }
}
