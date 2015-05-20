package com.thiagowittmann.attend;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thiago on 5/20/15.
 */
public class MainActivity extends ActionBarActivity {

    public static final String PREFS_NAME = "AttendFile";

    public static final String DEF_IP = "192.168.0.100";
    private static String url_all_palestras = "/android_connect/getallpalestras.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PALESTRAS = "palestras";
    private static final String TAG_ID = "id";
    private static final String TAG_TITULO = "titulo";
    private static final String TAG_PALESTRANTE = "palestrante";

    private ListView talks_listview;
    private TextView mainAlert;
    private boolean conErr = false;
    private JSONParser jParser;
    private JSONArray talksJson = null;
    private List<Talk> talksList = new ArrayList<Talk>();
    private ProgressDialog pDialog;
    private String serverIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.defaultColor)));

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        serverIP = settings.getString("serverIP", DEF_IP);
    }

}
