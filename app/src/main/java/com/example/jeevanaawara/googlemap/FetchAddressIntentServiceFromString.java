package com.example.jeevanaawara.googlemap;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jeevanaawara on 26-Dec-15.
 */
public class FetchAddressIntentServiceFromString extends IntentService {

    private static String TAG = "APP";

    protected ResultReceiver resultReceiver;
    private Geocoder geocoder;

    public FetchAddressIntentServiceFromString(){
        super("FetchAddressess");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        resultReceiver = intent.getParcelableExtra("resultReceiver");
        List<Address> addresses = null;
        try {
            Log.i(TAG, "Address Get from Source "+intent.getStringExtra("address"));
            geocoder = new Geocoder(this);
            addresses = geocoder.getFromLocationName(intent.getStringExtra("address").trim(), 20);
            Iterator<Address> iterator = addresses.iterator();
            String[] addressesArray = new String[addresses.size()];
            int index = 0;
            while(iterator.hasNext()){
                Address address = iterator.next();
                int i = 0;
                StringBuilder fullAddress = new StringBuilder();

                while(i <= address.getMaxAddressLineIndex()){
                    fullAddress.append(address.getAddressLine(i)+" ");
                    i++;
                }
//                String addressString = address.getCountryName() + " " + address.getAddressLine(0);
                String addressString = fullAddress.toString();
                addressesArray[index] = addressString;
                index++;
                Log.i(TAG, fullAddress.toString());
            }
            sendBackResult(1, addressesArray);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void sendBackResult(int resultCode, String[] addresses){
        Bundle bundle = new Bundle();
        bundle.putStringArray("addresses", addresses);
        resultReceiver.send(resultCode, bundle);
    }
}
