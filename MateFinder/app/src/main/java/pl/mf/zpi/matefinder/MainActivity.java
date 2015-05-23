package pl.mf.zpi.matefinder;

/**
 * Created by root on 22.03.15.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pl.mf.zpi.matefinder.app.AppConfig;
import pl.mf.zpi.matefinder.app.AppController;
import pl.mf.zpi.matefinder.helper.SQLiteHandler;
import pl.mf.zpi.matefinder.helper.SessionManager;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView txtLogin;
    private TextView txtEmail;

    private SQLiteHandler db;
    private SessionManager session;

    private Toolbar toolbar;

    private static boolean location_shared;

    private ViewPager pager;
    private ViewPagerAdapter adapter;
    private SlidingTabLayout zakladki;
    private CharSequence tytuly[] = {"Grupy", "Znajomi"};
    private int n = 2;

    private RecyclerView mRecyclerView;                           // Declaring RecyclerView
    private RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    private RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    private DrawerLayout Drawer;                                  // Declaring DrawerLayout

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }


        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size
        db = new SQLiteHandler(getApplicationContext());
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

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), tytuly, n);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        zakladki = (SlidingTabLayout) findViewById(R.id.tabs);
        zakladki.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        zakladki.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.kol3);
            }
        });

        //startService(new Intent(this, MessageService.class));
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
     */

    // Wyloguj
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();
        db.deleteFriends();
        db.deleteGroups();
        db.deleteSettings();

        // Launching the login activity
        //stopService(new Intent(this, MessageService.class));
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); //tylko tutaj finish() ma uzasadnienie !!!
    }

    private void createGroup(){
        Intent intent = new Intent(this, AddGroupActivity.class);
        startActivity(intent);
    }

    private void makeFriend(){
        Intent intent = new Intent (this, AddFriendActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast toast;
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_logout:
                logoutUser();
                toast = Toast.makeText(this, "Wylogowano!", Toast.LENGTH_SHORT);
                toast.show();
                return true;
            case R.id.action_notification:
                toast = Toast.makeText(this, "Przepraszamy, wysyłanie powiadomień jeszcze nie gotowe", Toast.LENGTH_SHORT);
                toast.show();
                return true;
            case R.id.home:
                return true;
            case R.id.action_change_view:
                toast = Toast.makeText(this, "Przepraszamy, zmiana widoku jeszcze nie gotowa", Toast.LENGTH_SHORT);
                toast.show();
                return true;
            case R.id.action_send_notification:
                toast = Toast.makeText(this, "Przepraszamy, wysyłanie powiadomień jeszcze nie gotowe", Toast.LENGTH_SHORT);
                toast.show();
                return true;
            case R.id.action_add_user:
                makeFriend();
                return true;
            case R.id.action_add_group:
                createGroup();
                return true;
            case R.id.action_search:
                toast = Toast.makeText(this, "Przepraszamy, wyszukiwanie znajomych jeszcze nie gotowe", Toast.LENGTH_SHORT);
                toast.show();
                return true;
            case R.id.action_share_location:
                toast = Toast.makeText(this, "Przepraszamy, udostępnianie lokalizacji jeszcze nie gotowe", Toast.LENGTH_SHORT);
                toast.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
