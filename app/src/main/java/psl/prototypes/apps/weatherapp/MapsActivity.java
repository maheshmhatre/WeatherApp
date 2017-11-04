package psl.prototypes.apps.weatherapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,GoogleMap.InfoWindowAdapter
        {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private double currentLat;
    private double currentLon;
private String temparature;
private String humidity;
private String minTemp1;
private String maxTemp1;



    private static final String TAG = "MapsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //mapView = (MapView) findViewById(R.id.map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(MapsActivity.this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }


    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(this);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.e(TAG,"Clicked on map");
                Intent intent = new Intent(MapsActivity.this,WeatherActivity.class);
                intent.putExtra("latitude",currentLat);
                intent.putExtra("longitude",currentLon);
                startActivity(intent);
            }
        });
    }

    public void getWeatherData(){

                String server_url  = "http://api.openweathermap.org/data/2.5/weather?lat=" + Double.toString(currentLat) + "&lon=" +  Double.toString(currentLon)+ "&APPID=ba6f50c8f8128fa2cce45dc13d58ed2c";
                Log.e("RegisterActivity",server_url);
                JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET,
                        server_url,null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONObject weather= response.getJSONObject("main");


                                    temparature = weather.getString("temp");
                                    humidity= weather.getString("humidity");
                                    minTemp1=weather.getString("temp_min");
                                    maxTemp1=weather.getString("temp_max");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                            } catch (Exception e1) {
                                // Couldn't properly decode data to string
                                Toast.makeText(getApplicationContext(),"An error occured while creating the account. Please try again later",Toast.LENGTH_SHORT).show();
                                e1.printStackTrace();
                            }
                        }
                    }
                });

                RequestQueueFactory.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
            }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    MY_PERMISSION_ACCESS_COARSE_LOCATION );
        }

        float zoomLevel = 8.0f;
       Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d(TAG,mLastLocation.getLatitude() + " , " +mLastLocation.getLongitude());
         currentLat = mLastLocation.getLatitude();
         currentLon = mLastLocation.getLongitude();

            LatLng currentLocation = new LatLng(currentLat, currentLon);
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("My Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,zoomLevel));

            getWeatherData();

        }else{
            Log.d(TAG,"Receiving nul for location");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "+ connectionResult.getErrorCode());
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        //return null;
        return prepareInfoView(marker);
    }

    private View prepareInfoView(Marker marker){
        //prepare InfoView programmatically
        LinearLayout infoView = new LinearLayout(MapsActivity.this);
        LinearLayout.LayoutParams infoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoView.setOrientation(LinearLayout.HORIZONTAL);
        infoView.setLayoutParams(infoViewParams);

        ImageView infoImageView = new ImageView(MapsActivity.this);
        //Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher);
        Drawable drawable = getResources().getDrawable(android.R.drawable.ic_dialog_map);
        infoImageView.setImageDrawable(drawable);
        infoView.addView(infoImageView);

        LinearLayout subInfoView = new LinearLayout(MapsActivity.this);
        LinearLayout.LayoutParams subInfoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subInfoView.setOrientation(LinearLayout.VERTICAL);
        subInfoView.setLayoutParams(subInfoViewParams);

        TextView subInfoLat = new TextView(MapsActivity.this);
        subInfoLat.setText("Temperature : " + temparature);
        TextView subInfoLnt = new TextView(MapsActivity.this);
        subInfoLnt.setText("Humidity :  " + humidity);
        TextView minTemp = new TextView(MapsActivity.this);
        minTemp.setText("Min Temp :  " + minTemp1);
        TextView maxTemp = new TextView(MapsActivity.this);
        maxTemp.setText("Max Temp :  " + maxTemp1);
        subInfoView.addView(subInfoLat);
        subInfoView.addView(subInfoLnt);
        subInfoView.addView(minTemp);
        subInfoView.addView(maxTemp);
        infoView.addView(subInfoView);

        return infoView;
    }


}
