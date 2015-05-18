package pl.mf.zpi.matefinder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pl.mf.zpi.matefinder.app.AppConfig;
import pl.mf.zpi.matefinder.app.AppController;
import pl.mf.zpi.matefinder.helper.SQLiteHandler;

/**
 * Created by root on 18.05.15.
 */
public class FriendCheckAdapter extends BaseAdapter implements View.OnClickListener {

    private Context context;
    private ArrayList<Friend> friends;
    private int id;
    private ListView listView;
    private SQLiteHandler db;
    private static final String TAG = "addGroup";
    private ProgressDialog pDialog;

    public FriendCheckAdapter(Context context, ListView list, int id){
        this.context=context;
        db = new SQLiteHandler(context);
        this.listView=list;
        this.id=id;

        friends =db.getMembersDetails(id);


        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);
    }


    @Override
    public int getCount() {
        return friends.size();
    }

    @Override
    public Object getItem(int position) {
        return friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return friends.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.list_check_item, parent, false);
        TextView name = (TextView) row.findViewById(R.id.name);
        Friend tmp = friends.get(position);
        name.setText(tmp.getName());
        return row;
    }

    @Override
    public void onClick(View v) {
        pDialog.setMessage("Zapisywanie...");
        showDialog();
        for(int i=0; i<friends.size(); i++){
            View row = listView.getChildAt(i);
            if(((CheckBox)row.findViewById(R.id.check)).isChecked()){
                Friend f = friends.get(i);
                addToGroup(id, f.getId());
            }
        }
        hideDialog();
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
        ((Activity)context).finish();
    }

    private void addToGroup(final int gid, final int mid){
        //TODO dodać json'a (odkomentować i wywalić dodawanie do sqlite)
        db.addMember(gid, mid);
        // Tag used to cancel the request
        //db = new SQLiteHandler(getApplicationContext());
//        String tag_string_req = "addMember_req";
//
//
//        StringRequest strReq = new StringRequest(Request.Method.POST,
//                AppConfig.URL_REGISTER, new Response.Listener<String>() {
//
//            @Override
//            public void onResponse(String response) {
//                Log.d(TAG, "Add Member Response: " + response.toString());
//                hideDialog();
//
//                try {
//                    JSONObject jObj = new JSONObject(response);
//                    boolean error = jObj.getBoolean("error");
//                    if (!error) {
//                        int gid = jObj.getInt("groupID");
//                        db.addMember(gid, mid);
//                        // Launch login activity
//                        //   backToMain();
//                    } else {
//
//                        // Error occurred in registration. Get the error
//                        // message
//                        String errorMsg = jObj.getString("error_msg");
//                        Toast.makeText(context,
//                                errorMsg, Toast.LENGTH_LONG).show();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e(TAG, "Adding member Error: " + error.getMessage());
//                Toast.makeText(context,
//                        error.getMessage(), Toast.LENGTH_LONG).show();
//                hideDialog();
//            }
//        }) {
//
//            @Override
//            protected Map<String, String> getParams() {
//                SQLiteHandler db = new SQLiteHandler(context);
//                HashMap<String, String> user = db.getUserDetails();
//                // Posting params to register url
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("tag", "addMember");
//                params.put("groupID", ""+gid);
//                params.put("friendID", ""+mid);
//
//                return params;
//            }
//        };
//
//        // Adding request to request queue
//        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
