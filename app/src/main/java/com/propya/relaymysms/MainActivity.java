package com.propya.relaymysms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    EditText webHook,keywords;
    Switch getPost, containsKeyword,startKeyWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeView();
        restoreVals();
    }

    private void restoreVals() {
        SharedPreferences appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        getPost.setChecked(appPrefs.getBoolean(Constants.POST_GET, false));
        containsKeyword.setChecked(appPrefs.getBoolean(Constants.CONTAINS_KEYWORD, false));
        startKeyWord.setChecked(appPrefs.getBoolean(Constants.KEYWORD_START, false));

        webHook.setText(appPrefs.getString(Constants.WEB_HOOK_TEXT,""));
        keywords.setText(appPrefs.getString(Constants.KEYWORD_TEXT,""));

    }


    void initializeView(){
        webHook = findViewById(R.id.webhook);
        keywords = findViewById(R.id.keywordText);
        getPost  = findViewById(R.id.triggerPost);
        containsKeyword = findViewById(R.id.triggerOnKey);
        startKeyWord = findViewById(R.id.triggerOnStart);
    }

    public void updatePref(View view) {
        SharedPreferences.Editor appPrefs = getSharedPreferences("AppPrefs",MODE_PRIVATE).edit();
        appPrefs.putBoolean(Constants.POST_GET,getPost.isChecked());
        appPrefs.putBoolean(Constants.CONTAINS_KEYWORD,containsKeyword.isChecked());
        appPrefs.putBoolean(Constants.KEYWORD_START,startKeyWord.isChecked());
        appPrefs.putString(Constants.WEB_HOOK_TEXT,webHook.getText().toString());
        appPrefs.putString(Constants.KEYWORD_TEXT,keywords.getText().toString());
        appPrefs.apply();

    }

    public void stopService(View view) {
        Intent i = new Intent(this,BackgroundService.class);
        i.putExtra("stopMe",true);
        ContextCompat.startForegroundService(this,i);
    }

    public void startService(View view) {
        Intent i = new Intent(this,BackgroundService.class);
        ContextCompat.startForegroundService(this,i);

    }
}