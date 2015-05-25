package pl.mf.zpi.matefinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pl.mf.zpi.matefinder.app.AppConfig;
import pl.mf.zpi.matefinder.app.AppController;
import pl.mf.zpi.matefinder.helper.SQLiteHandler;


public class SettingsActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private Button zapisz;

    private RecyclerView mRecyclerView;                           // Declaring RecyclerView
    private RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    private RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    private DrawerLayout Drawer;                                  // Declaring DrawerLayout

    private ActionBarDrawerToggle mDrawerToggle;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences settings = getSharedPreferences(getString(R.string.settings_save_file), this.MODE_PRIVATE);

        CheckBox notification = (CheckBox)findViewById(R.id.notification);
        notification.setChecked(settings.getBoolean(getString(R.string.settings_save_key_sounds), false));

        CheckBox internet = (CheckBox)findViewById(R.id.internet);
        internet.setChecked(settings.getBoolean(getString(R.string.settings_save_key_transfer), false));

        Spinner navigation = (Spinner)findViewById(R.id.navigation);
        navigation.setSelection(settings.getInt(getString(R.string.settings_save_key_navigation), 0));

        Spinner layout = (Spinner)findViewById(R.id.layout);
        layout.setSelection(settings.getInt(getString(R.string.settings_save_key_motive), 0));

        NumberPicker radius = (NumberPicker)findViewById(R.id.radius);
        radius.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        radius.setValue(settings.getInt(getString(R.string.settings_save_key_radius), 0));

        zapisz = (Button) findViewById(R.id.button);
        zapisz.setOnClickListener(this);

        //Boczne menu
        db = new SQLiteHandler(this);
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
                mAdapter.notifyDataSetChanged();
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }
        }; // Drawer Toggle Object Made
        Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        NumberPicker np=(NumberPicker) findViewById(R.id.radius);
        np.setMaxValue(5);
        np.setMinValue(1);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            backToMain();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void backToMain() {
        // Launching the login activity
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.equals(zapisz)) {
            CheckBox internet = (CheckBox)findViewById(R.id.internet);
            CheckBox soudS=(CheckBox)findViewById(R.id.notification);
            Spinner navigation = (Spinner)findViewById(R.id.navigation);
            Spinner layout =(Spinner)findViewById(R.id.layout);
            NumberPicker radius = (NumberPicker)findViewById(R.id.radius);

            SharedPreferences settings = getSharedPreferences(getString(R.string.settings_save_file), this.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();

            editor.putBoolean(getString(R.string.settings_save_key_transfer), internet.isChecked());
            editor.putBoolean(getString(R.string.settings_save_key_sounds), soudS.isChecked());
            editor.putInt(getString(R.string.settings_save_key_navigation), navigation.getSelectedItemPosition());
            editor.putInt(getString(R.string.settings_save_key_motive), layout.getSelectedItemPosition());
            editor.putInt(getString(R.string.settings_save_key_radius), radius.getValue());

            editor.commit();

            String radiusS =""+radius.getValue();
            updateRadius(radiusS);
            Toast toast = Toast.makeText(this, "Zapisano zmiany", Toast.LENGTH_SHORT);
            toast.show();
            backToMain();
        }
    }

    private void updateRadius(final String radius) {
        // Tag used to cancel the request
        String tag_string_req = "update_radius_req";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Set radius Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if(error){
                        Toast.makeText(getApplicationContext(), "Błąd połączenia z internetem", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Update Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                HashMap<String, String> user = db.getUserDetails();
                String userID = user.get("userID");
                params.put("tag", "setRadius");
                params.put("userID", userID);
                params.put("value", radius);

                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}
