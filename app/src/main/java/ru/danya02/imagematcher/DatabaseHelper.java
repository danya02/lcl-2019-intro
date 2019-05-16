package ru.danya02.imagematcher;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DatabaseHelper {
    private Context context;
    DatabaseHelper(Context context){
        this.context = context;
    }

    private SQLiteDatabase dbase;
    void instantiateDatabase(){
        Log.i("dbHelper", "instantiateDatabase()");
        dbase = context.openOrCreateDatabase("pictureDb.sqlite", SQLiteDatabase.OPEN_READWRITE,  null, new DatabaseErrorHandler(){
            @Override
            public void onCorruption(SQLiteDatabase dbObj) {
                Log.wtf("dbHelper", "Database corruption handler invoked?!");
            }
        });
        dbase.execSQL("pragma foreign_keys = on;");
        dbase.execSQL("create table if not exists category (id integer primary key autoincrement);");
        dbase.execSQL("create table if not exists picture (id integer primary key autoincrement, name string unique, mycategory integer, foreign key(mycategory) references category(id) );");
        createPicStatement = dbase.compileStatement(context.getString(R.string.prepared_statement_insert_picture));
    }

    public void disconnectDatabase(){
        Log.i("dbHelper", "disconnectDatabase()");
        dbase.close();

    }

    public int getCategoryCount(){
        Cursor cursor = dbase.rawQuery("SELECT * FROM category", null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    private SQLiteStatement createPicStatement;
    public void createPicture(String name){
        createPicStatement.bindString(1, name);
        createPicStatement.executeInsert();
    }


}
