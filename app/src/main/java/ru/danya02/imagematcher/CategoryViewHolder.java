package ru.danya02.imagematcher;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class CategoryViewHolder extends RecyclerView.ViewHolder {

    private RecyclerView pictureRecyclerView;

    public CategoryViewHolder(@NonNull View itemView) {
        super(itemView);
        pictureRecyclerView = itemView.findViewById(R.id.rv_pictures_in_category);
        pictureRecyclerView.setAdapter(new PictureAdapter(myCat, myHelper));
    }

    private int myCat;
    private DatabaseHelper myHelper;


    public void rebindTo(int category, DatabaseHelper helper) {
        myCat = category;
        myHelper = helper;
        if (pictureRecyclerView.getAdapter() != null) {
            ((PictureAdapter) pictureRecyclerView.getAdapter()).rebind(category, helper);
        }
    }

    public int getMyCat() {
        return myCat;
    }

    public PictureAdapter getMyAdapter() {
        return (PictureAdapter) pictureRecyclerView.getAdapter();
    }
}
