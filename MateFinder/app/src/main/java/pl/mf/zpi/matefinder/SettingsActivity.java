package pl.mf.zpi.matefinder;

import android.app.Fragment;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pl.mf.zpi.matefinder.app.AppConfig;
import pl.mf.zpi.matefinder.app.AppController;
import pl.mf.zpi.matefinder.helper.SQLiteHandler;


public class SettingsActivity extends ActionBarActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

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

        RadioGroup notification_group = (RadioGroup) findViewById(R.id.notification_radios);
        notification_group.setOnCheckedChangeListener(this);
        RadioButton notification_silent = (RadioButton) findViewById(R.id.notification_silent);
        notification_silent.setChecked(settings.getBoolean(getString(R.string.settings_save_key_notification_silent), false));
        RadioButton notification_loud = (RadioButton) findViewById(R.id.notification_loud);
        notification_loud.setChecked(settings.getBoolean(getString(R.string.settings_save_key_notification_loud), false));
        CheckBox notification_sound = (CheckBox) findViewById(R.id.notification_sound);
        notification_sound.setChecked(settings.getBoolean(getString(R.string.settings_save_key_notification_sound), false));
        CheckBox notification_vibrate = (CheckBox) findViewById(R.id.notification_vibrate);
        notification_vibrate.setChecked(settings.getBoolean(getString(R.string.settings_save_key_notification_vibrate), false));

        boolean some_checked = settings.getBoolean(getString(R.string.settings_save_key_notification_silent), false) || settings.getBoolean(getString(R.string.settings_save_key_notification_loud), false);
        if (!some_checked) {
            notification_loud.setChecked(true);
            notification_sound.setChecked(true);
            notification_vibrate.setChecked(true);
        }

        CheckBox internet = (CheckBox) findViewById(R.id.internet);
        internet.setChecked(settings.getBoolean(getString(R.string.settings_save_key_transfer), false));

        Spinner layout = (Spinner) findViewById(R.id.layout);
        layout.setSelection(settings.getInt(getString(R.string.settings_save_key_motive), 0));

        NumberPicker radius = (NumberPicker) findViewById(R.id.radius);
        radius.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        radius.setValue(settings.getInt(getString(R.string.settings_save_key_radius), 0));

        zapisz = (Button) findViewById(R.id.button);
        zapisz.setOnClickListener(this);

        //Boczne menu
        db = new SQLiteHandler(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new MenuAdapter(this, db);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, Drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        Drawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        NumberPicker np = (NumberPicker) findViewById(R.id.radius);
        np.setMaxValue(5);
        np.setMinValue(1);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                //backToMain();
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        CheckBox notification_sound = (CheckBox) findViewById(R.id.notification_sound);
        CheckBox notification_vibrate = (CheckBox) findViewById(R.id.notification_vibrate);
        switch (checkedId) {
            case R.id.notification_silent:
                notification_sound.setChecked(false);
                notification_sound.setEnabled(false);
                notification_vibrate.setChecked(false);
                notification_vibrate.setEnabled(false);
                break;
            case R.id.notification_loud:
                notification_sound.setEnabled(true);
                notification_vibrate.setEnabled(true);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(zapisz)) {
            CheckBox internet = (CheckBox) findViewById(R.id.internet);
            RadioButton notification_silent = (RadioButton) findViewById(R.id.notification_silent);
            RadioButton notification_loud = (RadioButton) findViewById(R.id.notification_loud);
            CheckBox notification_sound = (CheckBox) findViewById(R.id.notification_sound);
            CheckBox notification_vibrate = (CheckBox) findViewById(R.id.notification_vibrate);

            Spinner layout = (Spinner) findViewById(R.id.layout);
            NumberPicker radius = (NumberPicker) findViewById(R.id.radius);

            SharedPreferences settings = getSharedPreferences(getString(R.string.settings_save_file), this.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();

            editor.putBoolean(getString(R.string.settings_save_key_transfer), internet.isChecked());

            editor.putInt(getString(R.string.settings_save_key_motive), layout.getSelectedItemPosition());
            editor.putInt(getString(R.string.settings_save_key_radius), radius.getValue());

            editor.putBoolean(getString(R.string.settings_save_key_notification_silent), notification_silent.isChecked());
            editor.putBoolean(getString(R.string.settings_save_key_notification_loud), notification_loud.isChecked());
            editor.putBoolean(getString(R.string.settings_save_key_notification_sound), notification_sound.isChecked());
            editor.putBoolean(getString(R.string.settings_save_key_notification_vibrate), notification_vibrate.isChecked());

            editor.commit();

            String radiusS = "" + radius.getValue();
            updateRadius(radiusS);
            Toast toast = Toast.makeText(this, "Zmiany zostały zapisane.", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    private void updateRadius(final String radius) {
        String tag_string_req = "update_radius_req";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Set radius Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (error) {
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

    @Override
    public void onBackPressed() {
        backToMain();
    }
    private void backToMain() {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
    }
}
