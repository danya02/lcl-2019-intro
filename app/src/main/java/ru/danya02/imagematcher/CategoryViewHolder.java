package ru.danya02.imagematcher;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class CategoryViewHolder extends RecyclerView.ViewHolder {

    private RecyclerView pictureRecyclerView;
    private Context myContext;

    public CategoryViewHolder(@NonNull View itemView, @NonNull Context context) {
        super(itemView);
        myContext = context;
        pictureRecyclerView = itemView.findViewById(R.id.rv_pictures_in_category);
        pictureRecyclerView.setAdapter(new PictureAdapter(myCat, myHelper));
        pictureRecyclerView.setLayoutManager(new LinearLayoutManager(myContext));
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
