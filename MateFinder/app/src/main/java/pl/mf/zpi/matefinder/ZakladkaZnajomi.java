package pl.mf.zpi.matefinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.mf.zpi.matefinder.app.AppConfig;
import pl.mf.zpi.matefinder.app.AppController;
import pl.mf.zpi.matefinder.helper.SQLiteHandler;

/**
 * Created by root on 12.04.15.
 */
public class ZakladkaZnajomi extends Fragment{

    private static final String TAG = ZakladkaZnajomi.class.getSimpleName();
    FriendsAdapter adapter;
    SQLiteHandler db;
    ListView friendslist;
    private Context activity;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.zakladka_znajomi, container, false);
        friendslist = (ListView) v.findViewById(R.id.ListaZnajomych);
        db = new SQLiteHandler(getActivity().getApplicationContext());
        adapter = new FriendsAdapter(getActivity().getApplicationContext(), friendslist, getActivity());//czy tu konieczny jest context jako pierwszy argument?nie wystarczy aktywność?
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        updateFriendsList();
    }

    public void updateFriendsList(){
        friendslist.setAdapter(adapter);
        friendslist.setOnItemClickListener(adapter);
        adapter.getFriendsRequests(getActivity());
    }
}

class SingleFriend {
    String friendLogin;
    Bitmap friendPhoto;
    int id;

    public SingleFriend(String friendLogin,String friendPhoto, int id){
        this.friendLogin = friendLogin;
        getFriendPhoto(friendPhoto);
        Log.d("setting singlefriend","SingleFriend utworzony");
        this.id=id;
    }
    public void setFriendPhoto(Bitmap photo){
        this.friendPhoto = photo;
        Log.d("setting photo","Bitmapa ustawiona");
    }

    public void getFriendPhoto(String friendPhoto){
        String url = "http://156.17.130.212/android_login_api/images/" + friendPhoto;

        ImageRequest ir = new ImageRequest(url, new Response.Listener<Bitmap>(){
            @Override
            public void onResponse(Bitmap bitmap) {
                Log.d("Bitmap", "Bitmap Response: " + bitmap.toString());
                setFriendPhoto(bitmap);
            }
        }, 0, 0, null, null);
        AppController.getInstance().addToRequestQueue(ir, "image_request");
    }

    public int getID(){
        return id;
    }
}

class FriendsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener,
        PopupMenu.OnMenuItemClickListener{

    private static final String TAG = "removeFriend";
    ProgressDialog pDialog;
    SQLiteHandler dbHandler;
    ArrayList<SingleFriend> listaZnajomych;
    Context context;
    private ListView listView;
    int klikniete=0;
    List<HashMap<String, String>> friends;
    private Activity activity;


    public FriendsAdapter(Context c, ListView listView, Activity activity){
        dbHandler = new SQLiteHandler(c);
        // fetching friends from sqlite:
        this.activity=activity;
        friends = dbHandler.getFriendsDetails();
        listaZnajomych = new ArrayList<SingleFriend>();
        for(int i=0;i<friends.size();i++){
            SingleFriend sf = new SingleFriend(friends.get(i).get("login"),friends.get(i).get("photo"), Integer.parseInt(friends.get(i).get("userID")));
            listaZnajomych.add(sf);
        }
        context=c;
        this.listView = listView;
    }

    public void deleteFriendFromAdapter(String login){
        for(int i=0;i<listaZnajomych.size();i++){
            if(listaZnajomych.get(i).friendLogin.equals(login)){
                listaZnajomych.remove(i);
            }
        }
        notifyDataSetChanged();
    }

    public void addFriendToAdapter(String friendid){
        HashMap<String, String> singlefriend = dbHandler.getFriendLoginAndPhoto(friendid);
        SingleFriend singleFriend = new SingleFriend(singlefriend.get("login"),singlefriend.get("photo"),Integer.parseInt(friendid));
        listaZnajomych.add(singleFriend);
        notifyDataSetChanged();
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
        final View row = inflater.inflate(R.layout.friends_list_element,parent,false);
        final TextView friendLogin = (TextView) row.findViewById(R.id.textView_friend_login);
        ImageView friendPhoto = (ImageView) row.findViewById(R.id.imageView_friend_photo);
        final SingleFriend tmp = listaZnajomych.get(position);
        friendLogin.setText(tmp.friendLogin);
        friendPhoto.setImageBitmap(tmp.friendPhoto);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        }, 1000);

        return row;
    }

    public void getFriendsRequests(Context context){
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
                    if(!error){
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
                Toast.makeText(context1,
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                HashMap<String, String> user = dbHandler.getUserDetails();
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
                        dbHandler.deleteFriends();
                        String tempUserID = dbHandler.getUserDetails().get("userID");
                        addFriendsList(tempUserID);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                addFriendToAdapter(user2ID);
                            }
                        },2000);
                        // db.deleteFriends();
                        // addFriendsList(userID); + zaimplementować w tej klasie
                        // sprawdzic w metodzie addfriend w php jaki response ustawiony
                        // dodac add friend do sqlite i do listview
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
                        dbHandler.addFriend(userID, login, email, phone, name, surname, photo, location);
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
                Toast.makeText(context,
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

    private void removeFriend(final String login) {
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
                params.put("userID",user.get("userID"));

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
                String imie, nazwisko, mail, telefon;
                imie = temp.get("name");
                nazwisko = temp.get("surname");
                mail = temp.get("email");
                telefon = temp.get("phone");
                Toast toast = new Toast(this.context);
                toast = Toast.makeText(this.context, "Imię : " + imie + "\nNazwisko : " + nazwisko + "\nEmail : " + mail + "\nTelefon : " + telefon, Toast.LENGTH_LONG);
                toast.show();
                break;
            case R.id.do_grupy:
                Intent intent = new Intent(context, AddFriendToGroupActivity.class);
                intent.putExtra("adapter", 1);
                intent.putExtra("id", listaZnajomych.get(klikniete).getID());
                activity.startActivity(intent);
        }
        return false;
    }
}

/*
       final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        },2000);
 */