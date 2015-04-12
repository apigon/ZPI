package pl.mf.zpi.matefinder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by root on 12.04.15.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private CharSequence tutuly[];
    private int iloscZakladek;


    public ViewPagerAdapter(FragmentManager fm,CharSequence mTitles[], int n) {
        super(fm);

        this.tutuly = mTitles;
        this.iloscZakladek = n;

    }

    //Zwraca fragment dla kazdej zakładkir
    @Override
    public Fragment getItem(int position) {

        if(position == 0) // dla 0 zwracamy zkładkę grup
        {
            ZakladkaGrup grupy = new ZakladkaGrup();
            return grupy;
        }
        else             // dla 1 (w przeciwnym wypadku) zakładkę znajomych
        {
            ZakladkaZnajomi znajomi = new ZakladkaZnajomi();
            return znajomi;
        }


    }

    // Pobieranie tytułu zakładki o numerze n

    @Override
    public CharSequence getPageTitle(int n) {
        return tutuly[n];
    }

    //Pobieranie ilości zakładek
    @Override
    public int getCount() {
        return iloscZakladek;
    }
}
