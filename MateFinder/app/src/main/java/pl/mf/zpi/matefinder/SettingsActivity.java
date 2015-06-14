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
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import pl.mf.zpi.matefinder.helper.SessionManager;

/**
 * Aktywność odpowiadająca personalizowanie ustawień aplikacji według upodobań użytkownika. Wszelkie ustawienia przechowywane są przy pomocy SharedPreferences. Jedynie promień wyszukiwania użytkowników wysyłany jest na serwer.
 */
public class SettingsActivity extends ActionBarActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private Button zapisz;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DrawerLayout Drawer;

    private ActionBarDrawerToggle mDrawerToggle;
    private SQLiteHandler db;

    private SessionManager session;
    private boolean drawerOpened;

    /**
     * Metoda wywoływana przy tworzeniu aktywności, zawiera inicjalizację wszelkich potrzebnych parametrów, widoków, bocznego menu.
     *
     * @param savedInstanceState parametr przechowujący poprzedni stan, w którym znajdowała się aktywność przed jej zakończeniem; na jego podstawie odtwarzana jest poprzednia konfiguracja, np. orientacja ekranu
     */
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
        String[] displayed_values = {"100m", "200m", "500m", "1km", "2km", "5km"};
        radius.setMaxValue(displayed_values.length - 1);
        radius.setMinValue(0);
        radius.setDisplayedValues(displayed_values);
        radius.setOrientation(LinearLayout.HORIZONTAL);
        radius.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        radius.setValue(settings.getInt(getString(R.string.settings_save_key_radius), 0));

        zapisz = (Button) findViewById(R.id.button);
        zapisz.setOnClickListener(this);


        //wyloguj
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }


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
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
                drawerOpened = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
                drawerOpened = false;
            }
        };
        Drawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    /**
     * Metoda odpowiedzialna za przypisanie odpowiedniego, wyspecjalizowanego widoku menu do danej aktywności.
     *
     * @param menu parametr, do którego przypisywany jest odpowiedni widok
     * @return po dokonaniu przypisania zawsze zwraca wartość 'true'
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    /**
     * Metoda odpowiedzialna za przypisanie funkcjonalności, odpowiednich zachowań aplikacji do poszczególnych pozycji w menu.
     *
     * @param item wybrana pozycja, do której przypisywana jest określona funkcjonalność
     * @return zwraca wartość TRUE po przypisaniu funkcjonalności do danej pozycji Menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logoutUser();
                Toast.makeText(this, "Wylogowano!", Toast.LENGTH_SHORT).show();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Metoda odpowiedzialna za aktywację lub dezaktywację danych CheckBox'ów z ustawieniami powiadomień, w zależności od wybranej opcji z RadioGroup. Zaznaczony jest RadioButton z profilem cichym, CheckBox'y 'Dźwięk' oraz 'Wibracja' są nieaktywne.
     *
     * @param group     grupa, do której należą dane RadioButton'y
     * @param checkedId identyfikator zaznaczonego RadioButton'a
     */
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

    /**
     * Metoda odpowiedzialna za zapis zmodyfikowanych ustawień użytkownika, po zatwierdzeniu ich przez niego. Wszelkie ustawienia zostają zapisane w SharedPreferences. Promień wyszukiwania użytkowników zostaje wysłany na serwer.
     *
     * @param v widok z wybraną opcją 'Zapisz'
     */
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

            float[] radius_values = {(float) 0.1, (float) 0.2, (float) 0.5, 1, 2, 5};
            String radiusS = "" + radius_values[radius.getValue()];
            updateRadius(radiusS);
            Toast toast = Toast.makeText(this, "Zmiany zostały zapisane.", Toast.LENGTH_SHORT);
            toast.show();
            //finish();
        }
    }

    /**
     * Metoda odpowiedzialna za wylogowania użytkownika z aplikacji.
     */
    private void logoutUser() {
        session.setLogin(false);

        MainActivity.doAsynchronousTask.cancel();
        MainActivity.doAsynchronousTask = null;

        db.deleteFriends();
        db.deleteGroups();
        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); //tylko tutaj finish() ma uzasadnienie !!!
    }

    /**
     * Metoda odpowiedzialna za ustanowienie połączenia z serwerem oraz wysłanie do bazy danych umieszczonej na nim wartości promienia wyszukiwania użytkowników.
     *
     * @param radius wartość promienia wyszukiwania użytkowników
     */
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

    /**
     * Metoda odpowiedzialna za przypisanie określonego zachowania dla przycisku 'Wstecz'.
     */
    @Override
    public void onBackPressed() {
        if(!drawerOpened)
            backToMain();
        else
            hideMenu();
    }

    /**
     * Metoda odpowiedzialna za przejście z danej aktywności do aktywności głównej - tutaj ekranu znajomych.
     */
    private void backToMain() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void hideMenu(){
        Drawer.closeDrawers();
    }
}
