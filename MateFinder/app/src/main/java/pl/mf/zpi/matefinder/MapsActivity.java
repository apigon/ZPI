package pl.mf.zpi.matefinder;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivity extends ActionBarActivity {


    GoogleMap googleMap;
    StreetViewPanorama myStreetView;
    boolean isStreetView=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        createMapView();

        addMarker();
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                createStreetView();
                if (myStreetView != null) {
                    isStreetView=true;
                    myStreetView.setPosition(latLng);
                    Fragment mapView = getFragmentManager().findFragmentById(R.id.mapView);
                    getFragmentManager().beginTransaction().hide(mapView).commit();
                    Fragment street = getFragmentManager().findFragmentById(R.id.streetView);
                    getFragmentManager().beginTransaction().show(street).commit();

                }

            }
        });

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        //googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setMyLocalization();


         setSupportActionBar(toolbar);
         getSupportActionBar().setHomeButtonEnabled(true);
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void createStreetView() {

        myStreetView = ((StreetViewPanoramaFragment)
                getFragmentManager().findFragmentById(R.id.streetView))
                .getStreetViewPanorama();
    }

    public void setMyLocalization() {
        Criteria criteria = new Criteria();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null)
        {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            LatLng coordinate = new LatLng(lat, lng);

            CameraUpdate center = CameraUpdateFactory.newLatLng(coordinate);


            googleMap.moveCamera(center);

        }
        else Toast.makeText(getApplicationContext(),
                "Localization problem", Toast.LENGTH_SHORT).show();
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        googleMap.animateCamera(zoom);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            return true;
        }
        if (id == android.R.id.home) {

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

    private void addMarker() {

        /** Make sure that the map has been initialised **/
        if (null != googleMap) {
            LatLng wroclaw = new LatLng(51.107885, 17.038538);
            googleMap.addMarker(new MarkerOptions().position(wroclaw).title("Moja chata hihi")
                    .draggable(true));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(wroclaw, 10));

        }

    }

    private void backToMain() {
        // Launching the login activity
        if(isStreetView)
        {

            Fragment street = getFragmentManager().findFragmentById(R.id.streetView);
            getFragmentManager().beginTransaction().hide(street).commit();
            Fragment map = getFragmentManager().findFragmentById(R.id.mapView);
            getFragmentManager().beginTransaction().show(map).commit();
            isStreetView=false;
        }
        else
        {
            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }
    @Override
    public void onBackPressed()
    {
        backToMain();
    }
}
