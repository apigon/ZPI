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
 * Klasa umożliwiająca przechowywanie listy wiadomości w postaci adaptera.
 */
public class MessageAdapter extends ArrayAdapter<Message> {

    /**
     * Konstruktor dziedziczący po nadklasie, którą jest klasa ArrayAdapter.
     *
     * @param context kontekst danej intencji
     * @param objects lista wiadomości umieszczanych w adapterze
     */
    public MessageAdapter(Context context, List<Message> objects) {
        super(context, R.layout.message_row, objects);
    }

    /**
     * Metoda odpowiedzialna za dodawanie oraz wyświetlanie poszczególnych wiadomości w adapterze. Przyporządkowuje odpowiednim komponentom widoku ich treść, login autora, datę dostarczenia oraz ikonę wiadomości.
     *
     * @param position    pozycja danej wiadomości w adapterze
     * @param convertView stary widok, który może zostać użyty, jeżeli to możliwe
     * @param parent      grupa widoków, do których dany widok zostanie przyłączony
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View message_view = inflater.inflate(R.layout.message_row, parent, false);

        Message single_message = getItem(position);

        TextView message_text = (TextView) message_view.findViewById(R.id.message_text);
        TextView message_author = (TextView) message_view.findViewById(R.id.message_author);
        TextView message_date = (TextView) message_view.findViewById(R.id.message_date);
        ImageView message_image = (ImageView) message_view.findViewById((R.id.message_image));

        message_text.setText(single_message.getText());
        message_author.setText(single_message.getAuthor());
        message_date.setText(single_message.getDate());

        if (single_message.isRead())
            message_image.setImageResource(R.drawable.ic_message);
        else
            message_image.setImageResource(R.drawable.ic_new_message);
        return message_view;
    }
}
