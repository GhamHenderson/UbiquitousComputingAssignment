package com.college.rssassignment;

import static com.college.rssassignment.R.id;
import static com.college.rssassignment.R.layout;

import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener  {

    Button configButton;
    MyRecyclerViewAdapter adapter;
    URL url;
    TextView label;
    ArrayList<String> titles;
    ArrayList<String> links;
    ArrayList<String> descriptions;
    ArrayList<String> images;
    RecyclerView recyclerView;
    EditText text;
    Button button;
    JobScheduler jobScheduler;
    JobInfo jobInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

        label = findViewById(id.label);
        titles = new ArrayList<>();
        links = new ArrayList<>();
        images = new ArrayList<>();
        descriptions = new ArrayList<>();

        // set up the RecyclerView
        recyclerView = findViewById(id.rvList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        text = findViewById(id.rssText);
        Intent configintent = getIntent();
        String url = configintent.getStringExtra("url");
        String amount = configintent.getStringExtra("amount");

        if (url != null){
            text.setText(url);
        }

        jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        /*
         Here we can set the job criteria such as will it require network access,
         does it depend on whether the phone is charging or idle,
         should it be run periodically, or be persisted across reboots.
         */
        jobInfo = new JobInfo.Builder(11, new ComponentName(this, MyJobService.class))
                // only add if network access is required
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(50000)
                .build();


        configButton = findViewById(id.config);
        configButton.setOnClickListener(e->{
            jobScheduler.schedule(jobInfo);
            Intent intent = new Intent(ListActivity.this, ConfigActivity.class);
            startActivity(intent);
        });
        button = findViewById(id.button);
        button.setOnClickListener(e->{
            new BackgroundTask().execute();
            // backup fileshere to database.
            
            adapter = new MyRecyclerViewAdapter(this, titles, descriptions, images);
            adapter.setClickListener(this);
            recyclerView.setAdapter(adapter);
            text.setVisibility(View.GONE);
            label.setVisibility(View.GONE);
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Uri uri = Uri.parse(links.get(position));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public InputStream getInputStream(URL url){
        try {
            return url.openConnection().getInputStream();
        }
        catch (IOException e){
            return null;
        }
    }

    public class BackgroundTask extends AsyncTask<Integer,Void,String>{

        ProgressDialog progressDialog = new ProgressDialog(ListActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading Rss Feed");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Integer... params) {
            try {
                String urlString = text.getText().toString();
                url = new URL(urlString);
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(getInputStream(url), "UTF_8");
                boolean insideItem = false;
                int eventType = xpp.getEventType();

                // While its not the end of document
                while (eventType != XmlPullParser.END_DOCUMENT){
                    // if tag is equal to a start tag.
                    if(eventType == XmlPullParser.START_TAG){
                        // if the tag is an item tag set inside item to true.
                        if(xpp.getName().equalsIgnoreCase("item")){
                            insideItem = true;
                        }
                        // if the tag is a title tag
                        else if (xpp.getName().equalsIgnoreCase("title")) {
                            if (insideItem){
                                titles.add(xpp.nextText());
                            }
                        }
                        // if the tag is a link tag
                        else if (xpp.getName().equalsIgnoreCase("link")) {
                            if (insideItem){
                                links.add(xpp.nextText());
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("description")) {
                            if (insideItem){
                                descriptions.add(xpp.nextText());
                            }
                        }
//                        else if (xpp.getName().equalsIgnoreCase("media:content")) {
//                            if (insideItem){
//                                images.add(xpp.nextText());
//                            }
//                        }
//                        else if (xpp.getName().equalsIgnoreCase("enclosure")) {
//                            if (insideItem){
//                                images.add(xpp.nextText());
//                            }
//                        }
                    }

                    // if the tag is an end tag and the name is item reset inside item to false
                    else if(eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")){
                        insideItem = false;
                    }
                    eventType = xpp.next();
                }
            }
            catch (NullPointerException e){
                return null;
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
        }
    }

}