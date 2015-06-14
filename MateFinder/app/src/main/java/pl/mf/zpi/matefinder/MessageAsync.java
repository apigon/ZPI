package pl.mf.zpi.matefinder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pl.mf.zpi.matefinder.app.AppConfig;
import pl.mf.zpi.matefinder.app.AppController;
import pl.mf.zpi.matefinder.helper.SQLiteHandler;
import pl.mf.zpi.matefinder.helper.SessionManager;

/**
 * Klasa umożliwiająca asynchroniczne pobieranie wiadomości z serwera. Łączenie z serwerem odbywa się co 1 minutę.
 */
public class MessageAsync extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = MessageAsync.class.getSimpleName();
    private Context context;

    private SQLiteHandler db;
    private SessionManager session;

    private boolean[] notif_settings;

    /**
     * Konstruktor pobierający dane zalogowanego użytkownika oraz ustawienia dotyczące sposobu otrzymywania powiadomień
     *
     * @param context        kontekst intencji
     * @param notif_settings tablica parametrów, określających ustawienia otrzymywania powiadomień (dźwięk, wibracja, cichy)
     */
    public MessageAsync(Context context, boolean[] notif_settings) {
        Log.d(TAG, "Uruchomiono ASYNC");
        this.context = context;
        db = new SQLiteHandler(context);
        session = new SessionManager(context);
        this.notif_settings = notif_settings;
    }

    /**
     * Metoda odpowiedzialna za pobieranie z serwera nowych wiadomości, zaproszeń do znajomych oraz sprawdzanie, czy dane zaproszenie zostało akceptowane przez innego użytkownika. Jest wykonywana w tle.
     *
     * @param params parametry zadania asynchronicznego; argument konieczny w danej metodzie nadpisywanej po nadklasie
     * @return w każdym wypadku, po wykonaniu wszystkich operacji, zwraca wartość TRUE
     */
    @Override
    protected Boolean doInBackground(Void... params) {
        getMessages();
        getAcceptedRequests();
        getNewRequests();
        return true;
    }

    /**
     * Metoda odpowiedzialna za ustanowienia połączenia z serwerem oraz pobranie wiadomości z bazy danych i zapis ich na urządzeniu. W przypadku pobrania nowych wiadomości zostaje utworzone powiadomienie, zgodne z preferencjami użytkownika.
     */
    private void getMessages() {
        Log.d(TAG, "Sprawdzanie wiadomości...");
        if (session.isLoggedIn()) {
            String tag_string_req = "req_getMessages";
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_REGISTER, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Getting messages Response: " + response.toString());

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {
                            JSONArray user = jObj.getJSONArray("messages");
                            for (int i = 0; i < user.length(); i++) {
                                JSONObject u = user.getJSONObject(i);
                                final String requestID = u.getString(("messageID"));
                                final String authorLogin = u.getString(("authorLogin"));
                                final String content = u.getString("content");

                                db.addMessage(requestID, authorLogin, content);
                                makeNotification();
                            }
                            MainActivity.refreshMenuIcon(db.allMessagesRead());
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Message ERROR: " + error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    HashMap<String, String> user = db.getUserDetails();
                    String userID = user.get("userID");
                    params.put("tag", "getMessages");
                    params.put("userID", userID);
                    params.put("type", "1");
                    return params;
                }
            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    /**
     * Metoda odpowiedzialna za ustanowienie połączenia z serwerem oraz sprawdzenie, czy do danego użytkownika wpłynęło nowe zaproszenie do listy znajomych. W przypadku, gdy takie zaproszenie wpłynęło, zostaje wyświetlany dialog z treścią zaproszenia oraz możliwościami akceptacji, bądź też odrzucenia go.
     */
    private void getNewRequests() {
        Log.d(TAG, "Sprawdzanie potwierdzeń zaproszeń...");
        if (session.isLoggedIn()) {
            String tag_string_req = "req_getNewRequests";
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_REGISTER, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Getting new requests Response: " + response.toString());

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {
                            JSONArray new_req = jObj.getJSONArray("messages");
                            if (new_req.length() > 0) {
                                ZakladkaZnajomi.new_req = true;
                            }
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Message ERROR: " + error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    HashMap<String, String> user = db.getUserDetails();
                    String userID = user.get("userID");
                    params.put("tag", "getMessages");
                    params.put("userID", userID);
                    params.put("type", "0");
                    return params;
                }
            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    /**
     * Metoda odpowiedzialna za ustanowienie połączenia z serwerem oraz sprawdzenie, czy inny użytkownik akceptował zaproszenie do znajomych, wysłane przez danego. W przypadku, gdy tamten akceptował zaproszenie, lista znajomych zostaje odświeżona.
     */
    private void getAcceptedRequests() {
        Log.d(TAG, "Sprawdzanie zaproszeń...");
        if (session.isLoggedIn()) {
            String tag_string_req = "req_getAcceptedRequests";
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_REGISTER, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Getting accepted requests Response: " + response.toString());

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {
                            JSONArray accepted_req = jObj.getJSONArray("messages");
                            if (accepted_req.length() > 0) {
                                ZakladkaZnajomi.new_accepted_req = true;
                            }
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Message ERROR: " + error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    HashMap<String, String> user = db.getUserDetails();
                    String userID = user.get("userID");
                    params.put("tag", "getMessages");
                    params.put("userID", userID);
                    params.put("type", "2");
                    return params;
                }
            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    /**
     * Metoda odpowiedzialna za utworzenie powiadomienia o nowej wiadomości. W zależności od preferencji użytkownika, powiadomieniu zostaje przypisany, dźwięk, wibracja lub też jest ono tworzone w trybie cichym.
     */
    private void makeNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.ic_app);
        mBuilder.setContentTitle("MateFinder");
        mBuilder.setContentText("Masz nowe powiadomienia.");
        mBuilder.setAutoCancel(true);

        if (!notif_settings[0] && notif_settings[1]) {
            mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        }

        if (!notif_settings[0] && notif_settings[2]) {
            mBuilder.setVibrate(new long[]{200, 200, 200, 200, 200});
        }

        Intent resultIntent = new Intent(context, MessageActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = 001;
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
