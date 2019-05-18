package ru.danya02.imagematcher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class AnalysisTask extends AsyncTask<AnalysisTaskInput, String, String> {


    @Override
    protected String doInBackground(AnalysisTaskInput... analysisTaskInputs) {
        long picid = analysisTaskInputs[0].picid;
        DatabaseHelper dbhelper = analysisTaskInputs[0].dbhelper;

        String picpath = dbhelper.getPicturePath(picid);
        Bitmap picture = BitmapFactory.decodeFile(picpath);
        ImagePHash hasher = new ImagePHash();
        long hash = hasher.calcPHash(picture);
        dbhelper.setPictureHash(picid, hash);

        long mindiff = Long.MAX_VALUE;
        long mindiffid = 0;
        for (long otherpicid :
                dbhelper.getAllPictures()) {
            if (otherpicid != picid) {
                long otherhash = dbhelper.getPictureHash(otherpicid);
                long diff = ImagePHash.distance(hash, otherhash);
                if (diff < mindiff) {
                    mindiff = diff;
                    mindiffid = otherpicid;
                }
            }
        }
        if (mindiff > 10) { // the picture is not sufficiently similar
            long[] pics = new long[1];
            pics[0] = picid;
            dbhelper.createCategory(pics);
        } else {
            dbhelper.setPictureCategory(picid, dbhelper.getPictureCategoryStatement(mindiffid));
        }
        return null;
    }
}
