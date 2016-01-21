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
 * Created by jeevanaawara on 27-Dec-15.
 */
public class FetchAddressesIntentServiceFromLatLng extends IntentService{


    public FetchAddressesIntentServiceFromLatLng() {
        super("FetchAddressesIntentServiceFromLatLng");
    }

    private ResultReceiver resultReceiver;
    private static String TAG = "APP";
    private Geocoder geocoder;

    @Override
    protected void onHandleIntent(Intent intent) {
        resultReceiver = intent.getParcelableExtra("resultReceiver");
        List<Address> addresses = null;
        try {
            Log.i(Application.LOG, "Address Get from Source Latitude : " + intent.getDoubleExtra("latitude", 0.0) + ", Longitude " + intent.getDoubleExtra("longitude", 0.0));
            geocoder = new Geocoder(this);

            addresses = geocoder.getFromLocation(intent.getDoubleExtra("latitude", 0.0), intent.getDoubleExtra("longitude", 0.0), 20);
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
                Log.i(Application.LOG, fullAddress.toString());
            }
            sendBackResult(1, addressesArray);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(Application.LOG, Log.getStackTraceString(e));
        }

    }

    private void sendBackResult(int resultCode, String[] addresses){
        Bundle bundle = new Bundle();
        bundle.putStringArray("addresses", addresses);
        resultReceiver.send(resultCode, bundle);
    }

}
