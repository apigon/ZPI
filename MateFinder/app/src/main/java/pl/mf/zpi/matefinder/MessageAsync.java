package pl.mf.zpi.matefinder;

import android.app.NotificationManager;
import android.content.Context;
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

    private boolean running = true;

    private SQLiteHandler db;

    public MessageAsync(Context context){
        Log.d(TAG, "Uruchomiono ASYNC");
        this.context = context;
        db = new SQLiteHandler(context);
    }

    @Override
    protected Void doInBackground(Void... params) {
        getMessages();
        return null;
    }

    private void getMessages(){
        while(running) {
            Log.d(TAG, "Co 10 sek.");

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
                                final String userID = u.getString("userID");
                                String content = u.getString("content");

                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                                mBuilder.setSmallIcon(R.drawable.ic_action_notif);
                                mBuilder.setContentTitle("MateFinder");
                                mBuilder.setContentText(content);
                                int mNotificationId = i;
                                NotificationManager mNotifyMgr =
                                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                mNotifyMgr.notify(mNotificationId, mBuilder.build());
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
                    Log.e(TAG, "Login ERROR: " + error.getMessage());
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

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop(){
        running = false;
    }
}
