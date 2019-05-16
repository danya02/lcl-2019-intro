package ru.danya02.imagematcher;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Adapter;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        emptyLabel = findViewById(R.id.empty_list_label);
        recyclerView = findViewById(R.id.rv_category_view);

        recyclerView.setAdapter((RecyclerView.Adapter) categoryAdapter);

        databaseHelper = new DatabaseHelper(this);
        databaseHelper.instantiateDatabase();
        FloatingActionButton fab = findViewById(R.id.fab_add_to_index);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    DatabaseHelper databaseHelper;

    ArrayList<ArrayList<String>> data;

    private View emptyLabel;
    private RecyclerView recyclerView;
    private HashMap<Integer, Adapter> individualCategoryAdapters;
    private CategoryAdapter categoryAdapter = new CategoryAdapter(databaseHelper);
    void updateObjectVisibility(){
        if (categoryAdapter.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyLabel.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyLabel.setVisibility(View.GONE);
        }
    }

}
