package com.example.jeevanaawara.googlemap;

import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;

import java.net.HttpURLConnection;
import java.net.URLConnection;

/**
 * Created by jeevanaawara on 30-Dec-15.
 */
public class GetDirectionPathIntentService extends IntentService {
    public GetDirectionPathIntentService() {
        super("GetDirectionPathIntentService");
    }

    private ResultReceiver resultReceiver;

    @Override
    protected void onHandleIntent(Intent intent) {
//        new DefaultHttpClient();
    }

    private void sendBackResult(){

    }
}
