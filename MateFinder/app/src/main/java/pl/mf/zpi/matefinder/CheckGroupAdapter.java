package pl.mf.zpi.matefinder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Adapter grup wykorzystywany przy dodawaniu znajomego do grup. Adapter automatycznie filtruje grpy tak aby nie bylo mozliwosci dodania znajomoego kilkakrotnie do tej samej grupy
 */
public class CheckGroupAdapter extends  GroupAdapter implements View.OnClickListener{

    private static final String TAG = "addToGroup";
    private int id;
    private ProgressDialog pDialog;

    public CheckGroupAdapter(Context c, ListView list, int id) {
        super(c, list);
        this.id=id;

        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);

        db = new SQLiteHandler(context);
        ArrayList<Group> g = db.getMemberGroupsID(id);

        groups.removeAll(g);

        if(groups.size()==0){
            Toast.makeText(context, "Znajomy jest już dodany do wszystkich Twoich grup.", Toast.LENGTH_SHORT).show();
            backToMain();
        }
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.list_check_item, parent, false);
        TextView name = (TextView) row.findViewById(R.id.name);
        Group tmp = groups.get(position);
        name.setText(tmp.getName());
        return row;
    }

    /**
     * Zapisywanie przynaleznosci znajomego do poszczegolnych grup
     * @param v
     */
    @Override
    public void onClick(View v) {
        pDialog.setMessage("Zapisywanie...");
        showDialog();
        int count = countChecked();
        int added=0;
        for(int i=0; i<groups.size(); i++){
            View row = listView.getChildAt(i);
            if(((CheckBox)row.findViewById(R.id.check)).isChecked()){
                Group g = groups.get(i);
                added++;
                addToGroup(g, id, added==count);
            }
        }
        hideDialog();
    }

    private int countChecked(){
        int count = 0;
        for(int i=0; i<groups.size(); i++){
            View row = listView.getChildAt(i);
            if(((CheckBox)row.findViewById(R.id.check)).isChecked()){
                count++;
            }
        }
        return count;
    }

    /**
     * Dodawanie znajomego do konkretnej grupy
     * @param group grupa do ktorej dodajemy
     * @param mid znajomy ktorego dodajemy
     * @param end czy grupa, do ktorej dodajemy jest ostatnia, do ktorej mamy dodac
     */
    private void addToGroup(final Group group, final int mid, final boolean end){
        // Tag used to cancel the request
        db = new SQLiteHandler(context);
        String tag_string_req = "addMember_req";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Add Member Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        db.addMember(group.getID(), mid);
                        if(end)
                            backToMain();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(context,
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Adding member Error: " + error.getMessage());
                Toast.makeText(context,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                SQLiteHandler db = new SQLiteHandler(context);
                HashMap<String, String> user = db.getUserDetails();
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "addFriendToGroup");
                params.put("userID", db.getUserDetails().get("userID"));
                params.put("groupID", ""+group.getID());
                params.put("groupName", group.getName());
                params.put("memberID", ""+mid);

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

    private void backToMain(){
        ((Activity)context).setResult(1);
        ((Activity) context).finish();
    }
}
