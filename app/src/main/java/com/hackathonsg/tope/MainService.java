package com.hackathonsg.tope;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.Queue;

public class MainService extends Service {

    static UUID PEBBLE_APP_UUID = UUID.fromString("54ccf6fe-1af4-456a-8ee8-4726c96d0bcd");
    static boolean serviceCreated = false;
    //static boolean pebbleAppStarted = false;

    // Pebble -> Android
    static final byte MESSAGE_TYPE_APP_START = 0;
    static final byte MESSAGE_TYPE_TOPE_EVENT = 1;
    static final byte MESSAGE_TYPE_VALIDATION = 2;

    // Android -> Pebble
    static final byte MESSAGE_TYPE_REPONSETOPE = 0;

    final Queue<String> queue = new LinkedList<String>();
    public MainService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final GetOwnerInfo info = new GetOwnerInfo(this);
        Log.d("Tope", info.name);
        final SharedPreferences settings = getSharedPreferences("TopePrefs", MODE_PRIVATE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                AndroidHttpClient client = AndroidHttpClient.newInstance("TopeApp");
                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("name", info.name));
                    nameValuePairs.add(new BasicNameValuePair("email", info.email));
                    HttpPut req = new HttpPut("/api/user/" + settings.getInt("id", -1));
                    req.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse resp = client.execute(new HttpHost("lechateau.lambertz.fr", 3000), req);
                } catch (IOException e) {
                    Log.d("Tope", "HTTP Error", e);
                }
                client.close();
            }
        }).start();

        if(!serviceCreated) {

            serviceCreated = true;
            // Check if Pebble is connected
            //pebbleConnected = PebbleKit.isWatchConnected(getApplicationContext());

            //if (pebbleConnected) {
                // Check if Message is supported
            //    pebbleMessageSupported = PebbleKit.areAppMessagesSupported(getApplicationContext());
            //}

            // Start Handler for get Message
            PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
                @Override
                public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                    switch(data.getUnsignedInteger(0).byteValue()){
                        case MESSAGE_TYPE_APP_START:
                            Log.d("Tope", "Tope app started");
                            if (queue.peek() != null)
                                sendNotificationsToPebble(queue.peek().toString());
                        break;
                        case MESSAGE_TYPE_TOPE_EVENT:
                            Log.d("Tope", "Tope at " + System.currentTimeMillis());
                            findTopeNearMe();
                        break;
                        case MESSAGE_TYPE_VALIDATION:
                            Log.d("Tope", "Tope validated event");
                            queue.poll();
                            if (queue.peek() != null) {
                                sendNotificationsToPebble(queue.peek().toString());
                            }
                        break;
                    }
                    PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
                }
            });

            // Send Data to Pebble
            // PebbleDictionary data = new PebbleDictionary();
            // data.addString(0, "l'app Tope! est top");
            // PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, data);

            // Start App
            // PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);

        };

        return Service.START_NOT_STICKY;
    }

    private void findTopeNearMe() {
        /*LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_FINE);
        locationManager.requestLocationUpdates(1000, 1, crit, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                AndroidHttpClient client = AndroidHttpClient.newInstance("TopeApp");
                long timestamp = System.currentTimeMillis();
                try {
                    SharedPreferences settings = getSharedPreferences("TopePrefs", 0);
                    HttpResponse resp = client.execute(new HttpHost("lechateau.lambertz.fr", 3000),
                            new HttpGet("/api/user/" + settings.getInt("id", 0) + "/timestamp/" + timestamp));
                    Log.d("Tope", EntityUtils.toString(resp.getEntity(), "UTF_8"));
                } catch (IOException e) {
                    Log.d("Tope", "Got an error bro", e);
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        }, Looper.myLooper());*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                AndroidHttpClient client = AndroidHttpClient.newInstance("TopeApp");
                SharedPreferences settings = getSharedPreferences("TopePrefs", MODE_PRIVATE);
                long timestamp = System.currentTimeMillis();
                HttpResponse resp = null;
                JSONObject body = null;
                int caseInt = 0;
                int count = 0;
                do {
                    if (caseInt == 1) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        Log.d("Tope", ((Integer)settings.getInt("id", -1)).toString());
                        caseInt++;
                    }
                    try {
                        resp = client.execute(new HttpHost("lechateau.lambertz.fr", 3000),
                                new HttpGet("/api/tap/" + settings.getInt("id", -1) + "/timestamp/" + timestamp));
                        body = new JSONObject(EntityUtils.toString(resp.getEntity(), "UTF_8"));
                        Log.d("Tope", body.toString());
                        caseInt = body.getInt("case");
                    } catch (IOException e) {
                        Log.e("Tope", "HTTP Error", e);
                    } catch (JSONException e) {
                        Log.e("Tope", "JSON Error", e);
                    }
                    count++;
                } while (count < 5 && (body == null || caseInt == 1));
                client.close();
                if (body == null || caseInt == 1) {
                    return;
                }
                int id;
                String name = null;
                try {
                    id = body.getInt("id");
                    if (body.has("name")) {
                        name = body.getString("name");
                    }
                } catch (JSONException e) {
                    return;
                }
                if (name != null)
                    queue.add(name);
                else
                    queue.add(((Integer)id).toString());
                sendNotificationsToPebble(queue.peek().toString());

                // PebbleDictionary data = new PebbleDictionary();
                // data.addString(0, "l'app Tope! est top");
                // PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, data);

            }
        }).start();
    }

    private void sendNotificationsToPebble(String name) {
        PebbleDictionary senddata = new PebbleDictionary();
        senddata.addUint8(0, MESSAGE_TYPE_REPONSETOPE);
        senddata.addString(1, name);
        PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, senddata);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
