package ru.danya02.imagematcher;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryViewHolder> {
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View picturesListView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_category_item, viewGroup, false);
        CategoryViewHolder catHolder = new CategoryViewHolder(picturesListView, myContext);
        catHolder.rebindTo(i, dbhelper);
        pictureAdapters.put((long) i, catHolder.getMyAdapter());
        return catHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder catHolder, int i) {
        pictureAdapters.remove((long) catHolder.getMyCat());
        catHolder.rebindTo(i, dbhelper);
        pictureAdapters.put((long) i, catHolder.getMyAdapter());
    }

    @Override
    public int getItemCount() {
        return dbhelper.getCategoryCount();
    }

    private DatabaseHelper dbhelper;
    private Map<Long, PictureAdapter> pictureAdapters;
    private Context myContext;

    public CategoryAdapter(DatabaseHelper databaseHelper, Map<Long, PictureAdapter>pictureAdapter, Context context){
        dbhelper = databaseHelper;
        pictureAdapters = pictureAdapter;
        myContext = context;
    }

    public boolean isEmpty() {
        return dbhelper.getCategoryCount() == 0; // TODO: it is certainly empty if there are no pictures, but it doesn't have to be empty if there are no categories
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView){
        Log.w("catAdapter", "RecyclerView attached!!");
    }
}
