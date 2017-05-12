package com.bpalomino.menutest;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {
    private JSONObject datafile;

    private Socket mSocket;
    {
        try{
            mSocket = IO.socket("http://192.168.0.16:3000");
        } catch (URISyntaxException e) {
            Log.i("Socket", "Invalid URI");
        }
    }

    private Spinner mCollegeSpinner;
    private Spinner mParkingSpinner;

    //arrays for spinner info
    public ArrayList<String> colleges = new ArrayList<>();
    public ArrayList<String> lots = new ArrayList<>();

//    public String[] colleges = {"Cal Poly Pomona", "Cal Poly SLO", "Mt. Sac"};
//    public String[] lots = {"Parking Lot J", "Parking Lot M", "Parking Structure 2"};

    public String selected;

    public float lat;
    public float lng;
    public String parkingLotName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mSocket.on("data", onData);
        mSocket.connect();

        //setup for the two spinners for college and parking lot selection
        mCollegeSpinner = (Spinner) findViewById(R.id.collegeMenu);
        mParkingSpinner = (Spinner) findViewById(R.id.parkinglotMenu);

//        ArrayAdapter<String> collegeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colleges);
//        collegeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mCollegeSpinner.setAdapter(collegeAdapter);

//        ArrayAdapter<String> lotAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lots);
//        lotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mParkingSpinner.setAdapter(lotAdapter);

        mCollegeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "Selected: " + colleges.get(i), Toast.LENGTH_SHORT).show();
                selected = "10001" + Integer.toString(i);
                new GetParkingDataTask().execute(datafile);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mParkingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "Selected: " + lots.get(i), Toast.LENGTH_SHORT).show();
                setCoordinates(lots.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void setCoordinates(String lot) {
        String slat="", slng="";
        try {
            JSONObject subData = datafile.getJSONObject("parking_data");
            for (int i = 2001; i <= 2010; i++){
                if (subData.getJSONObject(Integer.toString(i)).getString("parkinglot_name").equals(lot)) {
                    slat = subData.getJSONObject(Integer.toString(i)).getString("coor_lat");
                    slng = subData.getJSONObject(Integer.toString(i)).getString("coor_lng");
                    break;
                }
            }
            lat = Float.parseFloat(slat);
            lng = Float.parseFloat(slng);
            parkingLotName = lot;
        } catch (JSONException e) {}

    }

    private Emitter.Listener onData = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            new GetCollegeDataTask().execute(args);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    JSONObject data = (JSONObject) args[0];
//                    ArrayList<String> colleges = new ArrayList<>();
//
//                    try {
//                        JSONObject subData = data.getJSONObject("college_data");
//                        for (int i = 100010; i <= 100012; i++){
//                            colleges.add(subData.getJSONObject(Integer.toString(i)).getString("college_name"));
//                        }
//
//                        //String cpp = data.getJSONObject("college_data").getJSONObject("100010").getString("college_name");
//                    } catch (JSONException e) {
//                        return;
//                    }
//                    //textView.setText(colleges.toString());
//                }
//            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off("data", onData);
    }

    private class GetCollegeDataTask extends AsyncTask<Object, Object, Void> {
        @Override
        protected Void doInBackground(Object... args) {
            JSONObject data = (JSONObject) args[0];
            datafile = data;
            colleges = new ArrayList<>();

            try {
                JSONObject subData = data.getJSONObject("college_data");
                for (int i = 100010; i <= 100012; i++){
                    colleges.add(subData.getJSONObject(Integer.toString(i)).getString("college_name"));
                }

                //String cpp = data.getJSONObject("college_data").getJSONObject("100010").getString("college_name");
            } catch (JSONException e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            PopulateSpinner(mCollegeSpinner, colleges);
        }
    }

    private class GetParkingDataTask extends AsyncTask<JSONObject, Void, Void> {
        @Override
        protected Void doInBackground(JSONObject... object) {
            JSONObject data = object[0];
            lots = new ArrayList<>();

            try {
                JSONObject subData = data.getJSONObject("parking_data");
                for (int i = 2001; i <= 2010; i++){
                    if (subData.getJSONObject(Integer.toString(i)).getString("college_id").equals(selected))
                        lots.add(subData.getJSONObject(Integer.toString(i)).getString("parkinglot_name"));
                }
            } catch (JSONException e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            PopulateSpinner(mParkingSpinner, lots);
        }
    }

    private void PopulateSpinner(Spinner spinner, ArrayList<String> list) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void viewMap(View view){
        Intent mapIntent = new Intent(this, MapsActivity.class);
        mapIntent.putExtra("lat", lat);
        mapIntent.putExtra("lng", lng);
        mapIntent.putExtra("parkingLotName", parkingLotName);
        startActivity(mapIntent);
    }
}
