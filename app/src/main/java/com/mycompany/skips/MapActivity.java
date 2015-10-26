package com.mycompany.skips;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.view.View;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends Activity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private ProgressDialog pDialog;
    TestParser jParser = new TestParser();
    public GoogleApiClient mGoogleApiClient;
    public LocationRequest mLocationRequest;
    ArrayList<LatLng> markerList = new ArrayList<LatLng>();
    JSONArray users = null, friends = null;
    TestParser jsonParser = new TestParser();
    boolean taskRunning = true;
    private static final String TAG_USER = "user";
    private static final String TAG_ID = "userID";
    private static final String TAG_SUCCESS = "success";
    private static String url_all_users = "http://testingforskipsproject.co.nf/get_all_users.php";
    private static String url_update_location = "http://testingforskipsproject.co.nf/update_location.php";
    private static String url_all_friends = "http://testingforskipsproject.co.nf/get_all_friends.php";
    private static final String TAG_FRIENDSHIP = "friendship";
    private static final String TAG_FRIEND1 = "friend1";
    private static final String TAG_FRIEND2 = "friend2";
    LatLng lastKnownLocation;
    private String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        //get username and password
        Intent intent = getIntent();
        userID = "" + intent.getStringExtra("userID");
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //test
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        android.location.LocationListener locationListener = new TestLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long)5000, (float)10, locationListener);
        //end test
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first

    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    public void onMapReady(GoogleMap map) {
        LatLng sevenSprings = new LatLng(40.022127,-79.299144);
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sevenSprings, 15));
        AsyncTask task = new PopulateMap(map).execute();
        while(taskRunning) {
        }
        //take all markers in markerList and add them to the map
        for(int i = 0; i < markerList.size(); i++) {
            map.addMarker(new MarkerOptions().position(markerList.get(i)).title("Marker"));
        }
    }


    public void moveToFriends(View view) {
        Intent intent = new Intent(this, FriendActivity.class);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        int duration = Toast.LENGTH_LONG;
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, "Connection Failed", duration);
        toast.show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Disconnected. Please re-connect.",Toast.LENGTH_LONG).show();
    }

    class UpdateLocation extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> paramsCreate = new ArrayList<NameValuePair>();
            paramsCreate.add(new BasicNameValuePair("longitude", Double.toString(lastKnownLocation.longitude)));
            paramsCreate.add(new BasicNameValuePair("latitude", Double.toString(lastKnownLocation.latitude)));
            paramsCreate.add(new BasicNameValuePair("userID", userID));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject jsonCreate = jsonParser.makeHttpRequest(url_update_location, "POST", paramsCreate);

            // check log cat fro response
            Log.d("Create Response", jsonCreate.toString());

            // check for success tag
            try {
                int success = jsonCreate.getInt(TAG_SUCCESS);

                if (success == 1) {

                } else {
                    // failed to create product
                    int duration = Toast.LENGTH_LONG;
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context, "There was an error updating your location in the system. Please restart the application.", duration);
                    toast.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {

        }
    }
    public class TestLocationListener implements android.location.LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            Toast.makeText(
                    getBaseContext(),
                    "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            lastKnownLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
            new UpdateLocation().execute();

        }
        @Override
        public void onProviderDisabled(String provider) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
    public class PopulateMap extends AsyncTask<String, String, String> {
        GoogleMap map;
        PopulateMap(GoogleMap m) {
            map = m;
        }

        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String doInBackground(String... args) {
            //build friend list
            //build friend list
            List<NameValuePair> friendParams = new ArrayList<NameValuePair>();
            JSONObject friendJson = jParser.makeHttpRequest(url_all_friends, "GET", friendParams);
            ArrayList<Integer> friendsList = new ArrayList<Integer>();
            if (friendJson != null) {
                try {
                    int success = friendJson.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        friends = friendJson.getJSONArray(TAG_FRIENDSHIP);
                        for (int i = 0; i < friends.length(); i++) {
                            JSONObject friend = friends.getJSONObject(i);
                            String friendOne = friend.getString(TAG_FRIEND1);
                            String friendTwo = friend.getString(TAG_FRIEND2);
                            int friend1 = Integer.parseInt(friendOne);
                            int friend2 = Integer.parseInt(friendTwo);
                            if (friend1 == Integer.parseInt(userID)) {
                                friendsList.add(friend2);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //get friend's locations
            // Building Parameters
            List<NameValuePair> userParams = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_users, "GET", userParams);
            if (json != null) {
                // Check your log cat for JSON reponse
                Log.d("All Users: ", json.toString());

                try {
                    // Checking for SUCCESS TAG
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        // products found
                        // Getting Array of Products
                        users = json.getJSONArray(TAG_USER);

                        // looping through All Products
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject c = users.getJSONObject(i);

                            // Storing each json item in variable
                            String id = c.getString(TAG_ID);
                            if (friendsList.contains(Integer.parseInt(id))) {
                                String lat = c.getString("lastKnownLatitude");
                                String longit = c.getString("lastKnownLongitude");
                                LatLng mark = new LatLng(Double.parseDouble(lat), Double.parseDouble(longit));
                                markerList.add(mark);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            taskRunning = false;
            return null;
        }

        protected void onPostExecute(String file_url) {

        }
    }
}
