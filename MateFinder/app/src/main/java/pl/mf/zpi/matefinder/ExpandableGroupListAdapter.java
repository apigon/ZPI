package pl.mf.zpi.matefinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.PopupMenu;
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
 * Created by root on 01.06.15.
 */
public class ExpandableGroupListAdapter extends BaseExpandableListAdapter implements AdapterView.OnItemLongClickListener, PopupMenu.OnMenuItemClickListener {

    private SQLiteHandler db;
    private ArrayList<Group> groups;
    private HashMap<Integer, ArrayList<Friend>> members;
    private Context context;
    private ExpandableListView listView;
    private int index;
    private static final String TAG = "MainGroupList";
    private ProgressDialog pDialog;

    public ExpandableGroupListAdapter(Context context, ExpandableListView listView){
        this.context=context;
        this.listView=listView;

        db = new SQLiteHandler(context);

        setData();

        index = -1;

        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);
        pDialog.setMessage("Zapisywaie...");

    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return members.get(groups.get(groupPosition).getID()).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return members.get(groups.get(groupPosition).getID()).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groups.get(groupPosition).getID();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return members.get(groups.get(groupPosition).getID()).get(childPosition).getId();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String groupName = groups.get(groupPosition).getName();
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.group_list_element, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.groupName);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(groupName);

        if(!groups.get(groupPosition).getVisible())
            convertView.setAlpha(0.4f);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = ((Friend)getChild(groupPosition, childPosition)).getLogin();

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.member_list_element, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.memberName);

        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        int itemType = ExpandableListView.getPackedPositionType(id);

        if ( itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            int childPosition = ExpandableListView.getPackedPositionChild(id);
            int groupPosition = ExpandableListView.getPackedPositionGroup(id);

            //do your per-item callback here
            return true; //true if we consumed the click, false if not

        } else if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            int groupPosition = ExpandableListView.getPackedPositionGroup(position);
            index = position;
            PopupMenu menu = new PopupMenu(context, listView.getChildAt(groupPosition));
            menu.getMenuInflater().inflate(R.menu.group_popup_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(this);
            MenuItem item = menu.getMenu().findItem(R.id.visible);
            if(!groups.get(groupPosition).getVisible())
                item.setTitle(R.string.group_menu_visible_on);
            menu.show();
            return true; //true if we consumed the click, false if not

        } else {
            // null item; we don't consume the click
            return false;
        }
    }


    public void refresh(){
        setData();
        notifyDataSetChanged();
    }

    private void setData(){
        groups = db.getGroupsDetails();
        members = new HashMap<Integer, ArrayList<Friend>>();
        for(Group g: groups){
            int id = g.getID();
            members.put(id, db.getMembersDetails(id));
        }
    }

    private void changeVisible(MenuItem item){
        Group group = groups.get(index);
        boolean visible = !group.getVisible();
        group.setVisible(visible);
        String tekst = visible?"Wybrana grupa będzie wyświetlana.":"Wybrana grupa nie będzie wyswietlana";
        Toast toas = Toast.makeText(context, tekst, Toast.LENGTH_SHORT);
        toas.show();
        String setValue = visible?"1":"0";
        db.setGroupVisible(group.getName(), setValue);
        item.setTitle(visible?R.string.group_menu_visible_off:R.string.group_menu_visible_on);
        listView.getChildAt(index).setAlpha(visible ? 1f : 0.4f);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.visible:
                changeVisible(item);
                break;
            case R.id.edit:
                intent = new Intent(context, UpdateGroupActivity.class);
                intent.putExtra("group", groups.get(index));
                ((Activity)context).startActivityForResult(intent, 1);
                break;
            case R.id.add:
                intent = new Intent(context, AddFriendToGroupActivity.class);
                intent.putExtra("adapter", 2);//2=grupy
                intent.putExtra("id", groups.get(index).getID());
                ((Activity)context).startActivityForResult(intent, 1);
                break;
            case R.id.delete:
                deleteGroup();
                break;
        }
        return false;
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
