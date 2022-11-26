package com.college.rssassignment;

import static com.college.rssassignment.R.id;
import static com.college.rssassignment.R.layout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener  {

    MyRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

        // data to populate the RecyclerView with
        ArrayList<String> RSSList = new ArrayList<>();
        RSSList.add("Graham");
        RSSList.add("Henderson");

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(id.rvList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, RSSList);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        EditText text;
        Button button;

        text = findViewById(id.rssText);

        button = findViewById(id.button);
        button.setOnClickListener(e->{
            try {
                readRSS(text.getText().toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

    }


    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    public String readRSS(String address) throws IOException {
        URL rssURL = new URL(address);
        BufferedReader in = new BufferedReader(new InputStreamReader(rssURL.openStream()));

        in.close();

        return address;
    }

}