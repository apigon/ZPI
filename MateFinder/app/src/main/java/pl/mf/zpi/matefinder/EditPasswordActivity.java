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

/**
 * Aktywność odpowiadajaca za zmiane hasla uzytkownika zalogowanego w aplikacji.
 */
public class EditPasswordActivity extends ActionBarActivity {

    private static final String TAG = EditPasswordActivity.class.getSimpleName();
    private ProgressDialog pDialog;

    private EditText old_password;
    private EditText new_password;
    private EditText new_password_rep;

    private Button btn_change;

    private SQLiteHandler db;

    /**
     * Metoda wywolywana przy tworzeniu aktywnosci, zawiera inicjalizacje wszelkich potrzebnych parametrow, widokow, bocznego menu.
     *
     * @param savedInstanceState parametr przechowujacy poprzedni stan, w ktorym znajdowala się aktywnosc przed jej zakonczeniem; na jego podstawie odtwarzana jest poprzednia konfiguracja, np. orientacja ekranu
     */
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

    /**
     * Metoda odpowiedzialna za przypisanie odpowiedniego, wyspecjalizowanego widoku menu do danej aktywnosci.
     *
     * @param menu parametr, do którego przypisywany jest odpowiedni widok
     * @return po dokonaniu przypisania zawsze zwraca wartość TRUE
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_password, menu);
        return true;
    }

    /**
     * Metoda odpowiedzialna za przypisanie funkcjonalnosci, odpowiednich zachowan aplikacji do poszczegolnych pozycji w menu.
     *
     * @param item wybrana pozycja, do ktorej przypisywana jest okreslona funkcjonalnosc
     * @return zwraca wartosc TRUE po przypisaniu funkcjonalnosci do danej pozycji menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backToEditProfile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Metoda odpowiedzialna za powrot do aktywnosci edycji profilu po zakonczeniu biezacej aktywnosci.
     */
    private void backToEditProfile() {
        Intent intent = new Intent(EditPasswordActivity.this, EditProfileActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Metoda pobierajaca dane uzytkownika z lokalnej bazy danych oraz dane z pol tekstowych przeznaczonych na stare oraz nowe hasla. Po pobraniu owych danych oraz porownaniu ich zgodnosci, haslo uzytkownika zostaje zmienione lub zostaje wyswietlony komunikat o bledzie.
     */
    private void changePassword() {
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

    /**
     * Metoda zapisujaca nowe haslo w bazie danych na serwerze.
     *
     * @param login    login uzytkownika
     * @param old_pass stare haslo uzytkownika
     * @param new_pass nowe haslo uzytkownika
     */
    private void changePasswordDB(final String login, final String old_pass, final String new_pass) {
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
}
