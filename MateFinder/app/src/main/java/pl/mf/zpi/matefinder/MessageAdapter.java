package pl.mf.zpi.matefinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Adam on 2015-05-23.
 */
public class MessageAdapter extends ArrayAdapter<Message> {

    public MessageAdapter(Context context, List<Message> objects) {
        super(context, R.layout.message_row, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View message_view = inflater.inflate(R.layout.message_row, parent, false);

        Message single_message = getItem(position);
        TextView message_text = (TextView)message_view.findViewById(R.id.message_text);
        TextView message_author = (TextView)message_view.findViewById(R.id.message_author);
        TextView message_date = (TextView)message_view.findViewById(R.id.message_date);
        ImageView message_image = (ImageView)message_view.findViewById((R.id.message_image));

        message_text.setText(single_message.getText());
        message_author.setText(single_message.getAuthor());
        message_date.setText(single_message.getDate());
        message_image.setImageResource(R.drawable.ic_message);
        return message_view;
    }
}
