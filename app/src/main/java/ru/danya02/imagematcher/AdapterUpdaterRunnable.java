package ru.danya02.imagematcher;

public class AdapterUpdaterRunnable {
    MainActivity mainActivity;
    public AdapterUpdaterRunnable(MainActivity activity){
        mainActivity = activity;
    }

    public void updateCatAdapter(CategoryAdapter adapter){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
    public void updatePicAdapter(PictureAdapter adapter){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

}
