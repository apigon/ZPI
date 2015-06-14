package pl.mf.zpi.matefinder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Aktywnosc ktora wyswietla profil wybranego znajomego - przekazywanie danych znajomego z adaptera do tej aktywnosci za pomoca Bundle
 */
public class ShowFriendProfileActivity extends ActionBarActivity {

    Button back;
    TextView imie,nazwisko,frlogin,telefon,mail;
    ImageView zdjecie;
    Bitmap bmp;
    private  Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_friend_profile);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imie = (TextView) findViewById(R.id.showProfile_name_content);
        nazwisko = (TextView) findViewById(R.id.showProfile_surname_content);
        frlogin = (TextView) findViewById(R.id.showProfile_login_content);
        telefon = (TextView) findViewById(R.id.showProfile_phone_content);
        mail = (TextView) findViewById(R.id.showProfile_email_content);
        zdjecie = (ImageView) findViewById(R.id.showProfile_photo);
        Bundle extras = this.getIntent().getExtras();
        if(extras!=null){
            imie.setText(extras.getString("name"));
            nazwisko.setText(extras.getString("surname"));
            frlogin.setText(extras.getString("login"));
            telefon.setText(extras.getString("phone"));
            mail.setText(extras.getString("email"));
            byte[] byteArray = extras.getByteArray("photo");
            bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            zdjecie.setImageBitmap(bmp);
        }
        else{
            Log.d("","brak info w bundlu");
        }
        back = (Button) findViewById(R.id.showProfile_btn_accept);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_friend_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                //backToMain();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
