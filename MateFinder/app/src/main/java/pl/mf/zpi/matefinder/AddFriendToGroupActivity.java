package pl.mf.zpi.matefinder;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;


public class AddFriendToGroupActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_to_group);

        Bundle extras = getIntent().getExtras();

        int id = extras.getInt("id");

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((TextView)findViewById(R.id.title)).setText(R.string.add_friend_to_groups);

        ListView list = (ListView) findViewById(R.id.list);
        if (extras.getInt("adapter") == 1){
            CheckGroupAdapter adapter = new CheckGroupAdapter(this, list, id);
            list.setAdapter(adapter);
            findViewById(R.id.zapisz).setOnClickListener(adapter);
        } else {
            FriendCheckAdapter adapter = new FriendCheckAdapter(this, list, id);
            list.setAdapter(adapter);
            findViewById(R.id.zapisz).setOnClickListener(adapter);
        }





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
