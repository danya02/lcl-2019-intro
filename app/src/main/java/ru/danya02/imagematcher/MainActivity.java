package ru.danya02.imagematcher;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Adapter;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends FragmentActivity implements DirectoryChooserFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        emptyLabel = findViewById(R.id.empty_list_label);
        recyclerView = findViewById(R.id.rv_category_view);


        pictureAdapters = new HashMap<>();
        databaseHelper = new DatabaseHelper(this, categoryAdapter, pictureAdapters, new Runnable() {
            @Override
            public void run() {
                updateObjectVisibility();
            }
        }, new AdapterUpdaterRunnable(this)
        );
        databaseHelper.instantiateDatabase();

        categoryAdapter = new CategoryAdapter(databaseHelper, pictureAdapters, this);
        databaseHelper.setCategoryAdapter(categoryAdapter);
        recyclerView.setAdapter(categoryAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final DirectoryChooserConfig config = DirectoryChooserConfig.builder().initialDirectory(Environment.getExternalStorageDirectory().toString()).newDirectoryName("test").build();

        mDialog = DirectoryChooserFragment.newInstance(config);
        FloatingActionButton fab = findViewById(R.id.fab_add_to_index);
        fab.setOnClickListener(view -> mDialog.show(getFragmentManager(), null));
    }

    @Override
    protected void onDestroy() {
        databaseHelper.disconnectDatabase();
        super.onDestroy();
    }

    HashMap<Long, PictureAdapter> pictureAdapters;

    DatabaseHelper databaseHelper;

    ArrayList<ArrayList<String>> data;

    private View emptyLabel;
    private RecyclerView recyclerView;
    private HashMap<Integer, Adapter> individualCategoryAdapters;
    private CategoryAdapter categoryAdapter;

    void updateObjectVisibility() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (categoryAdapter.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyLabel.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyLabel.setVisibility(View.GONE);
                }
            }
        });
    }


    private DirectoryChooserFragment mDialog;

    @Override
    public void onSelectDirectory(@NonNull String path) {
        Snackbar.make(findViewById(R.id.mainLayout), path, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        AddPicturesData conf = new AddPicturesData();
        conf.databaseHelper = databaseHelper;
        conf.targetFolder = new File(path);
        AddPicturesTask foo = new AddPicturesTask();
        foo.execute(conf);
        mDialog.dismiss();
    }

    @Override
    public void onCancelChooser() {
        mDialog.dismiss();

    }
}
