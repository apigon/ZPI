package pl.mf.zpi.matefinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.mf.zpi.matefinder.helper.SQLiteHandler;

/**
 * Created by root on 04.05.15.
 */
public class GroupAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, PopupMenu.OnMenuItemClickListener {

    protected SQLiteHandler dbHandler;
    protected ArrayList<Group> groups;
    protected Context context;
    protected ListView listView;
    protected int index;

    public GroupAdapter(Context c, ListView list) {
        dbHandler = new SQLiteHandler(c);
        groups = dbHandler.getGroupsDetails();
        context = c;
        listView = list;
        index = -1;
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
        View row = inflater.inflate(R.layout.group_list_element, parent, false);
        TextView name = (TextView) row.findViewById(R.id.groupName);
        Group tmp = groups.get(position);
        name.setText(tmp.getName());
        return row;
    }

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
    public boolean onMenuItemClick(MenuItem item) {
        //TODO pozostałe funkcjonalności
        if (item.getTitle().equals(context.getResources().getString(R.string.group_menu_edit))) {
            Intent intent = new Intent(context, UpdateGroupActivity.class);
            intent.putExtra("group", groups.get(index));
            context.startActivity(intent);
            ((Activity)context).finish();
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Group group = groups.get(position);
        boolean visible = group.getVisible();
        group.setVisible(!visible);
        String tekt = !visible?"Wybrana grupa będzie wyświetlana.":"Wybrana grupa nie będzie wyswietlana";
        Toast toas = Toast.makeText(context, tekt, Toast.LENGTH_SHORT);
        toas.show();
    }
}
