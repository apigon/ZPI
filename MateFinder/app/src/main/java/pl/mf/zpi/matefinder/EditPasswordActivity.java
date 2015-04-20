package pl.mf.zpi.matefinder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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


public class EditPasswordActivity extends ActionBarActivity {

    private static final String TAG = EditPasswordActivity.class.getSimpleName();
    private ProgressDialog pDialog;

    private EditText old_password;
    private EditText new_password;
    private EditText new_password_rep;

    private Button btn_change;

    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        old_password = (EditText) findViewById(R.id.editProfile_oldPass_content);
        new_password = (EditText) findViewById(R.id.editProfile_newPass_content);
        new_password_rep = (EditText) findViewById(R.id.editProfile_newPassRep_content);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        btn_change = (Button) findViewById(R.id.editPassword_btn_accept);
        // Update button Click Event
        btn_change.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                changePassword();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                backToEditProfile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Powrót do edycji profilu
    private void backToEditProfile() {
        // Launching the login activity
        Intent intent = new Intent(EditPasswordActivity.this, EditProfileActivity.class);
        startActivity(intent);
        finish();
    }

    private void changePassword() {
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String login = user.get("login");
        String old_pass = old_password.getText().toString();
        String new_pass = new_password.getText().toString();
        String new_pass_rep = new_password_rep.getText().toString();

        if (new_pass.equals(new_pass_rep)) {
            changePasswordDB(login, old_pass, new_pass);
        } else {
            Toast.makeText(getApplicationContext(), "Wprowadzone hasła nie są takie same.", Toast.LENGTH_LONG).show();
        }
    }

    private void changePasswordDB(final String login, final String old_pass, final String new_pass) {
        // Tag used to cancel the request
        String tag_string_req = "changePass_req";

        pDialog.setMessage("Zmiana hasła...");
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
                        Toast.makeText(getApplicationContext(), "Hasło zostało zmienione.", Toast.LENGTH_LONG).show();
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
                params.put("tag", "change_password");
                params.put("login", login);
                params.put("old_password", old_pass);
                params.put("new_password", new_pass);

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
