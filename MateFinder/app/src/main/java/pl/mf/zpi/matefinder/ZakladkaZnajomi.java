package pl.mf.zpi.matefinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import pl.mf.zpi.matefinder.app.AppConfig;
import pl.mf.zpi.matefinder.app.AppController;
import pl.mf.zpi.matefinder.helper.SQLiteHandler;

/**
 * Created by root on 12.04.15.
 */

/**
 * Klasa reprezentujaca fragment Znajomi na ekranie glownym aplikacji
 */
public class ZakladkaZnajomi extends Fragment {

    private static final String TAG = ZakladkaZnajomi.class.getSimpleName();
    FriendsAdapter adapter;
    SQLiteHandler db;
    ListView friendslist;
    private Context activity;
    private Timer refresh_friends_timer;
    private TimerTask refresh_friends;
    public static boolean new_accepted_req;
    public static boolean new_req;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.zakladka_znajomi, container, false);
        friendslist = (ListView) v.findViewById(R.id.ListaZnajomych);
        db = new SQLiteHandler(getActivity().getApplicationContext());
        adapter = new FriendsAdapter(getActivity().getApplicationContext(), friendslist, getActivity());//czy tu konieczny jest context jako pierwszy argument?nie wystarczy aktywność?
        if (new_accepted_req == true) {
            new_accepted_req = false;
        }
        if (new_req == true) {
            new_req = false;
        }
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateFriendsList();
        refreshFriendsList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        refresh_friends_timer.cancel();
        refresh_friends_timer = null;
        refresh_friends.cancel();
        refresh_friends = null;
    }

    /**
     * Metoda odswiezajaca adapter znajomych
     */
    public void updateFriendsList() {
        friendslist.setAdapter(adapter);
        friendslist.setOnItemClickListener(adapter);
        if (adapter.connChecker())
            //    getFriendsRequests();
            getFriendsRequests(getActivity(), adapter);
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

    public void onBackPressed() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack();
    }

    /**
     * Metoda realizujaca odbieranie i przetwarzania zaproszen do grona znajomych dla danego uzytkownika
     * @param context - kontekst w ktorym dziala metoda
     * @param a - adapter dla listy znajomych
     */
    public void getFriendsRequests(Context context, final FriendsAdapter a) {
        final Context context1 = context;
        String tag_string_req = "req_getFriendsRequests";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Getting friends requests Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        JSONArray user = jObj.getJSONArray("messages");
                        for (int i = 0; i < user.length(); i++) {
                            // user successfully logged in
                            JSONObject u = user.getJSONObject(i);
                            final String requestID = u.getString(("messageID"));
                            final String userID = u.getString("userID");
                            String content = u.getString("content");
                            // Wyświetlanie dialogów
                            new AlertDialog.Builder(context1)
                                    .setTitle("Zaproszenie do grona znajomych")
                                    .setMessage(content)
                                    .setPositiveButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            a.removeFriendRequest(requestID);
                                        }
                                    })
                                    .setNegativeButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            a.addFriend(requestID, userID);
                                            final Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    adapter = new FriendsAdapter(getActivity().getApplicationContext(), friendslist, getActivity());
                                                    friendslist.setAdapter(adapter);
                                                    friendslist.setOnItemClickListener(adapter);
                                                }
                                            }, 1000);
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
                Toast.makeText(context1,
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

    /**
     * Metoda odswiezajaca liste znajomych
     */
    public void refreshFriendsList() {
        final Handler h = new Handler();
        refresh_friends_timer = new Timer();
        refresh_friends = new TimerTask() {
            @Override
            public void run() {
                h.post(new Runnable() {
                    public void run() {
                        Log.d("Refresh Friends", "Odświeżanie listy znajomych");
                        if (new_accepted_req == true) {
                            try {
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter = new FriendsAdapter(getActivity().getApplicationContext(), friendslist, getActivity());
                                        friendslist.setAdapter(adapter);
                                        friendslist.setOnItemClickListener(adapter);
                                    }
                                }, 1000);
                                new_accepted_req = false;
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                            }
                        }


                        getFriendsRequests(getActivity(), adapter);
                        new_req = false;

                    }
                });
            }

            ;
        };
        refresh_friends_timer.schedule(refresh_friends, 0, 60000);
    }

}

/**
 * Klasa reprezentujaca obiekt znajomego
 */
class SingleFriend {
    String friendLogin;
    Bitmap friendPhoto;
    int id;

    public SingleFriend(String friendLogin, String friendPhoto, int id) {
        this.friendLogin = friendLogin;
        getFriendPhoto(friendPhoto);
        Log.d("setting singlefriend", "SingleFriend utworzony");
        this.id = id;
    }

    public void setFriendPhoto(Bitmap photo) {
        this.friendPhoto = photo;
        Log.d("setting photo", "Bitmapa ustawiona");
    }

    public void getFriendPhoto(String friendPhoto) {
        String url = "http://156.17.130.212/android_login_api/images/" + friendPhoto;

        ImageRequest ir = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                Log.d("Bitmap", "Bitmap Response: " + bitmap.toString());
                setFriendPhoto(bitmap);
            }
        }, 0, 0, null, null);
        AppController.getInstance().addToRequestQueue(ir, "image_request");
    }

    public int getID() {
        return id;
    }
}

/**
 * Klasa reprezentujaca adapter dla listy znajomych
 */
class FriendsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener,
        PopupMenu.OnMenuItemClickListener {

    private static final String TAG = "removeFriend";
    ProgressDialog pDialog;
    SQLiteHandler dbHandler;
    ArrayList<SingleFriend> listaZnajomych;
    Context context;
    private ListView listView;
    int klikniete = 0;
    List<HashMap<String, String>> friends;
    private Activity activity;


    /**
     * Konstruktor adaptera dla listy znajomych
     * @param c - kontekst aplikacji w ktorym wywolujemy konstruktor
     * @param listView - lista znajomych
     * @param activity - aktywnosc w ktorej wywolujemy konstruktor
     */
    public FriendsAdapter(Context c, ListView listView, Activity activity) {
        dbHandler = new SQLiteHandler(c);
        // fetching friends from sqlite:
        this.activity = activity;
        friends = dbHandler.getFriendsDetails();
        listaZnajomych = new ArrayList<SingleFriend>();
        for (int i = 0; i < friends.size(); i++) {
            final SingleFriend sf = new SingleFriend(friends.get(i).get("login"), friends.get(i).get("photo"), Integer.parseInt(friends.get(i).get("userID")));
            listaZnajomych.add(sf);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("tworzenie znajomego", "Nowy znajomy został utworzony: " + sf.toString());
                    notifyDataSetChanged();
                }
            }, 1000);
        }
        context = c;
        this.listView = listView;
    }

    /**
     * Metoda usuwajaca danego znajomego z adaptera
     * @param login - login usuwanego znajomego
     */
    public void deleteFriendFromAdapter(String login) {
        for (int i = 0; i < listaZnajomych.size(); i++) {
            if (listaZnajomych.get(i).friendLogin.equals(login)) {
                listaZnajomych.remove(i);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Metoda dodawajaca znajomego do adaptera
     * @param friendid - id dodawanego znajomego
     */
    public void addFriendToAdapter(String friendid) {
        HashMap<String, String> singlefriend = dbHandler.getFriendLoginAndPhoto(friendid);
        final SingleFriend singleFriend = new SingleFriend(singlefriend.get("login"), singlefriend.get("photo"), Integer.parseInt(friendid));
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listaZnajomych.add(singleFriend);
            }
        }, 1000);
        notifyDataSetChanged();
        /*
        final Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                Collections.sort(listaZnajomych, new Comparator<SingleFriend>() {
                    public int compare(SingleFriend result1, SingleFriend result2) {
                        return result1.friendLogin.compareTo(result2.friendLogin);
                    }
                });
            }
        }, 2000);
        notifyDataSetChanged();
        */
    }

    @Override
    public int getCount() {
        return listaZnajomych.size();
    }

    @Override
    public Object getItem(int position) {
        return listaZnajomych.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View row = inflater.inflate(R.layout.friends_list_element, parent, false);
        final TextView friendLogin = (TextView) row.findViewById(R.id.textView_friend_login);
        ImageView friendPhoto = (ImageView) row.findViewById(R.id.imageView_friend_photo);
        final SingleFriend tmp = listaZnajomych.get(position);
        friendLogin.setText(tmp.friendLogin);
        friendPhoto.setImageBitmap(tmp.friendPhoto);

        notifyDataSetChanged();
        return row;
    }

    /**
     * Metoda dodawajaca znajomego do lokalnej bazy danych SQLite
     * @param requestID - id zalogowanego uzytkownika
     * @param user2ID - id dodawanego uzytkownika
     */
    public void addFriend(final String requestID, final String user2ID) {
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
                        dbHandler.deleteFriends();
                        String tempUserID = dbHandler.getUserDetails().get("userID");
                        addFriendsList(tempUserID, user2ID);
                        Toast.makeText(context,
                                "Użytkownik został dodany do grona znajomych.", Toast.LENGTH_LONG).show();
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(context,
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
                Toast.makeText(context,
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                HashMap<String, String> user = dbHandler.getUserDetails();
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

    /**
     * Metoda odrzucajaca zaproszenie do znajomych
     * @param requestID - id powiadomienia
     */
    public void removeFriendRequest(final String requestID) {
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
                        Toast.makeText(context,
                                "Zaproszenie zostało odrzucone.", Toast.LENGTH_LONG).show();
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(context,
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
                Toast.makeText(context,
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                HashMap<String, String> user = dbHandler.getUserDetails();
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "removeFriendRequest");
                params.put("requestID", requestID);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Metoda dodajaca znajomego do bazy sqlite - pobieranie danych znajomego z serwera
     * @param userID - id zalogowanego użytkownika
     * @param user2ID - id użytkownika dodawanego do znajomych
     */

    public void addFriendsList(final String userID, final String user2ID) {
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
                        dbHandler.addFriend(userID, login, email, phone, name, surname, photo, location);
                        //     }
                   /*} else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }*/
                    }
                    addFriendToAdapter(user2ID);
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Getting friends list ERROR: " + error.getMessage());
                Toast.makeText(context,
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "friends");
                params.put("userID", userID);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Metoda usuwajaca uzytkownika ze znajomych
     * @param login
     */
    public void removeFriend(final String login) {
        // Tag used to cancel the request
        String tag_string_req = "req_remove_friend";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                SQLiteHandler db = new SQLiteHandler(context);
                db.removeFriend(login);
                deleteFriendFromAdapter(login);
                Log.d(TAG, "Removing response : " + response.toString());
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Removing friend ERROR: " + error.getMessage());
                Toast.makeText(context,
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                SQLiteHandler db = new SQLiteHandler(context);
                HashMap<String, String> user = db.getUserDetails();
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "removeFriend");
                params.put("login", login);
                params.put("userID", user.get("userID"));

                return params;
            }

        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        klikniete = position;
        PopupMenu menu = new PopupMenu(context, listView.getChildAt(position));
        menu.getMenuInflater().inflate(R.menu.friend_popup_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(this);
        menu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.usun:
                removeFriend(listaZnajomych.get(klikniete).friendLogin);
                Toast.makeText(context, "Usunięto użytkownika ze znajomych", Toast.LENGTH_LONG).show();
                break;
            case R.id.pokazDane:
                HashMap<String, String> temp = friends.get(klikniete);
                String login = listaZnajomych.get(klikniete).friendLogin;
                Bitmap photo = listaZnajomych.get(klikniete).friendPhoto;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                String imie, nazwisko, mail, telefon;
                imie = temp.get("name");
                nazwisko = temp.get("surname");
                mail = temp.get("email");
                telefon = temp.get("phone");
                Intent showProfile = new Intent(this.context, ShowFriendProfileActivity.class);
                showProfile.putExtra("name", imie);
                showProfile.putExtra("surname", nazwisko);
                showProfile.putExtra("email", mail);
                showProfile.putExtra("phone", telefon);
                showProfile.putExtra("login", login);
                showProfile.putExtra("photo", byteArray);
                showProfile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(showProfile);
                break;
            case R.id.do_grupy:
                Intent intent = new Intent(context, AddFriendToGroupActivity.class);
                intent.putExtra("adapter", 1);
                intent.putExtra("id", listaZnajomych.get(klikniete).getID());
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.wyznaczTrase:
                if (checkLocalizationOn())
                    getMyFriendsLocation("Znajomi");
                else {
                    Toast.makeText(context, "Proszę włączyć udostępnianie lokalizacji!", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * Metoda pobierajaca aktualna lokalizacje znajomego
     * @param userID -  id szukanego znajomego
     * @return
     */
    public LatLng getFriendLocation(int userID) {
        dbHandler = new SQLiteHandler(context);
        HashMap<String, String> user = dbHandler.getFriendLocation(userID);
        LatLng loc = null;
        if (user != null) {
            String lat = user.get("lat");
            String lng = user.get("lng");
            if (lat != null && lng != null && !lat.equals("null") && !lng.equals("null")) {
                double friendLat = Double.parseDouble(lat);
                double friendLng = Double.parseDouble(lng);
                loc = new LatLng(friendLat, friendLng);
            }

        }

        return loc;
    }

    /**
     * Funkcja przechodzaca do aktywnosci wyswietlania mapy
     */
    public void goToMapActivity() {
        HashMap<String, String> list = friends.get(klikniete);
        String friendID = list.get("location");
        LatLng friendLoc = getFriendLocation(Integer.parseInt(friendID));
        if (friendLoc != null) {
            //Bundle args = new Bundle();
            //args.putParcelable("friendLoc", friendLoc);
            Intent mapIntent = new Intent(context, MapsActivity.class);
            mapIntent.putExtra("friendID", friendID);
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //mapIntent.putExtra("id", listaZnajomych.get(klikniete).getID());
            context.startActivity(mapIntent);
        } else
            Toast.makeText(context, "Użytkownik nie jest aktywny", Toast.LENGTH_LONG).show();
    }

    /**
     * Metoda sprawdzajaca czy uzytkownik jest aktywny
     * @return
     */
    private boolean checkLocalizationOn() {
        SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.settings_save_file), Context.MODE_PRIVATE);
        return settings.getBoolean(context.getString(R.string.settings_save_key_visible_localization), true);
    }

    /**
     * Metoda pobierajaca lokalizacje znajomych z danej grupy
     * @param groupName -  nazwa szukanej grupy
     */
    private void getMyFriendsLocation(final String groupName) {
        dbHandler = new SQLiteHandler(context);
        // final String [] friends = getMyFriendsId();

        HashMap<String, String> user = dbHandler.getUserDetails();
        final String userId = user.get("userID");
        String tag_string_req = "update_req";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Update Response: " + response.toString());


                try {
                    dbHandler.deleteFriendsLocations();
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

                            dbHandler.addFriendLocation(location, login, lat, lng);
                        }
                        goToMapActivity();


                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Friends Locations Error: " + error.getMessage());
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();

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

    public boolean connChecker() {
        boolean conn_ok = false;
        SharedPreferences settings = this.activity.getSharedPreferences(this.activity.getString(R.string.settings_save_file), this.activity.MODE_PRIVATE);
        boolean transfer = settings.getBoolean(this.activity.getString(R.string.settings_save_key_transfer), true);
        Boolean visible = settings.getBoolean(this.activity.getString(R.string.settings_save_key_visible_localization), true);
        ConnectivityManager connManager = (ConnectivityManager) this.activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo internet = connManager.getActiveNetworkInfo();
        Log.d(TAG, "Shared transfer: " + connManager.getActiveNetworkInfo());
        if (visible == true && (transfer == false && internet != null && internet.isConnected() || transfer == true && mWifi.isConnected())) {
            conn_ok = true;
        }
        return conn_ok;
    }
}

