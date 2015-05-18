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
    private int id;
    private String login, name, surname, email, phone, photoS;
    private Bitmap photo;

    public Friend(int id, String friendLogin,String friendPhoto){
        this.login = friendLogin;
        this.id = id;
        photoS = friendPhoto;
    }
    public void setPhoto(Bitmap photo){
        this.photo = photo;
    }
    public void getFriendPhoto(){
        String url = "http://156.17.130.212/android_login_api/images/" + photoS;

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

    public int getId(){
        return id;
    }

    public void setName(String name){
        this.name=name;
    }

    public void setSurname(String surname){
        this.surname=surname;
    }

    public void setEmail(String email){
        this.email=email;
    }

    public  void setPhone(String phone){
        this.phone=phone;
    }

    public String getLogin(){
        return login;
    }

    public String getEmail(){
        return email;
    }
}
