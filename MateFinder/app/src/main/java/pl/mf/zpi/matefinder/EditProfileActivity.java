package pl.mf.zpi.matefinder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pl.mf.zpi.matefinder.app.AppConfig;
import pl.mf.zpi.matefinder.app.AppController;
import pl.mf.zpi.matefinder.helper.SQLiteHandler;

public class EditProfileActivity extends ActionBarActivity {

    private static final String TAG = EditProfileActivity.class.getSimpleName();
    private ProgressDialog pDialog;

    private Button btn_update;

    private TextView login;
    private TextView email;
    private TextView phone_number;
    private TextView name;
    private TextView surname;

    private ImageView profile_photo;

    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        login = (TextView)findViewById(R.id.editProfile_login_content);
        email = (TextView)findViewById(R.id.editProfile_email_content);
        phone_number = (TextView)findViewById(R.id.editProfile_phone_content);
        name = (TextView)findViewById(R.id.editProfile_name_content);
        surname = (TextView)findViewById(R.id.editProfile_surname_content);

        profile_photo = (ImageView) findViewById(R.id.editProfile_photo);
        // Change photo button Click Event
        profile_photo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Wybierz zdjęcie."),1);
            }
        });

        TextView change_pass = (TextView) findViewById(R.id.editProfile_changePass_link);
        change_pass.setMovementMethod(LinkMovementMethod.getInstance());
        change_pass.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(EditProfileActivity.this, EditPasswordActivity.class);
                startActivity(intent);
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
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        login.setText(user.get("login"));
        email.setText(user.get("email"));
        phone_number.setText(user.get("phone"));
        name.setText(user.get("name"));
        surname.setText(user.get("surname"));
    }

    private void actionUpdate(){
        String up_login = login.getText().toString();
        String up_email = email.getText().toString();
        String up_phone = phone_number.getText().toString();
        String up_name = name.getText().toString();
        String up_surname = surname.getText().toString();

        updateUserDB(up_login,up_email,up_phone,up_name,up_surname);
    }

    private void updateUserDB(final String login, final String email, final String phone,
                              final String name, final String surname) {
        // Tag used to cancel the request
        String tag_string_req = "update_req";

        pDialog.setMessage("Aktualizowanie informacji...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Update Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully updated in MySQL
                        // Now store the user in sqlite
                        JSONObject user = jObj.getJSONObject("user");
                        String login = user.getString("login");
                        String email = user.getString("email");
                        String phone = user.getString("phone_number");
                        String name = user.getString("name");
                        String surname = user.getString("surname");

                        // Inserting row in users table
                        db.deleteUsers();
                        db.addUser(login, email, phone, name, surname);
                        Toast.makeText(getApplicationContext(),"Zmiany zostały zapisane.", Toast.LENGTH_LONG).show();
                    } else {
                        // Error occurred in registration. Get the error
                        // message
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
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "update");
                params.put("login", login);
                params.put("email", email);
                params.put("phone_number", phone);
                params.put("name", name);
                params.put("surname", surname);

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
