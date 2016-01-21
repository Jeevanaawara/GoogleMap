package com.example.jeevanaawara.googlemap;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jeevanaawara on 28-Dec-15.
 */
public class GetDirectionIntentService extends IntentService {
    private ResultReceiver resultReceiver;
    public GetDirectionIntentService() {
        super("GetDirectionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        resultReceiver = intent.getParcelableExtra("resultReceiver");
//        Toast.makeText(getApplicationContext(), "Get Direction Path", Toast.LENGTH_SHORT).show();
        URL url;
        try {
            url = new URL("https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=AIzaSyDaXQyqsfpchMRZBVg2ShdybEVezkK8kx0");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String string = "";
            StringBuilder stringBuilder = new StringBuilder();
            while((string = bufferedReader.readLine()) != null){
                stringBuilder.append(string);
                Log.i(Application.LOG, "OUTPUT : "+string);
            }
            inputStream.close();
            sendBackResult(1, stringBuilder.toString());
        } catch (MalformedURLException e) {
            Log.getStackTraceString(e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.getStackTraceString(e);
            e.printStackTrace();
        }
    }
    private void sendBackResult(int resultCode, String result){
        Bundle bundle = new Bundle();
        bundle.putString("result", result);
        resultReceiver.send(resultCode, bundle);
    }

}
