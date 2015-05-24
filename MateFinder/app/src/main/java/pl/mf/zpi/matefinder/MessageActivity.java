package pl.mf.zpi.matefinder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
 * Created by Adam on 2015-05-23.
 */
public class MessageActivity extends ActionBarActivity implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, PopupMenu.OnMenuItemClickListener {

    private SQLiteHandler db;
    private Toolbar toolbar;

    private int message_id;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        db = new SQLiteHandler(getApplicationContext());
        ArrayList<Message> messages = db.getMessages();;

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

        MainActivity.messages = false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Message mess = (Message) parent.getItemAtPosition(position);
        new AlertDialog.Builder(MessageActivity.this)
                .setTitle("Wiadomość z\n" + mess.getDate())
                .setMessage("Użytkownik " + mess.getAuthor() + " jest w Twoim promieniu wyszukiwania.").
                setNeutralButton(R.string.dialog_back, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_message:
                if(db.deleteMessage(message_id)) {
                    Toast.makeText(getApplicationContext(), "Wiadomość usunięta.", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(getIntent());
                }
                else
                    Toast.makeText(getApplicationContext(), "Wystąpił błąd.", Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_messages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                backToMain();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Powrót do ekranu głównego
    private void backToMain() {
        // Launching the login activity
        Intent intent = new Intent(MessageActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
