package com.qrcodedetect;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.StringDef;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    String type;
    String s;
    String s1;
    Date d;
    String valid;
    String scannedText;
    Button scanbtn;
    TextView result;
    TextView dbMessage;
    public static final int REQUEST_CODE = 100;
    public static final int PERMISSION_REQUEST = 200;

    private static final String link = "http://192.168.136.80/qrscanner/update.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // change activity background color
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        scanbtn = (Button) findViewById(R.id.scanbtn);
        scanbtn.setBackgroundColor(Color.CYAN);
        result = (TextView) findViewById(R.id.result);
        dbMessage = (TextView) findViewById(R.id.message);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST);
        }


        scanbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }



    class Retrieve_data extends AsyncTask {
        private Context context;
        int fh=0;

        public Retrieve_data(Context applicationContext) {
            this.context = applicationContext;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String data;
            String link;
            BufferedReader bufferedReader;
            String result;

            try {
                Log.d("sent_data", "qrID: " +scannedText);
                Log.d("sent_data", "Date: " +s1);
                Log.d("sent_data", "Hour: " +type);
                data = "?qrID=" + URLEncoder.encode(scannedText, "UTF-8");
                data += "&date=" + URLEncoder.encode(s1, "UTF-8");
                data += "&type=" + URLEncoder.encode(type, "UTF-8");
                link = "http://192.168.136.80/qrscanner/update.php" +data;
                URL url = new URL(link);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                result = sb.toString();
                Log.d("rec_data", "result: " +result);
                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT);
                return result;

            } catch (Exception e) {
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(Object o) {

            String jsonStr = o.toString();
            Log.d("test","Returned value from database is " +jsonStr);

            dbMessage.setText(jsonStr);

            if(jsonStr.substring(0,1).equals("T")) {
                Log.d("TAG", "val is true");
                dbMessage.setText("Successful");
            }

            if(jsonStr.substring(0,1).equals("F")) {
                 Log.d("TAG", "val is false");
                 dbMessage.setText("Unsuccessful");
            }

            //Toast.makeText(getApplicationContext(), jsonStr, Toast.LENGTH_SHORT).show();
            super.onPostExecute(o);
            }
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            if(data != null){
                final Barcode barcode = data.getParcelableExtra("barcode");
                result.post(new Runnable() {
                    @Override
                    public void run() {
                        result.setText(barcode.displayValue);

                        // Get Scanned String.
                        scannedText = barcode.rawValue;
                        Log.d("TAG", "Scanned Text " +scannedText);

                        // Get current date
                        d = new Date();
                        s  = DateFormat.format("d MMMM, yyyy ", d.getTime()).toString();
                        s1 = s.substring(0,2);
                        Log.d("TAG", "date is " +s1);

                        // Get Time
                        Calendar c = Calendar.getInstance();
                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        Log.d("HOUR", "Hour: " +hour);
                        if(hour>=18 && hour<=23) {
                            type = "Dinner";
                        }
                        else
                            type = "Lunch";

                        // Validate scanned data
                        valid = scannedText.substring(0,9);
                        Log.d("valid", "scanned is"+valid);
                        String temp = "NCC_2018_".toString();
                        if(!(valid.equals(temp))) {
                            Log.d("valid", "String is invalid");
                            dbMessage.setText("Invalid QR code");
                            return;
                        }
                        new Retrieve_data(getApplicationContext()).execute();
                    }
                });
            }
        }
    }
}





