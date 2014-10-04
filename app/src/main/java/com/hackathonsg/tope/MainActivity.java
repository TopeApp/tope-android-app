package com.hackathonsg.tope;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start service
        final Intent i = new Intent(getApplicationContext(), MainService.class);
        final SharedPreferences settings = getSharedPreferences("TopePrefs", 0);
        if (settings.contains("id")) {
            getApplicationContext().startService(i);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AndroidHttpClient client = AndroidHttpClient.newInstance("TopeApp");
                    try {
                        HttpResponse resp = client.execute(new HttpHost("lechateau.lambertz.fr", 3000),
                                new HttpPost("/api/user"));
                        JSONObject obj = new JSONObject(EntityUtils.toString(resp.getEntity()));
                        int userid = obj.getInt("id");
                        settings.edit().putInt("id", userid).commit();
                        getApplicationContext().startService(i);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
