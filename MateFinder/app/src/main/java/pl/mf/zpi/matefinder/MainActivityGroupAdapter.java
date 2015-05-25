package pl.mf.zpi.matefinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
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
 * Created by root on 13.05.15.
 */
public class MainActivityGroupAdapter extends GroupAdapter implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, PopupMenu.OnMenuItemClickListener {

    private static final String TAG = "deleteGroup";
    private ProgressDialog pDialog;

    public MainActivityGroupAdapter(Context context, ListView listView){
        super(context, listView);
        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);
        pDialog.setMessage("Zapisywaie...");
    }

    //TODO do rozwoju wyświetlanie znajomych dla każdej grupy.

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        index = position;
        PopupMenu menu = new PopupMenu(context, listView.getChildAt(position));
        menu.getMenuInflater().inflate(R.menu.group_popup_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(this);
        menu.show();
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View row = super.getView(position, convertView, parent);
        if(!groups.get(position).getVisible())
            row.setAlpha(0.4f);
        return row;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.edit:
                intent = new Intent(context, UpdateGroupActivity.class);
                intent.putExtra("group", groups.get(index));
                context.startActivity(intent);
                ((Activity) context).finish();
                break;
            case R.id.add:
                intent = new Intent(context, AddFriendToGroupActivity.class);
                intent.putExtra("adapter", 2);//2=grupy
                intent.putExtra("id", groups.get(index).getID());
                context.startActivity(intent);
                break;
            case R.id.delete:
                deleteGroup();
                break;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Group group = groups.get(position);
        boolean visible = !group.getVisible();
        group.setVisible(visible);
        String tekt = visible?"Wybrana grupa będzie wyświetlana.":"Wybrana grupa nie będzie wyswietlana";
        Toast toas = Toast.makeText(context, tekt, Toast.LENGTH_SHORT);
        toas.show();
        String setValue = visible?"1":"0";
        db.setGroupVisible(group.getName(), setValue);
        listView.getChildAt(position).setAlpha(visible?1f:0.4f);
    }

    private void deleteGroup(){
        final Group g = groups.get(index);
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.delete_group_title)
                .setMessage(R.string.dlelete_group_confirm)
                .setPositiveButton(R.string.delete_group_yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete(g);
                    }

                })
                .setNegativeButton(R.string.delete_group_no, null)
                .show();

    }

    private void delete(final Group gid){
        // Tag used to cancel the request
        //db = new SQLiteHandler(getApplicationContext());
        showDialog();
        String tag_string_req = "deleteGroup_req";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Delete group Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        db.deleteGroup(gid.getID());
                        groups.remove(gid);
                        notifyDataSetChanged();
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
                Log.e(TAG, "Deleting group Error: " + error.getMessage());
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
                params.put("tag", "deleteGroup");
                params.put("groupID", ""+gid.getID());

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
