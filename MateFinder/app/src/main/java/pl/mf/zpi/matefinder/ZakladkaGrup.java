package pl.mf.zpi.matefinder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListView;

import java.io.Serializable;

/**
 * Created by root on 12.04.15.
 */
public class ZakladkaGrup extends Fragment{

    private ExpandableListView groupList;
    private ExpandableGroupListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.zakladka_grup, container, false);

        groupList = (ExpandableListView)v.findViewById(R.id.groupList);
        adapter = new ExpandableGroupListAdapter(getActivity(), groupList);
        groupList.setAdapter(adapter);
//        groupList.setOnGroupCollapseListener(adapter);
//        groupList.setOnGroupExpandListener(adapter);
        groupList.setOnItemLongClickListener(adapter);
//        groupList.setOnItemClickListener(adapter);
        return v;
    }

    public void refresh(){
        adapter.refresh();
    }

    public ExpandableGroupListAdapter getAdapter(){
        return adapter;
    }

}
