package pl.mf.zpi.matefinder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by root on 12.04.15.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private CharSequence tytuly[];
    private int iloscZakladek;
    private ZakladkaGrup grupy;
    private ZakladkaZnajomi znajomi;


    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int n) {
        super(fm);

        this.tytuly = mTitles;
        this.iloscZakladek = n;
    }

    //Zwraca fragment dla kazdej zakładkir
    @Override
    public Fragment getItem(int position) {

        if (position == 0) // dla 0 zwracamy zkładkę znajoych
        {
            znajomi = new ZakladkaZnajomi();
            return znajomi;
        } else             // dla 1 (w przeciwnym wypadku) zakładkę grup
        {
            grupy = new ZakladkaGrup();
            return grupy;
        }


    }

    // Pobieranie tytułu zakładki o numerze n

    @Override
    public CharSequence getPageTitle(int n) {
        return tytuly[n];
    }

    //Pobieranie ilości zakładek
    @Override
    public int getCount() {
        return iloscZakladek;
    }

    public void refresh(){
        grupy.refresh();
    }

    public ExpandableGroupListAdapter getGroupAdapter(){
        return grupy.getAdapter();
    }
}
