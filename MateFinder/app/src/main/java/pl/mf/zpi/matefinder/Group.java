package pl.mf.zpi.matefinder;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Przechowywanie informacji o grupie znajomych
 */
public class Group implements Serializable {
    private String name;
    private ArrayList<String> friends;
    private boolean visible;
    private int id;

    public Group(int id, String name, int visible) {
        this.id = id;
        if(name.toString().equals("Znajomi")){
            this.name = "Domy\u015blna";
        }
        else{
            this.name = name;
        }
        friends = new ArrayList<String>();
        this.visible = visible == 1;
        Log.d("CREATE NEW GROUP","Nazwa grupy " + this.name);
    }

    /**
     * Pobieranie Id grupy.
     * @return id grupy
     */
    public int getID() {
        return id;
    }


    public void addFriend(String friend) {
        friends.add(friend);
    }

    public ArrayList<String> getFriends() {
        return friends;
    }

    /**
     * Sprawdzanie czy grupa jest widoczna (wyswietlana).
     * @return true gdy grupa wyswietlana, false w przeciwnym wypadku
     */
    public boolean getVisible() {
        return visible;
    }

    /**
     * Ustawianie widocznyosci (wyswietlania) grupy.
     * @param visible wyswietlanie grupy
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Pobieranie nazwy grupy.
     * @return nazwa grupy
     */
    public String getName() {
        return name;
    }

    /**
     * Ustawianie nazwy grupy.
     * @param name nazwa grupy
     */
    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object o){
        Group g = (Group) o;
        return id==((Group) o).getID();
    }
}
