package pl.mf.zpi.matefinder;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import pl.mf.zpi.matefinder.app.AppConfig;
import pl.mf.zpi.matefinder.app.AppController;
import pl.mf.zpi.matefinder.helper.JSONParser;
import pl.mf.zpi.matefinder.helper.SQLiteHandler;
import pl.mf.zpi.matefinder.helper.SessionManager;


public class MapsActivity extends ActionBarActivity implements LocationListener {


    GoogleMap googleMap;
    StreetViewPanorama myStreetView;
    boolean isStreetView = false;
    String provider;
    Location location;
    LocationManager locationManager;
    Criteria criteria;
    Marker me;
    List<Marker> markers;
    Context context;
    Bundle bundle;
    LatLng friendLoc; //do wyznaczania trasy

    private SQLiteHandler db;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private ProgressDialog pDialog;

    private RecyclerView mRecyclerView;                           // Declaring RecyclerView
    private RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    private RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    private DrawerLayout Drawer;                                  // Declaring DrawerLayout

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        createMapView();
        context = MapsActivity.this;

        if (googleMap != null)
            googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    createStreetView(latLng);
                }
            });


        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        //pobiera lokalizacje i ustawia na nas kamere
        setMyLocation();

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        //actionbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { // jesli wylaczony GPS wyswietl ALERT
            buildAlertMessageNoGps();
        }


        //baza danych
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        db = new SQLiteHandler(getApplicationContext());

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        mAdapter = new MenuAdapter(this, db);       // Creating the Adapter of MenuAdapter class(which we are going to see in a bit)
        // And passing the titles,icons,header view name, header view email,
        // and header view profile picture

        mRecyclerView.setAdapter(mAdapter);                              // Setting the adapter to RecyclerView

        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager

        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager


        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this, Drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }


        }; // Drawer Toggle Object Made
        Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State


        bundle = getIntent().getParcelableExtra("friendID");
        if (bundle != null) {
            friendLoc = bundle.getParcelable("friendLoc");
            if (friendLoc != null) {
                fetchRouteToFriend(friendLoc); // wyznacz trase
            }
        }

        if (connChecker())
            getMyFriendsLocation("Znajomi");

    }


    public void fetchRouteToFriend(LatLng friendLocation) {
        LatLng myLocation = null;
        try {
            myLocation = getMyLastLocation();
        } catch (IOException e) {
            Log.e("Error with you", "s");
        }
        ;
        if (myLocation != null) {
            Log.e("moja", Double.toString(myLocation.latitude) + " " + Double.toString(friendLocation.latitude));
            String url = makeURL(myLocation.latitude, myLocation.longitude, friendLocation.latitude, friendLocation.longitude);
            new connectAsyncTask(url).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else
            Toast.makeText(getApplicationContext(), "Brak historii Twojej lokalizacji!", Toast.LENGTH_LONG).show();
    }

    //aktualizacja danych na serwer oraz do bazy SQLite
    private void updateLocationDB(final String lat, final String lng) throws IOException {
        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> user = db.getUserDetails();
        final String userId = user.get("userID");
        final String login = user.get("login");
        String tag_string_req = "update_req";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Update Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully updated in MySQL
                        // Now store the user in sqlite
                        JSONObject user = jObj.getJSONObject("user");
                        String locationID = user.getString("locationID");
                        String lat = user.getString("lat");
                        String lng = user.getString("lng");

                        // Inserting row in users table
                        db.deleteLocations();
                        db.addLocation(locationID, lat, lng);
                        Toast.makeText(getApplicationContext(), "Zmiany zostały zapisane.", Toast.LENGTH_LONG).show();
                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "My Location Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }

        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "updateLocation");
                params.put("userID", userId);
                params.put("lat", lat);
                params.put("lng", lng);

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void createStreetView(final LatLng latLng) {


        if (myStreetView == null)
            myStreetView = ((StreetViewPanoramaFragment)
                    getFragmentManager().findFragmentById(R.id.streetView))
                    .getStreetViewPanorama();
        myStreetView.setPosition(latLng);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {


                if (myStreetView.getLocation() != null) {


                    isStreetView = true;
                    Fragment mapView = getFragmentManager().findFragmentById(R.id.mapView);
                    getFragmentManager().beginTransaction().hide(mapView).commit();
                    Fragment street = getFragmentManager().findFragmentById(R.id.streetView);
                    getFragmentManager().beginTransaction().show(street).commit();
                } else
                    Toast.makeText(getApplicationContext(),
                            "StreetView niedostępne w tym miejscu!", Toast.LENGTH_SHORT).show();
            }
        }, 1000);


    }

    public void setMyLocation() {
        //LatLng coordinate = null;
        criteria = new Criteria();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        refresh();
        locationManager.requestLocationUpdates(provider, 5000, 10, this); // odswiezanie co 5 sek lub 10 metrow

        //CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        if (location != null) {
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
            moveCameraOnMe();
            googleMap.animateCamera(zoom);
        } else {
            Toast.makeText(getApplicationContext(), "Problem z lokalizacją!", Toast.LENGTH_SHORT).show();
            Log.e("mapApp", "Problem z lokalizacją!");
            try {

                if (getMyLastLocation() != null) {
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                    CameraUpdate lastLocation = CameraUpdateFactory.newLatLng(getMyLastLocation());
                    googleMap.moveCamera(lastLocation);
                    googleMap.animateCamera(zoom);
                } else
                    addWroclawMarker();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //refresh();
        }


        //  return coordinate;
    }


    public void moveCameraOnMe() {
        if (me != null) {
            me.remove();
            me = null;
        }


        double lat = location.getLatitude();
        double lng = location.getLongitude();
        LatLng coordinate = new LatLng(lat, lng);
        me = googleMap.addMarker(new MarkerOptions().position(coordinate).title("Ty")
                .draggable(false));
        CameraUpdate center = CameraUpdateFactory.newLatLng(coordinate);
        googleMap.moveCamera(center);

    }

    public void refresh() {
        provider = locationManager.getBestProvider(criteria, false);
        //location = locationManager.getLastKnownLocation(provider);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null)
            try {
                if (connChecker()) {
                    updateLocationDB(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));
                }
                moveCameraOnMe();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private SessionManager session;
   // private static TimerTask doAsynchronousTask;
    //private static TimerTask doAsynchronousTask;
    // Wyloguj
    private void logoutUser() {
        session.setLogin(false);

      //  doAsynchronousTask.cancel();
      //  doAsynchronousTask = null;

        db.deleteFriends();
        db.deleteGroups();
        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); //tylko tutaj finish() ma uzasadnienie !!!
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_logout:
                logoutUser();
                Toast.makeText(this, "Wylogowano!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createMapView() {
        /**
         * Catch the null pointer exception that
         * may be thrown when initialising the map
         */
        try {
            if (null == googleMap) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();


                /**
                 * If the map is still null after attempted initialisation,
                 * show an error to the user
                 */
                if (null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception) {
            Log.e("mapApp", exception.toString());
        }
    }

    private void addWroclawMarker() {
        /** Make sure that the map has been initialised **/
        if (null != googleMap) {
            LatLng wroclaw = new LatLng(51.107885, 17.038538);
            googleMap.addMarker(new MarkerOptions().position(wroclaw).title("Centrum Wrocławia")
                    .draggable(true));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(wroclaw, 10));

        }

    }

    private void getMyFriendsLocation(final String groupName) {
        db = new SQLiteHandler(getApplicationContext());
        // final String [] friends = getMyFriendsId();

        HashMap<String, String> user = db.getUserDetails();
        final String userId = user.get("userID");
        String tag_string_req = "update_req";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Update Response: " + response.toString());
                hideDialog();

                try {
                    db.deleteFriendsLocations();
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully updated in MySQL
                        // Now store the user in sqlite

                        JSONArray user = jObj.getJSONArray("users");
                        for (int i = 0; i < user.length(); i++) {
                            // user successfully logged in
                            JSONObject u = user.getJSONObject(i);
                            int location = u.getInt("location");
                            String login = u.getString("login");
                            String lat = u.getString("lat");
                            String lng = u.getString("lng");
                            // Inserting row in users table

                            db.addFriendLocation(location, login, lat, lng);
                        }


                        // Inserting row in users table
                        //db.deleteLocations();
                        try {
                            showMyFriends();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ;
                        Toast.makeText(getApplicationContext(), "Pobrano lokalizacje użytkowników", Toast.LENGTH_LONG).show();
                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Friends Locations Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }

        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "getFriendsLocations");
                params.put("userID", userId);
                params.put("groupName", groupName);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void showMyFriends() throws IOException {
        db = new SQLiteHandler(getApplicationContext());
        markers = new ArrayList<Marker>();
        db.setGroupVisible("Znajomi1", "1");
        Marker marker;
        // Fetching user details from sqlite
        LatLng friendLocation = null;
        // List<HashMap<String, String>> friends = db.getFriendLocationDetails();//
        List<HashMap<String, String>> friends = db.getFriendLocationsFromGroups();
        int i = 0;
        while (i < friends.size()) {
            if (friends.get(i).get("lat") != null && friends.get(i).get("lng") != null) {


                String friendLogin = friends.get(i).get("login");
                String friendLat = friends.get(i).get("lat");
                String friendLng = friends.get(i).get("lng");
                if (!friendLat.equals("null") && !friendLng.equals("null")) {
                    double lat = Double.parseDouble(friendLat.toString());
                    double lng = Double.parseDouble(friendLng.toString());
                    friendLocation = new LatLng(lat, lng);
                    marker = googleMap.addMarker(new MarkerOptions()
                            .position(friendLocation)
                            .title(friendLogin)
                            .snippet(friendLogin)
                            .draggable(false)
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    marker.showInfoWindow();
                    markers.add(marker);
                }
                Log.e(TAG, "friend " + friendLogin + " " + friends.get(i).get("locationID"));
            } else
                Log.e(TAG, "NULL FRIEND LOCATION" + friends.get(i).get("locationID"));
            i++;
        }
    }

    private LatLng getMyLastLocation() throws IOException {
        db = new SQLiteHandler(getApplicationContext());
        // Fetching user details from sqlite
        LatLng lastLocation = null;
        HashMap<String, String> locations = db.getLocationDetails();
        if (!(locations.get("lat")).equals("0") && !(locations.get("lng")).equals("0")) {
            String latString = locations.get("lat");
            String lngString = locations.get("lng");
            double lat = Double.parseDouble(latString.toString());
            double lng = Double.parseDouble(lngString.toString());
            lastLocation = new LatLng(lat, lng);
        }
        return lastLocation;
    }

    private void backToMain() {
        // Launching the login activity
        if (isStreetView) {

            Fragment street = getFragmentManager().findFragmentById(R.id.streetView);
            getFragmentManager().beginTransaction().hide(street).commit();
            Fragment map = getFragmentManager().findFragmentById(R.id.mapView);
            getFragmentManager().beginTransaction().show(map).commit();
            isStreetView = false;
            myStreetView.setPosition((LatLng) null);
        } else {
            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }


    Polyline line;

    public void drawPath(String result) {
        if (line != null) {
            googleMap.clear();
        }

        try {
            // Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes
                    .getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);

            LatLng middle=null;
            PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
            for (int z = 0; z < list.size(); z++) {
                LatLng point = list.get(z);
                options.add(point);
                if(list.size()/2==z)
                    middle=list.get(z); //do wyznaczania miejsca spotkania
            }
            line = googleMap.addPolyline(options);
            if(middle!=null)
                    googleMap.addMarker(new MarkerOptions()
                    .position(middle)
                    .title("Proponowane miejsce spotkania")
                    .draggable(false)
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            LatLngBounds.Builder builder = new LatLngBounds.Builder(); //oddalenie kamery dla widocznosci calej trasy
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            builder.include(getMyLastLocation());
            LatLngBounds bounds = builder.build();
            int padding = 10; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            googleMap.animateCamera(cu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    public String makeURL(double sourcelat, double sourcelog, double destlat,
                          double destlog) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString.append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        return urlString.toString();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Twój GPS wygląda na wyłączony, chcesz go włączyć?")
                .setCancelable(false)
                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Handler handler = new Handler(); //wait 1 sec than try again set my location
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setMyLocation();
            }
        }, 1500);

    }

    @Override
    public void onBackPressed() {
        backToMain();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        refresh();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    private class connectAsyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressDialog;
        String url;

        connectAsyncTask(String urlPass) {
            url = urlPass;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONParser jParser = new JSONParser();
            String json = jParser.getJSONFromUrl(url);
            return json;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();
            if (result != null) {
                drawPath(result);
            }
        }
    }

    private boolean connChecker() {
        boolean conn_ok = false;
        SharedPreferences settings = getSharedPreferences(getString(R.string.settings_save_file), this.MODE_PRIVATE);
        boolean transfer = settings.getBoolean(getString(R.string.settings_save_key_transfer), true);
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo internet = connManager.getActiveNetworkInfo();
        Log.d(TAG, "Shared transfer: " + connManager.getActiveNetworkInfo());
        if (transfer == false && internet != null && internet.isConnected() || transfer == true && mWifi.isConnected()) {
            conn_ok = true;
        }
        return conn_ok;
    }
}
