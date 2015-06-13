package pl.mf.zpi.matefinder;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by root on 04.05.15.
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

    public int getID() {
        return id;
    }

    public void addFriend(String friend) {
        friends.add(friend);
    }

    public ArrayList<String> getFriends() {
        return friends;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object o){
        Group g = (Group) o;
        return id==((Group) o).getID();
    }
}
