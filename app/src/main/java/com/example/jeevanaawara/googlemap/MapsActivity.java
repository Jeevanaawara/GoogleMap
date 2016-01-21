package com.example.jeevanaawara.googlemap;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import android.widget.Toolbar;

public class MapsActivity extends AppCompatActivity{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private ListView suggestionListView;
    private ArrayAdapter<String> arrayAdapter;
    private static String TAG = "APP";
    private EditText sourceEt;
    private EditText destinationEt;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);


//        Got to My Location
        final FloatingActionButton myLocationFab = (FloatingActionButton) findViewById(R.id.my_location);
        myLocationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMap != null){
                    if (mMap.isMyLocationEnabled()) {
                        Location myLocation = mMap.getMyLocation();
    //                    CameraUpdateFactory.
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 13.0f));
                    } else {
                        Toast.makeText(MapsActivity.this, "Location Enabling" +
                                //                        " Lat : "+myLocation.getLatitude()+", Longt : "+myLocation.getLongitude() +
                                " ", Toast.LENGTH_LONG).show();
                        mMap.setMyLocationEnabled(true);
    //                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            }
        });
//        End Go to My Location

        final FloatingActionButton getDirectionFab = (FloatingActionButton) findViewById(R.id.get_direction);
        final LinearLayout getDirectionView = (LinearLayout) findViewById(R.id.get_direction_view);
        getDirectionFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (getDirectionView.getVisibility()){
                    case View.VISIBLE:
                        getDirectionView.setVisibility(View.INVISIBLE);
                        getDirectionFab.setImageResource(R.drawable.directions);
                        break;
                    case View.INVISIBLE:
                        getDirectionView.setVisibility(View.VISIBLE);
                        getDirectionFab.setImageResource(R.drawable.close);
                        break;
                }
//                getDirectionView.setVisibility(View.INVISIBLE);
            }
        });


//        Begin Geocoding

        sourceEt = (EditText) findViewById(R.id.source_et);
        destinationEt = (EditText) findViewById(R.id.destination_et);
        sourceEt.setOnFocusChangeListener(focusChangeListener);
        destinationEt.setOnFocusChangeListener(focusChangeListener);
        suggestionListView = (ListView) findViewById(R.id.suggestion_list);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        suggestionListView.setAdapter(arrayAdapter);
//        sourceEt.setAdapter(arrayAdapter);

//        arrayAdapter.add("Hello");
//        arrayAdapter.add("hi");
        sourceEt.setOnKeyListener(keyListener);
        destinationEt.setOnKeyListener(keyListener);
        suggestionListView.setOnItemClickListener(onItemClickListener);

//        End Geocoding

        ImageView myLocationSet = (ImageView) findViewById(R.id.my_location_source);
        myLocationSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle extras = myLocation.getExtras();
                if (extras != null){
                    String mylocationaddress = extras.getString("mylocationaddress");
                    if (mylocationaddress != null ){
                        sourceEt.setText(mylocationaddress);
                    }
                }
            }
        });
    }

    EditText focusedEt = null;

    private View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (b){
                focusedEt = (EditText) view;
            }
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            focusedEt.setText(arrayAdapter.getItem(i));
        }
    };

    private View.OnKeyListener keyListener = new  View.OnKeyListener(){

        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {

//            focusedEt = (EditText) view;

            if(!keyEvent.isAltPressed()
                    && !keyEvent.isCtrlPressed()
                    && !keyEvent.isShiftPressed()
                    && keyEvent.getAction() == KeyEvent.ACTION_UP
                    && !focusedEt.getText().toString().trim().isEmpty()){
                Log.i(Application.LOG, "View Listener: "+focusedEt);
                fetchAddressIntentServiceFromString(focusedEt.getText().toString().trim());
//                    arrayAdapter.clear();
//                        arrayAdapter.addAll(addressesArray);
//                    arrayAdapter.notifyDataSetChanged();
            }
            return false;
        }
    };



    private AddressesResultReceiver resultReceiver;

    private void fetchAddressIntentServiceFromString(String address){
        resultReceiver = new AddressesResultReceiver(new Handler());
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, FetchAddressIntentServiceFromString.class);
        intent.putExtra("resultReceiver", resultReceiver);
        intent.putExtra("address", address);
        startService(intent);
    }
    private void fetchAddressIntentServiceFromLatLng(Double latitude, Double longitude){
        resultReceiver = new AddressesResultReceiver(new Handler());
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, FetchAddressesIntentServiceFromLatLng.class);
        intent.putExtra("resultReceiver", resultReceiver);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startService(intent);
    }
    private MyLocationAddressResultReceiver myLocationAddressResultReceiver;
    private void fetchAddressFromMyLocation(Double latitude, Double longitude){
        myLocationAddressResultReceiver = new MyLocationAddressResultReceiver(new Handler());
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, FetchMyLocation.class);
        intent.putExtra("resultReceiver", myLocationAddressResultReceiver);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startService(intent);
    }



    @SuppressLint("ParcelCreator")
    private class AddressesResultReceiver extends ResultReceiver{

        public AddressesResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i(Application.LOG, "RESULT CODE : "+resultCode+"");
//            Log.i("DATA", resultData.getStringArray("addresses") + "");
            arrayAdapter.clear();
            arrayAdapter.addAll(resultData.getStringArray("addresses"));
            arrayAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("ParcelCreator")
    private class MyLocationAddressResultReceiver extends ResultReceiver{

        public MyLocationAddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
//            Log.i("RESULT_CODE", resultCode + "");
            String[] addresses = resultData.getStringArray("addresses");
            if (addresses.length > 0){
                Bundle bundle = new Bundle();
                bundle.putString("mylocationaddress", addresses[0]);
                myLocation.setExtras(bundle);
            }
        }
    }

    private void gpsService(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(MapsActivity.this, "GPS Not Enabled", Toast.LENGTH_SHORT).show();
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        }else{
            Toast.makeText(MapsActivity.this, "GPS Enabled", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        gpsService();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        int gpsIsAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if(gpsIsAvailable == ConnectionResult.SUCCESS){
            Toast.makeText(this, "Google Play Services Available", Toast.LENGTH_SHORT).show();
            if (mMap == null) {
                // Try to obtain the map from the SupportMapFragment.
                mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                        .getMap();
                if (mMap != null) {
                    setUpMap();
                }
            }
        }else{
            Toast.makeText(this, ConnectionsStatusCodes.getStatusCodeString(gpsIsAvailable), Toast.LENGTH_SHORT).show();
        }

        // Do a null check to confirm that we have not already instantiated the map.
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private static Location myLocation = null;
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (myLocation == null) {
                    myLocation = location;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10.0f));
                }
                if (myLocation != null && (myLocation.getLatitude() != location.getLatitude()
                        || myLocation.getLongitude() != location.getLongitude())) {
                    myLocation = location;
                    Toast.makeText(MapsActivity.this, "New Location is Lat : " + location.getLatitude() + ", Longt: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10.0f));
                    fetchAddressFromMyLocation(location.getLatitude(), location.getLongitude());
                }
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (focusedEt != null){
                    fetchAddressIntentServiceFromLatLng(latLng.latitude, latLng.longitude);
                }else{
                }
                Toast.makeText(MapsActivity.this, "Lat : "+latLng.latitude+", Long : "+latLng.longitude+"", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        if (menuId == R.id.search_menu){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void getDirectionPath(View view){
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, GetDirectionIntentService.class);
        intent.putExtra("resultReceiver", new GetDirectionPathResultReceiver(new Handler()));
//        intent.putExtra("latitude", latitude);
//        intent.putExtra("longitude", longitude);
        startService(intent);
   }

    @SuppressLint("ParcelCreator")
    public class GetDirectionPathResultReceiver extends ResultReceiver{

        public GetDirectionPathResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            try {
                JSONObject jsonObject = new JSONObject(resultData.getString("result"));
                String status = jsonObject.getString("status");
                Toast.makeText(MapsActivity.this, "Status : "+ status, Toast.LENGTH_SHORT).show();
                if(status.equals(Application.GoogleRequest.REQUEST_DENIED)){
                    Toast.makeText(MapsActivity.this,"Error Message : "+jsonObject.getString("error_message"), Toast.LENGTH_SHORT).show();
                }else if(status.equals(Application.GoogleRequest.OK)){
                    JSONArray routes = jsonObject.getJSONArray("routes");
                    Log.e(Application.LOG, "Routes Lenght : " + routes.length() + "");
                    if(routes.length() > 0){
//                        Log.i(Application.LOG, "Get Overview Polyline");
                        JSONObject route = routes.getJSONObject(0);
//                        Log.i(Application.LOG, "Get Route "+route.toString());
                        JSONObject overview_polyline = route.getJSONObject("overview_polyline");
                        List<LatLng> points = PolyUtil.decode(overview_polyline.getString("points"));

                        Iterator<LatLng> iterator = points.iterator();
                        PolylineOptions polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.BLUE);
                        while(iterator.hasNext()){
                            polylineOptions.add(iterator.next());
                        }


                        JSONObject bounds = route.getJSONObject("bounds");
                        JSONObject northeast = bounds.getJSONObject("northeast");
                        JSONObject southwest = bounds.getJSONObject("southwest");
                        mMap.addPolyline(polylineOptions);
                        LatLng southwestLatLng = new LatLng(southwest.getDouble("lat"),southwest.getDouble("lng"));
                        LatLng northeastLatLng = new LatLng(northeast.getDouble("lat"),northeast.getDouble("lng"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(southwestLatLng, northeastLatLng), 0));

                    }
                }
            } catch (JSONException e) {
                Log.getStackTraceString(e);
            }

        }
    }
}