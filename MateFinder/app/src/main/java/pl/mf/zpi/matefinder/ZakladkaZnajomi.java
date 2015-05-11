package pl.mf.zpi.matefinder;

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
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;

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

    ListView friendslist;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.zakladka_znajomi, container, false);
        friendslist = (ListView) v.findViewById(R.id.ListaZnajomych);
        FriendsAdapter adapter = new FriendsAdapter(getActivity().getApplicationContext(), friendslist);
        friendslist.setAdapter(adapter);
        friendslist.setOnItemClickListener(adapter);
        return v;
    }


}

class SingleFriend {
    String friendLogin;
    Bitmap friendPhoto;
    public SingleFriend(String friendLogin,String friendPhoto){
        this.friendLogin = friendLogin;
        getFriendPhoto(friendPhoto);
        Log.d("setting singlefriend","SingleFriend utworzony");
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


    public FriendsAdapter(Context c, ListView listView){
        dbHandler = new SQLiteHandler(c);
        List<HashMap<String, String>> friends = dbHandler.getFriendsDetails();
        listaZnajomych = new ArrayList<SingleFriend>();
        for(int i=0;i<friends.size();i++){
            SingleFriend sf = new SingleFriend(friends.get(i).get("login"),friends.get(i).get("photo"));
            listaZnajomych.add(sf);
        }
        context=c;
        this.listView = listView;
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
        // ImageButton removeFriendButton = (ImageButton) row.findViewById(R.id.removeFriendButton);
        final SingleFriend tmp = listaZnajomych.get(position);
        friendLogin.setText(tmp.friendLogin);
        friendPhoto.setImageBitmap(tmp.friendPhoto);
        // removeFriendButton.setImageResource(R.drawable.removeFriend);
        /*
        removeFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               new AlertDialog.Builder(v.getContext())
                        .setTitle("Usuwanie użytkownika ze znajomych")
                        .setMessage("Na pewno chcesz usunąć tego użytkownika ze znajomych ?")
                        .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                SQLiteHandler db = new SQLiteHandler(context);
                                db.removeFriend(tmp.friendLogin);
                            }
                        })
                        .setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        */
        return row;
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
        removeFriend(listaZnajomych.get(klikniete).friendLogin);
        Toast.makeText(context,"Usunięto użytkownika ze znajomych",Toast.LENGTH_LONG).show();
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