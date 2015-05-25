package pl.mf.zpi.matefinder;

/**
 * Created by root on 22.03.15.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

    public static boolean messages;
    private static boolean async = false;

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

        getFriendsRequests();

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

        if(!async) {
           // new MessageAsync(MainActivity.this).execute(); //KOD ADAMA ZAKOMENTOWANY NA PROBE!
            async = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_notification);
        if(messages)
            item.setIcon(R.drawable.ic_action_new_email);
        else
            item.setIcon(R.drawable.ic_action_email);

        return super.onPrepareOptionsMenu(menu);
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
        async = false;
        MessageAsync.running = false;

        // Launching the login activity
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

    private void openMessages(){
        Intent intent = new Intent(this, MessageActivity.class);
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
                openMessages();
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
                SharedPreferences settings = getSharedPreferences(getString(R.string.settings_save_file), MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                Boolean visible = !settings.getBoolean(getString(R.string.settings_save_key_visible_localization), true);
                editor.putBoolean(getString(R.string.settings_save_key_visible_localization), visible);
                editor.commit();
                toast = Toast.makeText(this,visible?"Lokalizacja bedzie wyświetlana":"Lokalizacja nie będzie wyświetlana", Toast.LENGTH_SHORT);
                toast.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getFriendsRequests(){
        String tag_string_req = "req_getFriendsRequests";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Getting friends requests Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if(!error){
                        JSONArray user = jObj.getJSONArray("messages");
                        for (int i = 0; i < user.length(); i++) {
                            // user successfully logged in
                            JSONObject u = user.getJSONObject(i);
                            final String requestID = u.getString(("messageID"));
                            final String userID = u.getString("userID");
                            String content = u.getString("content");
                            // Wyświetlanie dialogów
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Zaproszenie do grona znajomych")
                                    .setMessage(content)
                                    .setPositiveButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            removeFriendRequest(requestID);
                                        }
                                    })
                                    .setNegativeButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            addFriend(requestID, userID);
                                        }
                                    })
                                    .show();
                        }
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login ERROR: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                HashMap<String, String> user = db.getUserDetails();
                String userID = user.get("userID");
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "getMessages");
                params.put("userID", userID);
                params.put("type", "0");
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void addFriend(final String requestID, final String user2ID){
        String tag_string_req = "req_addFriend";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Adding friend Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        db.deleteFriends();
                        String tempUserID = db.getUserDetails().get("userID");
                        addFriendsList(tempUserID);

                        // db.deleteFriends();
                        // addFriendsList(userID); + zaimplementować w tej klasie
                        // sprawdzic w metodzie addfriend w php jaki response ustawiony
                        // dodac add friend do sqlite i do listview
                        Toast.makeText(getApplicationContext(),
                                "Użytkownik został dodany do grona znajomych.", Toast.LENGTH_LONG).show();
                    } else {
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
                Log.e(TAG, "Adding friend ERROR: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                HashMap<String, String> user = db.getUserDetails();
                String user1ID = user.get("userID");
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "addFriend");
                params.put("user1ID", user1ID);
                params.put("user2ID", user2ID);
                params.put("requestID", requestID);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void removeFriendRequest(final String requestID){
        String tag_string_req = "req_removeFriendRequest";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Removing friend request Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        Toast.makeText(getApplicationContext(),
                                "Zaproszenie zostało odrzucone.", Toast.LENGTH_LONG).show();
                    } else {
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
                Log.e(TAG, "Removing friend request ERROR: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                HashMap<String, String> user = db.getUserDetails();
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "removeFriendRequest");
                params.put("requestID", requestID);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void addFriendsList(final String userID) {
        // Tag used to cancel the request
        String tag_string_req = "req_getFriends";

        // pDialog.setMessage("Aktualizowanie listy znajomych...");
        // showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Getting friends list Response: " + response.toString());
                // hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    //    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    //   if (!error) {
                    // JSONObject users = jObj.getJSONObject("users");
                    JSONArray user = jObj.getJSONArray("users");
                    for (int i = 0; i < user.length(); i++) {
                        // user successfully logged in
                        JSONObject u = user.getJSONObject(i);
                        String userID = u.getString("userID");
                        String login = u.getString("login");
                        String email = u.getString("email");
                        String phone = u.getString("phone_number");
                        String name = u.getString("name");
                        String surname = u.getString("surname");
                        String photo = u.getString("photo");
                        String location = u.getString("location");
                        //   String photo = user.getJSONArray(Integer.toString(i)).getString("photo");
                        //  String location = user.getJSONArray(Integer.toString(i)).getString("location");
                        // Inserting row in users table
                        db.addFriend(userID, login, email, phone, name, surname, photo, location);
                        //     }
                   /*} else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }*/ }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Getting friends list ERROR: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "friends");
                params.put("userID",userID );
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}
