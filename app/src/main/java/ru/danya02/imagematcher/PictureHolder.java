package ru.danya02.imagematcher;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class PictureHolder extends RecyclerView.ViewHolder {
    public PictureHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.picture_item);
    }
    ImageView imageView;
    public void setPicture(String path){
        Picasso.get().load(path).placeholder(R.drawable.image_placeholder).error(R.drawable.ic_launcher_background).into(imageView);
    }
}
