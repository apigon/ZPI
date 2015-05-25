package pl.mf.zpi.matefinder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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

/**
 * Created by Adam on 2015-05-15.
 */
public class MessageAsync extends AsyncTask<Void, Void, Void> {

    private static final String TAG = MessageAsync.class.getSimpleName();
    private Context context;
    private String userID;

    private SQLiteHandler db;
    public static boolean running;

    public MessageAsync(Context context){
        Log.d(TAG, "Uruchomiono ASYNC");
        this.context = context;
        db = new SQLiteHandler(context);
        running = true;
    }

    @Override
    protected Void doInBackground(Void... params) {
        //while(running)
            getMessages();
        Log.d(TAG, "Zatrzymano ASYNC");
        return null;
    }

    private void getMessages(){
            Log.d(TAG, "Sprawdzanie wiadomości...");

            String tag_string_req = "req_getMessages";
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_REGISTER, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Getting messages Response: " + response.toString());

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        if(!error){
                            JSONArray user = jObj.getJSONArray("messages");
                            for (int i = 0; i < user.length(); i++) {
                                // user successfully logged in
                                JSONObject u = user.getJSONObject(i);
                                final String requestID = u.getString(("messageID"));
                                final String authorLogin = u.getString(("authorLogin"));
                                final String content = u.getString("content");

                                db.addMessage(requestID, authorLogin, content);
                            }
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                            mBuilder.setSmallIcon(R.drawable.ic_app);
                            mBuilder.setContentTitle("MateFinder");
                            mBuilder.setContentText("Masz nowe powiadomienia.");
                            mBuilder.setVibrate(new long[]{ 200, 200, 200, 200, 200 });

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

                            MainActivity.messages = true;
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

            /*try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
    }
}
