package ru.danya02.imagematcher;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class DatabaseHelper {
    private Context context;
    private CategoryAdapter catAdapter;
    private Map<Long, PictureAdapter> picAdapters;

    DatabaseHelper(Context context, @NonNull CategoryAdapter catAdapter, @NonNull Map<Long, PictureAdapter> picAdapters) {
        this.context = context;
        this.catAdapter = catAdapter;
        this.picAdapters = picAdapters;
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
        dbase.execSQL("create table if not exists hashes (id integer unique primary key autoincrement, picture integer, hash biginteger, foreign key(picture) references picture(id));");
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
        long id = createPicStatement.executeInsert();
        dbase.execSQL("insert into hashes (picture) values (" + id + ")");
        return id;
    }

    public void setPictureCategory(long picid, long catid) {
        long lastcat = getPictureCategoryStatement(picid);
        setPictureCategory.bindLong(1, catid);
        setPictureCategory.bindLong(2, picid);
        setPictureCategory.executeUpdateDelete();
        PictureAdapter olda = picAdapters.get(lastcat);
        PictureAdapter newa = picAdapters.get(catid);
        if (olda != null) {
            olda.notifyDataSetChanged();
        }
        if (newa != null) {
            newa.notifyDataSetChanged();
        }
    }

    public void setPictureHash(long picid, long hash) {
        dbase.execSQL("update hashes set hash=" + hash + " where picture=" + picid + ";");
    }

    public long getPictureHash(long picid) {
        Cursor cursor = dbase.rawQuery("select hash from hashes where picture=" + picid + ";", null);
        cursor.moveToFirst();
        long outp = cursor.getLong(cursor.getColumnIndex("hash"));
        cursor.close();
        return outp;
    }

    public Collection<Long> getAllPictures(){
        Cursor piccursor = dbase.rawQuery("select id from picture;", null);
        ArrayList<Long> outp = new ArrayList<>();
        int idcolumn = piccursor.getColumnIndex("id");
        piccursor.moveToFirst();
        while (!piccursor.isAfterLast()) {
            outp.add(piccursor.getLong(idcolumn));
            piccursor.moveToNext();
        }
        piccursor.close();
        return outp;

    }

    private SQLiteStatement createCategoryStatement;
    private SQLiteStatement getPictureCategory;
    private SQLiteStatement setPictureCategory;

    public long getPictureCategoryStatement(long picid) {
        Cursor cursor = dbase.rawQuery("select mycategory from picture where id=" + picid + ";", null);
        cursor.moveToFirst();
        long outp = cursor.getLong(cursor.getColumnIndex("mycategory"));
        cursor.close();
        return outp;
    }

    public long createCategory(long[] ids) {
        Log.i("dbaseHelper", "createCategory(...)");
        dbase.beginTransaction();
        long mycat = createCategoryStatement.executeInsert();
        Log.d("dbaseHelper", "mycat: " + mycat);
        if (ids != null) {
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
        }
        dbase.setTransactionSuccessful();
        dbase.endTransaction();
        return mycat;

    }

    public ArrayList<Integer> getCategories() {
        Cursor catcursor = dbase.rawQuery("select id from category order by id;", null);
        ArrayList<Integer> outp = new ArrayList<>();
        int idcolumn = catcursor.getColumnIndex("id");
        catcursor.moveToFirst();
        while (!catcursor.isAfterLast()) {
            outp.add(catcursor.getInt(idcolumn));
            catcursor.moveToNext();
        }
        catcursor.close();
        return outp;
    }

    public ArrayList<Integer> getPicturesByCategory(int cat) {
        Cursor catcursor = dbase.rawQuery("select name from picture where mycategory=" + cat + " order by name;", null);
        ArrayList<Integer> outp = new ArrayList<>();
        int idcolumn = catcursor.getColumnIndex("name");
        catcursor.moveToFirst();
        while (!catcursor.isAfterLast()) {
            outp.add(catcursor.getInt(idcolumn));
            catcursor.moveToNext();
        }
        catcursor.close();
        return outp;
    }


    public String getPictureInCategory(int cat, int index) {
        Cursor catcursor = dbase.rawQuery("select name from picture where mycategory=" + cat + " order by name;", null);
        catcursor.move(index);
        String outp = catcursor.getString(catcursor.getColumnIndex("name"));
        catcursor.close();
        return outp;
    }

    public int getPictureCountByCategory(int cat) {
        Cursor catcursor = dbase.rawQuery("select name from picture where mycategory=" + cat + " order by name;", null);
        int count = catcursor.getCount();
        catcursor.close();
        return count;
    }

    public String getPicturePath(long picid){
        Cursor piccursor = dbase.rawQuery("select name from picture where id="+picid+";", null);
        piccursor.moveToFirst();
        String path = piccursor.getString(piccursor.getColumnIndex("name"));
        piccursor.close();
        return path;
    }
    public void startAnalysis(long picid){
        AnalysisTaskInput data = new AnalysisTaskInput();
        data.picid = picid;
        data.dbhelper = this;
        AnalysisTask task = new AnalysisTask();
        task.execute(data);

    }

}