package com.thiagowittmann.attend;

/**
 * Created by thiago on 5/20/15.
 */

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.TextView;

public class SettingsActivity extends ActionBarActivity {

    public static final String PREFS_NAME = "AttendFile";
    private String DEF_ADDRESS;

    private String serverIP;
    private TextView IpField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        setTitle(getResources().getString(R.string.settings));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.defaultColor)));

        DEF_ADDRESS = getResources().getString(R.string.default_address);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        serverIP = settings.getString("serverIP", DEF_ADDRESS);

        IpField = (TextView) findViewById(R.id.serverIpField);
        IpField.setText(serverIP);

        TextView IpText = (TextView) findViewById(R.id.serverIpTextView);
        IpText.setText(getResources().getString(R.string.server_address));
    }

    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("serverIP", IpField.getText().toString());

        editor.commit();
    }
}
