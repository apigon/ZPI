package pl.mf.zpi.matefinder;

import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import javax.xml.namespace.NamespaceContext;

import pl.mf.zpi.matefinder.app.AppController;

/**
 * Created by root on 06.05.15.
 */
public class Friend {
    String login, name, surname, email, phone;
    Bitmap photo;
    public Friend(String friendLogin,String friendPhoto, String name, String surname, String phone){
        this.login = friendLogin;
        this.name=name;
        this.surname=surname;
        this.phone=phone;
        getFriendPhoto(friendPhoto);
        Log.d("setting singlefriend", "SingleFriend utworzony");
    }
    public void setPhoto(Bitmap photo){
        this.photo = photo;
        Log.d("setting photo","Bitmapa ustawiona");
    }
    public void getFriendPhoto(String friendPhoto){
        String url = "http://156.17.130.212/android_login_api/images/" + friendPhoto;

        ImageRequest ir = new ImageRequest(url, new Response.Listener<Bitmap>(){
            @Override
            public void onResponse(Bitmap bitmap) {
                Log.d("Bitmap", "Bitmap Response: " + bitmap.toString());
                setPhoto(bitmap);
            }
        }, 0, 0, null, null);
        AppController.getInstance().addToRequestQueue(ir, "image_request");
    }

    public String getName(){
        return name;
    }

    public String getSurname(){
        return surname;
    }

    public String getPhone(){
        return phone;
    }
}
