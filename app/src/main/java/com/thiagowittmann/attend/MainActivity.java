package com.thiagowittmann.attend;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thiago on 5/20/15.
 */
public class MainActivity extends ActionBarActivity {

    public static final String PREFS_NAME = "AttendFile";

    public static final String DEF_IP = "192.168.0.100";
    private static String url_all_talks = "/android_connect/getallpalestras.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_TALKS = "palestras";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "titulo";
    private static final String TAG_SPEAKER = "palestrante";

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

        talks_listview = (ListView) findViewById(R.id.talks_listview);
        new LoadTalks().execute();
    }

    class LoadTalks extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mainAlert = (TextView) findViewById(R.id.mainAlert);
            mainAlert.setText(getResources().getString(R.string.loading));
            mainAlert.setVisibility(View.VISIBLE);
        }


        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            jParser = null;
            jParser = new JSONParser();
            JSONObject json = null;

            json = jParser.makeHttpRequest("http://" + serverIP + url_all_talks, "GET", params);
            if(jParser.getError()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainAlert.setText(getResources().getString(R.string.con_error));
                        mainAlert.setVisibility(View.VISIBLE);
                    }
                });
                conErr = true;
                return null;
            }
            conErr = false;

            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    talksJson = json.getJSONArray(TAG_TALKS);

                    for (int i = 0; i < talksJson.length(); i++) {
                        JSONObject c = talksJson.getJSONObject(i);

                        String id = c.getString(TAG_ID);
                        String name = c.getString(TAG_NAME);
                        String speaker = c.getString(TAG_SPEAKER);

                        talksList.add(new Talk(id, name, speaker));
                    }

                } else {
                    Intent i = new Intent(getApplicationContext(),
                            MainActivity.class);

                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }


        protected void onPostExecute(String file_url) {
            if(talksList.size() == 0 && !conErr){
                mainAlert.setText(getResources().getString(R.string.no_talks));
                mainAlert.setVisibility(View.VISIBLE);

            } else if(!conErr) {
                mainAlert.setVisibility(View.INVISIBLE);
                runOnUiThread(new Runnable() {
                    public void run() {
                        final ArrayAdapter<Talk> arrayAdapter = new ArrayAdapter<Talk>(
                                MainActivity.this,
                                android.R.layout.simple_list_item_1,
                                talksList);

                        talks_listview.setAdapter(arrayAdapter);
                        talks_listview.setClickable(true);
                        talks_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View item,
                                                    final int position, long id) {
                                /*Palestra palestra = arrayAdapter.getItem(position);
                                Intent intent = new Intent(MainActivity.this, PalestraActivity.class);
                                intent.putExtra("palestra.id", palestra.getId());
                                intent.putExtra("palestra.name", palestra.getName());
                                intent.putExtra("palestra.speaker", palestra.getSpeaker());
                                startActivity(intent);*/
                            }
                        });
                    }
                });
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.refresh:
                talksList.clear();
                finish();
                startActivity(getIntent());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
