package pl.mf.zpi.matefinder;

/**
 * Created by root on 22.03.15.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import pl.mf.zpi.matefinder.helper.SQLiteHandler;
import pl.mf.zpi.matefinder.helper.SessionManager;



public class MainActivity extends ActionBarActivity {

    private TextView txtLogin;
    private TextView txtEmail;

    private SQLiteHandler db;
    private SessionManager session;

    private Toolbar toolbar;

    private static boolean location_shared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */

    // Wyloguj
     private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); //tylko tutaj finish() ma uzasadnienie !!!
    }

    // Edytuj profil
    private void editProfile() {
        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
        startActivity(intent);

    }

    // Ustawienia
    private void settings() {
        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        //finish(); NIE DAWAJCIE TEGO FINISH() bo potem przy przycisku powrotu
        // wychodzi z aplikacji zamiast wracac do poprzedniego ekranu!!!
    }
    private void maps() {

        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_logout:
                logoutUser();
                return true;
            case R.id.action_edit_profile:
                editProfile();
                return true;
            case R.id.action_settings:
                maps();
                return true;
            case R.id.action_notification:
                settings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
