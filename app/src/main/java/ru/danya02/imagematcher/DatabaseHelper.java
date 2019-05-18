package ru.danya02.imagematcher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class DatabaseHelper {
    private String TAG = "dbHelper";
    private Context context;
    private CategoryAdapter catAdapter;
    private Map<Long, PictureAdapter> picAdapters;
    private Runnable visibilityUpdater;
    private AdapterUpdaterRunnable updaterRunnable;

    DatabaseHelper(Context context, @NonNull CategoryAdapter catAdapter, @NonNull Map<Long, PictureAdapter> picAdapters, @NonNull Runnable visibilityUpdater, AdapterUpdaterRunnable adapterUpdaterRunnable) {
        this.context = context;
        this.catAdapter = catAdapter;
        this.picAdapters = picAdapters;
        this.visibilityUpdater = visibilityUpdater;
        updaterRunnable = adapterUpdaterRunnable;
    }

    private SQLiteDatabase dbase;

    void instantiateDatabase() {
        Log.i(TAG, "instantiateDatabase()");
        dbase = context.openOrCreateDatabase("pictureDb.sqlite", SQLiteDatabase.OPEN_READWRITE, null, new DatabaseErrorHandler() {
            @Override
            public void onCorruption(SQLiteDatabase dbObj) {
                Log.wtf(TAG, "Database corruption handler invoked?!");
            }
        });
 //       dbase.execSQL(context.getString(R.string.pragma_enable_foreignkey));
        dbase.execSQL("create table if not exists category (id integer primary key autoincrement, random integer);");
        dbase.execSQL("create table if not exists picture (id integer primary key autoincrement, name string unique, mycategory integer, foreign key(mycategory) references category(id) );");
        dbase.execSQL("create table if not exists hashes (id integer unique primary key autoincrement, picture integer, hash biginteger, foreign key(picture) references picture(id));");
        createPicStatement = dbase.compileStatement(context.getString(R.string.prepared_statement_insert_picture));

        getPictureCategory = dbase.compileStatement("select mycategory from picture where id=?;");
        setPictureCategory = dbase.compileStatement("update picture set mycategory=? where id=?;");
        getPictureByName = dbase.compileStatement("select id from picture where name=?;");
    }

    public void disconnectDatabase() {
        Log.i(TAG, "disconnectDatabase()");
        dbase.close();

    }

    public int getCategoryCount() {
        Cursor cursor = dbase.rawQuery("select * from category", null);
        int count = cursor.getCount();
        cursor.close();
        Log.i(TAG, String.format("getCategoryCount() -> %d", count));
        return count;
    }

    private SQLiteStatement createPicStatement;

    public long createPicture(String name) {
        Log.i(TAG, "createPicture("+name+")");
        getPictureByName.bindString(1,name);
        boolean exists = false;
        long picid = -1;
        try {
            Log.d(TAG, "trying to query for this name...");
            picid = getPictureByName.simpleQueryForLong();
            exists = true;
            Log.i(TAG, "this picture exists in the database");
        } catch (SQLiteDoneException e){
            exists = false;
            Log.i(TAG, "picture "+name+" does not yet exist in the index", e);
        }
        if (!exists){
        createPicStatement.bindString(1, name);
        long id = createPicStatement.executeInsert();
        dbase.execSQL("insert into hashes (picture) values (" + id + ");");
        return id;}
        else {return picid;}
    }

    public void setPictureCategory(long picid, long catid) {
        long lastcat = getPictureCategoryStatement(picid);
        setPictureCategory.bindLong(1, catid);
        setPictureCategory.bindLong(2, picid);
        setPictureCategory.executeUpdateDelete();
        updaterRunnable.updateCatAdapter(catAdapter);
        PictureAdapter olda = picAdapters.get(lastcat);
        PictureAdapter newa = picAdapters.get(catid);
        if (olda != null) {
            updaterRunnable.updatePicAdapter(olda);
        }
        if (newa != null) {
            updaterRunnable.updatePicAdapter(newa);
        }
    }

    public void setPictureHash(long picid, long hash) {
        dbase.execSQL("update hashes set hash=" + hash + " where picture=" + picid + ";");
    }

    public long getPictureHash(long picid) {
        Cursor cursor = dbase.rawQuery("select hash from hashes where picture=" + picid + ";", null);
        cursor.moveToFirst();
        if (cursor.getCount()==0){cursor.close();return Long.MAX_VALUE;}
        long outp = cursor.getLong(cursor.getColumnIndex("hash"));
        cursor.close();
        visibilityUpdater.run();
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
        visibilityUpdater.run();
        return outp;

    }

    private SQLiteStatement getPictureCategory;
    private SQLiteStatement setPictureCategory;

    public long getPictureCategoryStatement(long picid) {
        Cursor cursor = dbase.rawQuery("select mycategory from picture where id=" + picid + ";", null);
        cursor.moveToFirst();
        long outp = cursor.getLong(cursor.getColumnIndex("mycategory"));
        cursor.close();
        visibilityUpdater.run();
        return outp;
    }

    public long createCategory(long[] ids) {
        Log.i("dbaseHelper", "createCategory(...)");
        dbase.beginTransaction();
        ContentValues values = new ContentValues();
        values.put("random",0);
        long mycat = dbase.insert("category","random", values);
        Log.d("dbaseHelper", "mycat: " + mycat);
        if (ids != null) {
            for (long id : ids) {

                {
                    getPictureCategory.bindLong(1, id);
                    long othercat = getPictureCategory.simpleQueryForLong();
                    Log.d(TAG, "id" + id + " othercat:" + othercat);
/*                    if (othercat != 0) {
                        dbase.endTransaction();
                        throw new IllegalArgumentException("ID " + id + " already is in cat " + othercat + " while we're adding it to tentative " + mycat);
                    }
*/
                    setPictureCategory.bindLong(1, mycat);
                    setPictureCategory.bindLong(2, id);
                    setPictureCategory.executeUpdateDelete();
                }
            }
        }
        dbase.setTransactionSuccessful();
        dbase.endTransaction();
        updaterRunnable.updateCatAdapter(catAdapter);
        visibilityUpdater.run();
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
        visibilityUpdater.run();
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
        Log.i(TAG, "getPictureInCategory("+cat+", "+index+")");
        Cursor catcursor = dbase.rawQuery("select name from picture where mycategory=" + cat + " order by name;", null);
        catcursor.move(index);
        String outp = catcursor.getString(catcursor.getColumnIndex("name"));
        catcursor.close();
        Log.i(TAG, "getPictureInCategory("+cat+", "+index+") -> "+outp);
        return outp;
    }

    public int getPictureCountByCategory(int cat) {
        Cursor catcursor = dbase.rawQuery("select name from picture where mycategory=" + cat + " order by name;", null);
        int count = catcursor.getCount();
        catcursor.close();
        Log.i(TAG, "getPictureCountByCategory("+cat+") -> "+count);
        return count;
    }

    public String getPicturePath(long picid){
        Cursor piccursor = dbase.rawQuery("select name from picture where id="+picid+";", null);
        if (piccursor.getCount()==0){return null;}
        piccursor.moveToFirst();
        String path = piccursor.getString(piccursor.getColumnIndex("name"));
        piccursor.close();
        Log.i(TAG, "getPicturePath("+picid+") -> "+path);
        return path;
    }
    public void startAnalysis(long picid){
        AnalysisTaskInput data = new AnalysisTaskInput();
        data.picid = picid;
        data.dbhelper = this;
        AnalysisTask task = new AnalysisTask();
        task.execute(data);
    }

    public void doAnalysisSynchronous(long picid){
        AnalysisTaskInput data = new AnalysisTaskInput();
        data.picid = picid;
        data.dbhelper = this;
        AnalysisTask task = new AnalysisTask();
        task.doInBackground(data);
    }

    SQLiteStatement getPictureByName;

    public void setCategoryAdapter(CategoryAdapter categoryAdapter) {
        catAdapter = categoryAdapter;
    }
}