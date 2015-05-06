package pl.mf.zpi.matefinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.mf.zpi.matefinder.helper.SQLiteHandler;

/**
 * Created by root on 04.05.15.
 */
public class GroupAdapter extends BaseAdapter {

    SQLiteHandler dbHandler;
    ArrayList<Group> groups;
    Context context;

    public GroupAdapter(Context c){
        dbHandler = new SQLiteHandler(c);
        List<HashMap<String, String>> groupsDB = dbHandler.getGroupsDetails();
        groups = new ArrayList<Group>();
        for(int i=0;i<groupsDB.size();i++){
            Group group = new Group(groupsDB.get(i).get("name"),Integer.parseInt(groupsDB.get(i).get("visible")));
            groups.add(group);
        }
        context=c;
    }
    @Override
    public int getCount() {
        return groups.size();
    }

    @Override
    public Object getItem(int position) {
        return groups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.group_list_element,parent,false);
        TextView name = (TextView) row.findViewById(R.id.groupName);
        Group tmp = groups.get(position);
        name.setText(tmp.getName());
        return row;
    }
}
