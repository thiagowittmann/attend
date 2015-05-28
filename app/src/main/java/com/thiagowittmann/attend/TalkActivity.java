package com.thiagowittmann.attend;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by thiago on 5/20/15.
 */
public class TalkActivity extends ActionBarActivity {

    public static final String PREFS_NAME = "AttendFile";
    private String DEF_ADDRESS;

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private String url_talk_viewers;
    private String url_set_attendance;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_VIEWERS = "pessoas";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "nome";
    private static final String TAG_QRID = "qrid";
    private static final String TAG_ATTENDANCE = "presente";

    private ListView viewers_listview;
    private TextView talkAlert;
    private boolean conErr = false;
    private boolean attendErr = false;
    private int attendSuccess = 0;
    private String talkName;
    private String talkSpeaker;
    private String talkId;
    private ArrayAdapter<Viewer> arrayAdapter;
    private JSONParser jParser;
    private JSONArray viewersJson = null;
    private List<Viewer> viewersList = new ArrayList<Viewer>();
    private ProgressDialog pDialog;
    private String serverIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

        // Getting connection preferences
        DEF_ADDRESS = getResources().getString(R.string.default_address);
        url_talk_viewers = getResources().getString(R.string.url_talk_viewers);
        url_set_attendance = getResources().getString(R.string.url_set_attendance);

        // Getting stored server address
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        serverIP = settings.getString("serverIP", DEF_ADDRESS);

        Intent incomingIntent = getIntent();

        // Getting message from previous intent
        talkId = incomingIntent.getStringExtra("talk.id");
        talkName = incomingIntent.getStringExtra("talk.name");
        talkSpeaker = incomingIntent.getStringExtra("talk.speaker");

        setTitle(talkSpeaker + ": " + talkName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.defaultColor)));

        viewers_listview = (ListView) findViewById(R.id.viewers_listview);

        new LoadViewers().execute();
    }


    // Opens external app used to scan QR codes
    public void startQR(View view){

        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            showDialog(this, getResources().getString(R.string.qrreader_not_found), getResources().getString(R.string.would_you_like_to_download), getResources().getString(R.string.yes), getResources().getString(R.string.no)).show();
        }
    }


    // Returns QR code from external app
    public void onActivityResult(int requestCode, int resultCode, Intent intent)    {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                boolean found = false;
                String contents = intent.getStringExtra("SCAN_RESULT");
                for (int i = 0; i < arrayAdapter.getCount(); i++) {
                    if(arrayAdapter.getItem(i).getQRID().equals(contents)) {
                        found = true;
                        if(!viewers_listview.isItemChecked(i)){
                            checkAttendance(i);
                        } else {
                            Toast toast = Toast.makeText(this, getResources().getString(R.string.attendance_already_confirmed) + " " + arrayAdapter.getItem(i).getName(), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        break;
                    }
                }
                if(!found) {
                    Toast toast = Toast.makeText(this, getResources().getString(R.string.id_not_found), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    }


    public void raffle(View view) {
        AlertDialog.Builder raffle = new AlertDialog.Builder(TalkActivity.this);
        raffle.setTitle(getResources().getString(R.string.raffle));
        raffle.setMessage(" ");
        raffle.setPositiveButton(getResources().getString(R.string.raffle_action), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Random myRandomizer = new Random();
                SparseBooleanArray attendants = viewers_listview.getCheckedItemPositions();
                AlertDialog.Builder lucky = new AlertDialog.Builder(TalkActivity.this);
                lucky.setTitle(":)");
                if(attendants.size() == 0) {
                    lucky.setMessage(getResources().getString(R.string.no_viewers));
                } else {
                    int sorteado = attendants.keyAt(myRandomizer.nextInt(attendants.size()));
                    lucky.setMessage(arrayAdapter.getItem(sorteado).getName());
                }
                lucky.setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i){
                    }
                });
                lucky.show();
            }
        });
        raffle.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i){
            }
        });
        raffle.show();
    }


    public void refresh(View view) {
        viewersList.clear();
        finish();
        startActivity(getIntent());
    }


    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    public void checkAttendance(int position) {
        new setAttendance(arrayAdapter.getItem(position).getID(), talkId, "1", position).execute();
    }

    public void uncheckAttendance(int position) {
        new setAttendance(arrayAdapter.getItem(position).getID(), talkId, "0", position).execute();
    }


    // Async task used to load all viewers from a given talk
    class LoadViewers extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            // Creating a loading message
            super.onPreExecute();
            talkAlert = (TextView) findViewById(R.id.talkAlert);
            talkAlert.setText(getResources().getString(R.string.loading));
            talkAlert.setVisibility(View.VISIBLE);
        }

        // HTTP request and JSON verification
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("palestra_id", talkId));
            // getting JSON string from URL
            jParser = null;
            jParser = new JSONParser();
            JSONObject json;

            json = jParser.makeHttpRequest("http://" + serverIP + url_talk_viewers, "GET", params);
            if(jParser.getError()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        talkAlert.setText(getResources().getString(R.string.con_error));
                        talkAlert.setVisibility(View.VISIBLE);
                    }
                });
                conErr = true;
                return null;
            }

            conErr = false;
            // Check your log cat for JSON response
            Log.d("Viewers: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    viewersJson = json.getJSONArray(TAG_VIEWERS);

                    for (int i = 0; i < viewersJson.length(); i++) {
                        JSONObject c = viewersJson.getJSONObject(i);

                        String id = c.getString(TAG_ID);
                        String name = c.getString(TAG_NAME);
                        String qrid = c.getString(TAG_QRID);
                        String attendance = c.getString(TAG_ATTENDANCE);

                        viewersList.add(new Viewer(id, name, qrid, attendance));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {

            if(viewersList.size() == 0 && !conErr){
                talkAlert.setText(getResources().getString(R.string.no_viewers));
                talkAlert.setVisibility(View.VISIBLE);

            } else if(!conErr) {
                talkAlert.setVisibility(View.INVISIBLE);
                runOnUiThread(new Runnable() {
                    public void run() {
                        arrayAdapter = new ArrayAdapter<Viewer>(
                                TalkActivity.this,
                                android.R.layout.simple_list_item_multiple_choice,
                                viewersList);

                        viewers_listview.setAdapter(arrayAdapter);
                        viewers_listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                        viewers_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View item,
                                                    final int position, long id) {

                                if (viewers_listview.isItemChecked(position)) {
                                    AlertDialog.Builder confirm = new AlertDialog.Builder(TalkActivity.this);
                                    confirm.setTitle(getResources().getString(R.string.confirm_attendance));
                                    confirm.setMessage(getResources().getString(R.string.confirm_attendance1) + " " + arrayAdapter.getItem(position).getName() + getResources().getString(R.string.confirm_attendance2));
                                    confirm.setCancelable(false);
                                    confirm.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            checkAttendance(position);
                                        }
                                    });
                                    confirm.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            viewers_listview.setItemChecked(position, false);
                                        }
                                    });
                                    confirm.show();
                                } else {
                                    AlertDialog.Builder disconfirm = new AlertDialog.Builder(TalkActivity.this);
                                    disconfirm.setTitle(getResources().getString(R.string.disconfirm_attendance));
                                    disconfirm.setMessage(getResources().getString(R.string.disconfirm_attendance1) + " " + arrayAdapter.getItem(position).getName() + getResources().getString(R.string.disconfirm_attendance2));
                                    disconfirm.setCancelable(false);
                                    disconfirm.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            uncheckAttendance(position);
                                        }
                                    });
                                    disconfirm.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            viewers_listview.setItemChecked(position, true);
                                        }
                                    });
                                    disconfirm.show();
                                }
                            }
                        });

                        for (int i = 0; i < arrayAdapter.getCount(); i++) {
                            if (arrayAdapter.getItem(i).getAttendance().equals("1")) {
                                viewers_listview.setItemChecked(i, true);
                            }
                        }
                    }
                });

            }

        }

    }

    // Async task used to confirm attendance
    class setAttendance extends AsyncTask<String, String, String> {

        private String id_viewer;
        private String id_talk;
        private String attendance;
        private int position;
        public setAttendance(String id_viewer, String id_talk, String attendance, int position){
            this.id_viewer = id_viewer;
            this.id_talk = id_talk;
            this.attendance = attendance;
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            // Creating a loading message
            super.onPreExecute();
            pDialog = new ProgressDialog(TalkActivity.this);
            pDialog.setMessage(getResources().getString(R.string.loading));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id_pessoa", id_viewer));
            params.add(new BasicNameValuePair("id_palestra", id_talk));
            params.add(new BasicNameValuePair("presente", attendance));

            // getting JSON string from URL
            jParser = null;
            jParser = new JSONParser();
            JSONObject json;

            json = jParser.makeHttpRequest("http://" + serverIP + url_set_attendance, "GET", params);
            if(jParser.getError()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(TalkActivity.this);
                        builder.setMessage(getResources().getString(R.string.con_error));
                        builder.setPositiveButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        builder.show();
                    }
                });
                attendErr = true;
                return null;
            }

            attendErr = false;
            attendSuccess = 0;
            try {
                attendSuccess = json.getInt(TAG_SUCCESS);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            if(attendErr) {
                if(attendance.equals("0")){
                    viewers_listview.setItemChecked(position, true);
                } else {
                    viewers_listview.setItemChecked(position, false);
                }
                return;
            } else if(attendSuccess == 0){
                if(attendance.equals("0")){
                    viewers_listview.setItemChecked(position, true);
                } else {
                    viewers_listview.setItemChecked(position, false);
                }
                pDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(TalkActivity.this);
                builder.setMessage(getResources().getString(R.string.attendance_not_updated));
                builder.setPositiveButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.show();
                return;
            }
            if(attendance.equals("0")){
                viewers_listview.setItemChecked(position, false);
                Toast toast = Toast.makeText(TalkActivity.this, getResources().getString(R.string.disconfirmed_attendance) + ": " + arrayAdapter.getItem(position).getName(), Toast.LENGTH_SHORT);
                toast.show();
            } else {
                viewers_listview.setItemChecked(position, true);
                Toast toast = Toast.makeText(TalkActivity.this, getResources().getString(R.string.confirmed_attendance) + ": "  + arrayAdapter.getItem(position).getName(), Toast.LENGTH_SHORT);
                toast.show();
            }
            pDialog.dismiss();
        }

    }

}
