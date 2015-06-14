package pl.mf.zpi.matefinder;
/**
 * Created by root on 22.03.15.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pl.mf.zpi.matefinder.app.AppConfig;
import pl.mf.zpi.matefinder.app.AppController;

public class RegisterActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputLogin;
    private EditText inputEmail;
    private EditText inputPassword;
    private EditText inputRepeatPassword;

    private ProgressDialog pDialog;
    private String login, email, password, phone;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputLogin = (EditText) findViewById(R.id.registerLogin);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        inputRepeatPassword = (EditText) findViewById(R.id.password_repeat);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                login = inputLogin.getText().toString();
                email = inputEmail.getText().toString();
                password = inputPassword.getText().toString();
                String repeatPassword = inputRepeatPassword.getText().toString();

                if (getPhone())
                    if (repeatPassword.equals(password))
                        if (!login.isEmpty() && !email.isEmpty() && !password.isEmpty() && !phone.isEmpty()) {
                            registerUser(login, email, password, phone, "", "");
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Proszę wprowadzić swoje dane!", Toast.LENGTH_LONG)
                                    .show();
                        }
                    else
                        Toast.makeText(getApplicationContext(), "Podane hasła nie są takie same!", Toast.LENGTH_LONG).show();
            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     */
    private void registerUser(final String login, final String email,
                              final String password, final String phone,
                              final String name, final String surname) {
        // Tag used to cancel the request
        String tag_string_req = "register_req";

        pDialog.setMessage("Rejestrowanie...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // Launch login activity
                        Toast.makeText(getApplicationContext(), "Rejestracja powiodła się. Zaloguj się!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(
                                RegisterActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
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
                params.put("tag", "register");
                params.put("login", login);
                params.put("email", email);
                params.put("password", password);
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

    private boolean getPhone() {
        Context mAppContext = getApplicationContext();
        TelephonyManager tMgr = (TelephonyManager) mAppContext.getSystemService(Context.TELEPHONY_SERVICE);
        phone = tMgr.getLine1Number();
        boolean done = true;
        if (phone == null || phone.equals("")) {
            done = false;
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_PHONE);
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.podaj_nr);
            alert.setMessage(R.string.podaj_nr_wiad);
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    phone = input.getText().toString();
                    registerUser(login, email, password, phone, "", "");
                }
            });
            alert.show();
        }
        return done;
    }

}