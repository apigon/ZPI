package pl.mf.zpi.matefinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import pl.mf.zpi.matefinder.helper.SQLiteHandler;

/**
 * Adapter grup
 */
public class GroupAdapter extends BaseAdapter {

    protected SQLiteHandler db;
    protected ArrayList<Group> groups;
    protected Context context;
    protected ListView listView;
    protected int index;

    public GroupAdapter(Context c, ListView list) {
        db = new SQLiteHandler(c);
        groups = db.getGroupsDetails();
        context = c;
        listView = list;
        index = -1;
    }

    /**
     * Pobieranie ilosci elementow w adapterze
     * @return ilosc elementow
     */
    @Override
    public int getCount() {
        return groups.size();
    }

    /**
     * Pobieranie danego elementu z adaptera
     * @param position pozycja w adapterze
     * @return zwraca wybrany element
     */
    @Override
    public Object getItem(int position) {
        return groups.get(position);
    }

    /**
     * Pobieranie id wybranego elementu
     * @param position Pozycja w adapterze wybranego elementu
     * @return id wybranego elementu
     */
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
}
