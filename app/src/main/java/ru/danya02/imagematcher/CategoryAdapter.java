package ru.danya02.imagematcher;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryViewHolder> {
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View picturesListView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_category_item, viewGroup, false);
        CategoryViewHolder catHolder = new CategoryViewHolder(picturesListView);
        catHolder.rebindTo(i, dbhelper);
        return catHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder categoryViewHolder, int i) {
        categoryViewHolder.rebindTo(i, dbhelper);
    }

    @Override
    public int getItemCount() {
        return dbhelper.getCategoryCount();
    }

    private DatabaseHelper dbhelper;

    public CategoryAdapter(DatabaseHelper databaseHelper){
        dbhelper = databaseHelper;
    }

    public boolean isEmpty() {
        return dbhelper.getCategoryCount() == 0; // TODO: it is certainly empty if there are no pictures, but it doesn't have to be empty if there are no categories
    }
}
