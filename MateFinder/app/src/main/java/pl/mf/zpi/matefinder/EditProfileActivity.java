package pl.mf.zpi.matefinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class EditProfileActivity extends ActionBarActivity {

    private Button btn_update;
    private Button btn_change_photo;

    private TextView login;
    private TextView email;
    private TextView phone_number;
    private TextView name;
    private TextView surname;

    private ImageView profile_photo;
    private int reqCode;
    private int resCode;
    private Intent data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        login = (TextView)findViewById(R.id.editProfile_login_content);
        email = (TextView)findViewById(R.id.editProfile_email_content);
        phone_number = (TextView)findViewById(R.id.editProfile_phone_content);
        name = (TextView)findViewById(R.id.editProfile_name_content);
        surname = (TextView)findViewById(R.id.editProfile_surname_content);

        profile_photo = (ImageView) findViewById(R.id.editProfile_photo);

        btn_change_photo = (Button) findViewById(R.id.editProfile_btn_changePhoto);
        // Change photo button Click Event
        btn_change_photo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Wybierz zdjęcie."),1);
            }
        });

        btn_update = (Button) findViewById(R.id.editProfile_btn_accept);
        // Update button Click Event
        btn_update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                actionUpdate();
            }
        });

        updateUserInfo();
    }


    public void onActivityResult(int reqCode, int resCode, Intent data){
        if(resCode == RESULT_OK){
            if(reqCode == 1){
                profile_photo.setImageURI(data.getData());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                backToMain();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Powrót do ekranu głównego
    private void backToMain() {
        // Launching the login activity
        Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateUserInfo(){
        // Shred preferences
        SharedPreferences sharedpreferences = getSharedPreferences
                (LoginActivity.UserPREFERENCES, Context.MODE_PRIVATE);

        String tmp_name = sharedpreferences.getString("name","");
        String tmp_surname = sharedpreferences.getString("surname", "");

        if(tmp_name.equals("null")){
            tmp_name = "";
        }
        if(tmp_surname.equals("null")){
            tmp_surname = "";
        }

        login.setText(sharedpreferences.getString("login", ""));
        email.setText(sharedpreferences.getString("email", ""));
        phone_number.setText(sharedpreferences.getString("phone_number", ""));
        name.setText(tmp_name);
        surname.setText(tmp_surname);
    }

    private void actionUpdate(){
        SharedPreferences sharedpreferences = getSharedPreferences
                (LoginActivity.UserPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("login", login.getText().toString());
        editor.putString("email", email.getText().toString());
        editor.putString("phone_number", phone_number.getText().toString());
        editor.putString("name", name.getText().toString());
        editor.putString("surname", surname.getText().toString());
        editor.commit();

        Toast.makeText(getApplicationContext(),"Zmiany zostały zapisane.", Toast.LENGTH_LONG).show();
    }
}
