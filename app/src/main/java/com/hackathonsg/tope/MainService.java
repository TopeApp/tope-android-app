package com.hackathonsg.tope;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

public class MainService extends Service {

    static UUID PEBBLE_APP_UUID = UUID.fromString("54ccf6fe-1af4-456a-8ee8-4726c96d0bcd");
    static boolean serviceCreated = false;
    static boolean pebbleConnected = false;
    static boolean pebbleMessageSupported = false;
    static boolean pebbleAppStarted = false;

    static final byte MESSAGE_TYPE_APP_START = 0;
    static final byte MESSAGE_TYPE_TOPE_EVENT = 1;

    public MainService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(!serviceCreated) {

            serviceCreated = true;
            // Check if Pebble is connected
            pebbleConnected = PebbleKit.isWatchConnected(getApplicationContext());

            if (pebbleConnected) {
                // Check if Message is supported
                pebbleMessageSupported = PebbleKit.areAppMessagesSupported(getApplicationContext());
            }

            // Start Handler for get Message
            PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
                @Override
                public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                    switch(data.getUnsignedInteger(0).byteValue()){
                        case MESSAGE_TYPE_APP_START :

                        break;
                        case MESSAGE_TYPE_TOPE_EVENT:
                            Log.d("", "Tope at "+data.getUnsignedInteger(1));
                        break;
                    }
                    PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
                }
            });

            // Send Data to Pebble
            // PebbleDictionary data = new PebbleDictionary();
            // data.addString(0, "l'app Tope! est géniale");
            // PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, data);

            // Start App
            // PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);

        };

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
