package com.college.rssassignment;

import static com.college.rssassignment.R.id;
import static com.college.rssassignment.R.layout;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener  {

    LinearLayout lin;
    Button configButton;
    MyRecyclerViewAdapter adapter;
    URL url;
    String n;
    TextView label;
    TextView headlines;
    ArrayList<String> titles;
    ArrayList<String> links;
    ArrayList<String> descriptions;
    ArrayList<String> images;
    RecyclerView recyclerView;
    EditText inputField;
    Button button;
    JobScheduler jobScheduler;
    JobInfo jobInfo;
    private FirebaseAuth mAuth;
    String selectedText;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        label = findViewById(id.label);
        titles = new ArrayList<>();
        links = new ArrayList<>();
        images = new ArrayList<>();
        descriptions = new ArrayList<>();
        headlines = findViewById(id.textView2);
        spinner = findViewById(R.id.spinnermain);
        lin = findViewById(id.linLayout);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.selection, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter2);

        // set up the RecyclerView
        recyclerView = findViewById(id.rvList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // URL INPUT Field
        inputField = findViewById(id.rssText);

        //Setting up jon scheduler.
        jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobInfo = new JobInfo.Builder(11, new ComponentName(this, MyJobService.class))
                // only add if network access is required
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(50000)
                .build();




        // configButton to bring to config activity.
        configButton = findViewById(id.config);
        configButton.setOnClickListener(e->{
            jobScheduler.schedule(jobInfo);
            Intent intent = new Intent(ListActivity.this, ListActivity.class);
            startActivity(intent);
        });

        Intent configIntent = getIntent();
        String configUrl = configIntent.getStringExtra("url");
        if (configUrl != null) {

            URL config = null;
            try {
                config = new URL(configUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            url = config;
            new BackgroundTask().execute();
        }

        button = findViewById(id.button);
        button.setOnClickListener(e->{
            new BackgroundTask().execute();
            // Send the data to firebase.
            sendToFB();

            // backup fileshare to database
            adapter = new MyRecyclerViewAdapter(this, titles, descriptions, images);
            adapter.setClickListener(this);
            recyclerView.setAdapter(adapter);

            // HIDE FIELDS ON CLICK.
            inputField.setVisibility(View.GONE);
            label.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);
            headlines.setVisibility(View.GONE);
            button.setVisibility(View.GONE);
            lin.setVisibility(View.GONE);
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        // Get position of click and load webpage with intent and retrieved link.
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
                //clear arraylist before reloading data.
                titles.removeAll(titles);
                links.removeAll(links);
                descriptions.removeAll(descriptions);
                images.removeAll(images);


                // get ammount of rows selected by user.
                String urlString = inputField.getText().toString();
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
                        else if (xpp.getName().equalsIgnoreCase("media:content")) {
                            if (insideItem){
                                images.add(xpp.getAttributeValue(null, "url"));
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("enclosure")) {
                            if (insideItem){
                                images.add(xpp.getAttributeValue(null,"url"));
                            }
                        }
                    }

                    // if the tag is an end tag and the name is item reset inside item to false
                    else if(eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")){
                        insideItem = false;
                    }
                    eventType = xpp.next();
                }


                selectedText = spinner.getSelectedItem().toString();
                int amountInt = Integer.parseInt(selectedText);

                titles = adjustArray(titles, amountInt);
                descriptions = adjustArray(descriptions, amountInt);
                links = adjustArray(links, amountInt);
            }
            catch (NullPointerException e){
                return null;
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            Log.d("INFO", images.get(1));
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
        }
    }

    public void sendToFB() {
        for (int i = 0; i < titles.size(); i++) {
            NewsItem newsItem = new NewsItem(titles.get(i), descriptions.get(i),images.get(i));
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference dbRef = database.getReference();
                Log.d("User uid:", user.getUid());
                dbRef.child("users").child(user.getUid()).push().setValue(newsItem);
                Toast.makeText(getApplicationContext(),"Data Sent to Firebase Database",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(),"Data Sent to Firebase Database",Toast.LENGTH_LONG).show();
                Log.d("InfoMsg:","not sent");
            }
        }
    }

    public ArrayList<String> adjustArray(ArrayList <String> list, int n){
        while (list.size() != n){
            list.remove(0);
        }
        return list;
    }
}