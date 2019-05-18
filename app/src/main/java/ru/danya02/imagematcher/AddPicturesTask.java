package ru.danya02.imagematcher;

import android.os.AsyncTask;

import java.io.File;
import java.util.LinkedList;

public class AddPicturesTask extends AsyncTask<AddPicturesData, String, String> {

    @Override
    protected String doInBackground(AddPicturesData... datas) {
        AddPicturesData data = datas[0];

        File folder = data.targetFolder;
        DatabaseHelper dbhelper = data.databaseHelper;


        File[] myfiles = folder.listFiles((dir, name) -> {
                    name = name.toLowerCase();
                    LinkedList<String> extensions = new LinkedList<>();
                    extensions.add("png");
                    extensions.add("jpg");
                    extensions.add("bmp");
                    extensions.add("gif");
                    for (String extension :
                            extensions) {
                        if (name.endsWith("." + extension)) {
                            return true;
                        }

                    }
                    return false;
                }
        );
        for (File to_add :
                myfiles) {
            long fileid = dbhelper.createPicture(to_add.getAbsolutePath());
//                dbhelper.startAnalysis(fileid);
            dbhelper.doAnalysisSynchronous(fileid);
        }
        return null;
    }
}
