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
 * Aktywność odpowiadająca za wyświetlanie listy otrzymanych wiadomości, wyswietlanie poszczególnych pozycji tej listy oraz usuwanie ich.
 */
public class MessageActivity extends ActionBarActivity implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, PopupMenu.OnMenuItemClickListener {

    private SQLiteHandler db;
    private Toolbar toolbar;

    private int message_id;

    /**
     * Metoda wywoływana przy tworzeniu aktywności, zawiera inicjalizację wszelkich potrzebnych parametrów, widoków, bocznego menu.
     *
     * @param savedInstanceState parametr przechowujący poprzedni stan, w którym znajdowała się aktywność przed jej zakończeniem; na jego podstawie odtwarzana jest poprzednia konfiguracja, np. orientacja ekranu
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
     * Metoda odpowiadająca za wyświetlanie danej wiadomości w postaci dialogu, po kliknięciu na nią. Wyświetlona w ten sposób wiadomość zostaje oznaczona jako przeczytana, jej ikona zostaje zmieniona.
     *
     * @param parent   adapter przechowujący wiadomości
     * @param view     widok, w którym uzytkownik obecnie się znajduje
     * @param position pozycja wybranej, klikniętej wiadomości w adapterze
     * @param id       identyfikator wybranej, klikniętej wiadomości
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
     * Metoda odpowiadająca za umożliwienie użytkownikowi zarządzanie wiadomością po dłuższym kliknięciu na nią. Po dłuższym kliknięciu na wybraną wiadomość, zostaje wyświetlone menu, zawierające opcje takie jak usunięcie danej wiadomości lub usunięcie wszystkich wiadomości.
     *
     * @param parent   adapter przechowujący wiadomości
     * @param view     widok, w którym uzytkownik obecnie się znajduje
     * @param position pozycja wybranej, klikniętej wiadomości w adapterze
     * @param id       identyfikator wybranej, klikniętej wiadomości
     * @return po wykonaniu operacji związanej z dłuższym kliknięciem na daną wiadomość, zawsze zwraca wartość TRUE
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
     * Metoda odpowiadająca za przypisanie odpowiednich funkcji opcjom w menu danej wiadomości, wyświetlanym po dłuższym kliknięciu na wiadomość. W zależności od wybranej opcji, dana wiadomość może zostać usunięta lub też może zostać wyczyszczona cała skrzynka odbiorcza. W przypadku błędu, który wystąpił podczas wykonywania danej operacji, lub też po pomyślnym wykonaniu danej operacji, zostaje wyświetlony odpowiedni komunikat.
     *
     * @param item wybrana pozycja w menu
     * @return w każdym wypadku zwraca wartość FALSE; żadne inne zadania nie zostaną wykonane, ponieważ każdy ciąg instrukcji kończy się poleceniem BREAK
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
     * Metoda odpowiedzialna za przypisanie odpowiedniego, wyspecjalizowanego widoku Menu do danej aktywności.
     *
     * @param menu parametr, do którego przypisywany jest odpowiedni widok
     * @return po dokonaniu przypisania zawsze zwraca wartość TRUE
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_messages, menu);
        return true;
    }

    /**
     * Metoda odpowiedzialna za przypisanie funkcjonalności, odpowiednich zachowań aplikacji do poszczególnych pozycji w Menu całej aktywności.
     *
     * @param item wybrana pozycja, do której przypisywana jest określona funkcjonalność
     * @return zwraca wartość 'true' po przypisaniu funkcjonalności do danej pozycji Menu
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
