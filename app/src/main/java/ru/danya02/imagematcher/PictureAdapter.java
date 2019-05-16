package ru.danya02.imagematcher;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PictureAdapter extends RecyclerView.Adapter<PictureHolder> {
    @NonNull
    @Override
    public PictureHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View pictureView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.picture_item, viewGroup, false);
        PictureHolder picHolder = new PictureHolder(pictureView);
        picHolder.setPicture(getPicName(i));
        return picHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PictureHolder viewHolder, int i) {
        viewHolder.setPicture(getPicName(i));

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private int myCat;
    private DatabaseHelper dbhelper;

    private String getPicName(int i){
        return dbhelper.getPictureInCategory(myCat, i);
    }

    public PictureAdapter(int cat, DatabaseHelper databaseHelper){
        rebind(cat, databaseHelper);
    }

    public void rebind(int cat, DatabaseHelper databaseHelper){
        myCat = cat;
        dbhelper = databaseHelper;

    }
}
