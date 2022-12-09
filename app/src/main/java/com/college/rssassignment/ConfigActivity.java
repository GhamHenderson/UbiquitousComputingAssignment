package com.college.rssassignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class ConfigActivity extends AppCompatActivity {

    EditText text;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        text = findViewById(R.id.rssText);

        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter= ArrayAdapter.createFromResource(this, R.array.selection, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);

        String selectedText = spinner.getSelectedItem().toString();

        button = findViewById(R.id.button);

        button.setOnClickListener(e ->{
            String urlString = text.getText().toString();
            Intent intent = new Intent(ConfigActivity.this,ListActivity.class);
            intent.putExtra("url" , urlString);
            intent.putExtra("amount" , selectedText);
            startActivity(intent);
        });
    }
}