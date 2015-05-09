package pl.mf.zpi.matefinder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by root on 12.04.15.
 */
public class ZakladkaGrup extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.zakladka_grup, container, false);

        ListView groupList = (ListView)v.findViewById(R.id.groupList);
        groupList.setAdapter(new GroupAdapter(getActivity()));
        return v;
    }
}
