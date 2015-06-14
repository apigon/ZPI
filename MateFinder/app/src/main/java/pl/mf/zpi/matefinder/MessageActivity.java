package pl.mf.zpi.matefinder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.ArrayList;

import pl.mf.zpi.matefinder.helper.SQLiteHandler;

/**
 * Aktywnosc odpowiadajaca za wyswietlanie listy otrzymanych wiadomosci, wyswietlanie poszczegolnych pozycji tej listy oraz usuwanie ich.
 */
public class MessageActivity extends ActionBarActivity implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, PopupMenu.OnMenuItemClickListener {

    private SQLiteHandler db;
    private Toolbar toolbar;

    private int message_id;

    /**
     * Metoda wywolywana przy tworzeniu aktywności, zawiera inicjalizację wszelkich potrzebnych parametrów, widoków, bocznego menu.
     *
     * @param savedInstanceState parametr przechowujący poprzedni stan, w którym znajdowala sie aktywnosc przed jej zakonczeniem; na jego podstawie odtwarzana jest poprzednia konfiguracja, np. orientacja ekranu
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        db = new SQLiteHandler(getApplicationContext());
        ArrayList<Message> messages = db.getMessages();

        ListAdapter messages_adapter = new MessageAdapter(this, messages);
        ListView messages_list_view = (ListView) findViewById(R.id.list_messages);
        messages_list_view.setAdapter(messages_adapter);

        messages_list_view.setOnItemClickListener(this);
        messages_list_view.setOnItemLongClickListener(this);
        /////////////////
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
    }

    /**
     * Metoda odpowiadajaca za wyświetlanie danej wiadomości w postaci dialogu, po kliknieciu na nia. Wyswietlona w ten sposob wiadomosc zostaje oznaczona jako przeczytana, jej ikona zostaje zmieniona.
     *
     * @param parent   adapter przechowujacy wiadomosci
     * @param view     widok, w ktorym uzytkownik obecnie się znajduje
     * @param position pozycja wybranej, kliknietej wiadomosci w adapterze
     * @param id       identyfikator wybranej, kliknietej wiadomosci
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Message mess = (Message) parent.getItemAtPosition(position);
        db.setMessageRead(mess.getId());
        new AlertDialog.Builder(MessageActivity.this)
                .setTitle("Wiadomość z\n" + mess.getDate())
                .setMessage("Użytkownik " + mess.getAuthor() + " jest w Twoim promieniu wyszukiwania.").
                setNeutralButton(R.string.dialog_back, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                        startActivity(getIntent());
                    }
                })
                .show();
        MainActivity.refreshMenuIcon(db.allMessagesRead());
    }

    /**
     * Metoda odpowiadajaca za umozliwienie uzytkownikowi zarzadzanie wiadomoscią po dluzszym kliknieciu na nia. Po dluzszym kliknieciu na wybrana wiadomosc, zostaje wyswietlone menu, zawierajace opcje takie jak usuniecie danej wiadomosci lub usuniecie wszystkich wiadomosci.
     *
     * @param parent   adapter przechowujacy wiadomosci
     * @param view     widok, w ktorym uzytkownik obecnie się znajduje
     * @param position pozycja wybranej, kliknietej wiadomosci w adapterze
     * @param id       identyfikator wybranej, kliknietej wiadomosci
     * @return po wykonaniu operacji zwiazanej z dluższym kliknieciem na dana wiadomosc, zawsze zwraca wartosc TRUE
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Message mess = (Message) parent.getItemAtPosition(position);
        message_id = mess.getId();
        PopupMenu menu = new PopupMenu(getApplicationContext(), view);
        menu.getMenuInflater().inflate(R.menu.message_popup_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(this);
        menu.show();

        return true;
    }

    /**
     * Metoda odpowiadajaca za przypisanie odpowiednich funkcji opcjom w menu danej wiadomosci, wyswietlanym po dluższym kliknieciu na wiadomosc. W zalezności od wybranej opcji, dana wiadomosc moze zostac usunieta lub tez moze zostac wyczyszczona cala skrzynka odbiorcza. W przypadku bledu, ktory wystąpil podczas wykonywania danej operacji, lub tez po pomyslnym wykonaniu danej operacji, zostaje wyswietlony odpowiedni komunikat.
     *
     * @param item wybrana pozycja w menu
     * @return w kazdym wypadku zwraca wartosc FALSE; zadne inne zadania nie zostana wykonane, poniewaz kazdy ciag instrukcji konczy sie poleceniem BREAK
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_message:
                if (db.deleteMessage(message_id)) {
                    Toast.makeText(getApplicationContext(), "Wiadomość usunięta.", Toast.LENGTH_SHORT).show();
                    MainActivity.refreshMenuIcon(db.allMessagesRead());
                    finish();
                    startActivity(getIntent());
                } else
                    Toast.makeText(getApplicationContext(), "Wystąpił błąd.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete_all_messages:
                db.deleteMessages();
                Toast.makeText(getApplicationContext(), "Wiadomości zostały usunięte.", Toast.LENGTH_SHORT).show();
                MainActivity.refreshMenuIcon(db.allMessagesRead());
                finish();
                startActivity(getIntent());
                break;
        }
        return false;
    }

    /**
     * Metoda odpowiedzialna za przypisanie odpowiedniego, wyspecjalizowanego widoku menu do danej aktywności.
     *
     * @param menu parametr, do ktorego przypisywany jest odpowiedni widok
     * @return po dokonaniu przypisania zawsze zwraca wartosc TRUE
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_messages, menu);
        return true;
    }

    /**
     * Metoda odpowiedzialna za przypisanie funkcjonalnosci, odpowiednich zachowan aplikacji do poszczegolnych pozycji w menu calej aktywnosci.
     *
     * @param item wybrana pozycja, do której przypisywana jest okreslona funkcjonalnosc
     * @return zwraca wartosc 'true' po przypisaniu funkcjonalnosci do danej pozycji menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
