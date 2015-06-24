package pl.mf.zpi.matefinder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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


public class RemindPasswordActivity extends ActionBarActivity {

    private static final String TAG = RemindPasswordActivity.class.getSimpleName();
    private EditText inputLogin;
    private EditText inputMail;
    private EditText inputPassword;
    private EditText inputRepeatPassword;
    private ProgressDialog pDialog;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remind_password);

        Button btnSave = (Button) findViewById(R.id.btnSave);
        Button btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        Button btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkToRegister();
            }
        });

        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkToLogin();
            }
        });

        inputLogin = (EditText) findViewById(R.id.login);
        inputMail = (EditText) findViewById(R.id.mail);
        inputPassword = (EditText) findViewById(R.id.password);
        inputRepeatPassword = (EditText) findViewById(R.id.confirmPassword);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        db = new SQLiteHandler(getApplicationContext());
        pDialog.setMessage("Zmiana hasła...");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_remind_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void changePassword() {
        String login = inputLogin.getText().toString();
        String new_pass = inputPassword.getText().toString();
        String new_pass_rep = inputRepeatPassword.getText().toString();
        String mail = inputMail.getText().toString();

        if (!login.equals("") && !mail.equals("") && !new_pass.equals("") && !new_pass_rep.equals(""))
            if (new_pass.equals(new_pass_rep)) {
                changePassword(login, mail, new_pass);
            } else {
                Toast.makeText(getApplicationContext(), "Wprowadzone hasła nie są takie same.", Toast.LENGTH_LONG).show();
            }
        else {
            Toast.makeText(getApplicationContext(), "Wprowadź wszystkie dane.", Toast.LENGTH_LONG).show();
        }
    }

    private void changePassword(final String login, final String mail, final String password) {
        String tag_string_req = "changePass_req";

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
                        linkToLogin();
                    } else {
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
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "forgot_password");
                params.put("login", login);
                params.put("mail", mail);
                params.put("password", password);

                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Metoda odpowiedzialna za wyswietlanie komunikatu "Zmiana hasla..." po zatwierdzeniu zmian przez uzytkownika.
     */
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    /**
     * Metoda odpowiedzialna za ukrycie komunikatu "Zmiana hasla...", gdy proces aktualizacji zakonczyl sie, to znaczy, gdy dane pomyslnie zostaly przekazane na serwer.
     */
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void linkToRegister() {
        Intent i = new Intent(getApplicationContext(),
                RegisterActivity.class);
        startActivity(i);
        finish();
    }

    private void linkToLogin() {
        Intent i = new Intent(getApplicationContext(),
                LoginActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed(){
        linkToLogin();
    }
}
