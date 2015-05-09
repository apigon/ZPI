package pl.mf.zpi.matefinder;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.mf.zpi.matefinder.app.AppController;
import pl.mf.zpi.matefinder.helper.SQLiteHandler;

/**
 * Created by root on 12.04.15.
 */
public class ZakladkaZnajomi extends Fragment implements View.OnClickListener{


    ListView friendslist;
    Button showFriends;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.zakladka_znajomi, container, false);
        /*
        showList = (Button) v.findViewById(R.id.buttonPokazZnajomych);
        showList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendslist = (ListView)v.findViewById(R.id.ListaZnajomych);
                friendslist.setAdapter(new FriendsAdapter(getActivity().getApplicationContext()));
            }
        });
        */
        // friendslist = (ListView) v.findViewById(R.id.ListaZnajomych);
        // friendslist.setAdapter(new FriendsAdapter(getActivity().getBaseContext()));
        friendslist = (ListView) v.findViewById(R.id.ListaZnajomych);
        showFriends = (Button) v.findViewById(R.id.buttonPokazZnajomych);
        showFriends.setOnClickListener(this);
        Handler handler = new Handler(); //wait 1 sec than try again set my location
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                friendslist.setAdapter(new FriendsAdapter(getActivity().getApplicationContext()));
                //friendslist.invalidateViews();
                Log.d("tag", "FRAGMENT TOMKA");
            }},1000);

        return v;
    }

    @Override
    public void onClick(View v) {
        //friendslist.setAdapter(new FriendsAdapter(getActivity().getApplicationContext()));
    }
    // @Override

}

class SingleFriend {
    String friendLogin;
    Bitmap friendPhoto;
    // Context c;

    public SingleFriend(String friendLogin,String friendPhoto){
        // this.c=c;
        // ImageView tmp = new ImageView(this.c);
        this.friendLogin = friendLogin;
        // tmp.setTag("http://156.17.130.212/android_login_api/images/" + friendPhoto);
        // new DownloadImagesTask().execute(tmp);
        // this.friendPhoto = getFriendPhoto(friendPhoto);
        DownloadBitmapTask dbt = new DownloadBitmapTask();
        dbt.execute(friendPhoto);
        // String tmp = new DownloadBitmapTask().execute(friendPhoto).toString();


       // byte[] decodedString = Base64.decode(friendPhoto, Base64.URL_SAFE);
        //this.friendPhoto = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);


        // DownloadBitmapTask dbt = new DownloadBitmapTask();
        // this.friendPhoto = dbt.doInBackground(friendPhoto);
        // new DownloadBitmapTask().execute(friendPhoto);
        // DownloadBitmapTask dbt = new DownloadBitmapTask();
        // dbt.execute(this.friendPhoto);
        // this.friendPhoto = getFriendPhoto(friendPhoto);
        // this.friendPhoto = new ImageLoader(c,friendPhoto).doInBackground();
    }
    public Bitmap getFriendPhoto(String friendPhoto){
        final Bitmap[] friendBitmap = new Bitmap[1];
        String url = "http://156.17.130.212/android_login_api/images/" + friendPhoto;
        ImageRequest ir = new ImageRequest(url, new Response.Listener<Bitmap>(){

            @Override
            public void onResponse(Bitmap bitmap) {
                friendBitmap[0] = bitmap;
            }
        }, 0, 0, null, null);

        AppController.getInstance().addToRequestQueue(ir, "image_request");
        return friendBitmap[0];
    }

    public Bitmap getFriendBitmap(String friendPhoto){
        String url_photo = "http://156.17.130.212/android_login_api/images/" + friendPhoto;
        InputStream is = null;
        try {
               URL url = new URL(url_photo);
               HttpURLConnection connection  = (HttpURLConnection) url.openConnection();
               is = connection.getInputStream();
        } catch (IOException e) {
                e.printStackTrace();
        }
        Bitmap img = BitmapFactory.decodeStream(is);
        return img;
        }

    /* private void savePhotoToGallery(final String photo_name) {
        String url = "http://156.17.130.212/android_login_api/images/" + photo_name;
        ImageRequest ir = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                File my_path = new File(directory, "profile.jpg");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(my_path);
                    response.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                    Toast.makeText(getApplicationContext(),
                            "Udało się!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Lipa ;/", Toast.LENGTH_LONG).show();
                }
            }
        }, 0, 0, null, null);

        AppController.getInstance().addToRequestQueue(ir, "image_request");
    }
    */
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

class DownloadBitmapTask extends AsyncTask<String,Void,String>{
    String result;
    Bitmap tmp;
    @Override
    protected String doInBackground(String... params) {
        String url_string = "http://156.17.130.212/android_login_api/images/" + params[0];
        URL url = null;
        try {
            url = new URL(url_string);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            result = convertStreamToString(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

        // tmp = getFriendBitmap(params[0]);
        // return tmp;
    }

    @Override
    protected void onPostExecute(String s) {
        s=result;
    }

    String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public Bitmap getFriendBitmap(String friendPhoto){
        String url_photo = "http://156.17.130.212/android_login_api/images/" + friendPhoto;
        InputStream is = null;
        try {
            URL url = new URL(url_photo);
            HttpURLConnection connection  = (HttpURLConnection) url.openConnection();
            is = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap img = BitmapFactory.decodeStream(is);
        return img;
    }
}