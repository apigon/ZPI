package pl.mf.zpi.matefinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                friendslist.setAdapter(new FriendsAdapter(getActivity().getApplicationContext()));
            }
        }, 5000);
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

class FriendsAdapter extends BaseAdapter{

    SQLiteHandler dbHandler;
    ArrayList<SingleFriend> listaZnajomych;
    Context context;

    public FriendsAdapter(Context c){
        dbHandler = new SQLiteHandler(c);
        List<HashMap<String, String>> friends = dbHandler.getFriendsDetails();
        listaZnajomych = new ArrayList<SingleFriend>();
        for(int i=0;i<friends.size();i++){
            SingleFriend sf = new SingleFriend(friends.get(i).get("login"),friends.get(i).get("photo"));
            listaZnajomych.add(sf);
        }
        context=c;
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
        View row = inflater.inflate(R.layout.friends_list_element,parent,false);
        TextView friendLogin = (TextView) row.findViewById(R.id.textView_friend_login);
        ImageView friendPhoto = (ImageView) row.findViewById(R.id.imageView_friend_photo);
        SingleFriend tmp = listaZnajomych.get(position);
        friendLogin.setText(tmp.friendLogin);
        friendPhoto.setImageBitmap(tmp.friendPhoto);
        return row;
    }
}