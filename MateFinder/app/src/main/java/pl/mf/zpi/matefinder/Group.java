package pl.mf.zpi.matefinder;

import java.util.ArrayList;

/**
 * Created by root on 04.05.15.
 */
public class Group {

    private String name;
    private ArrayList<String> friends;
    private boolean visible;

    public Group(String name, int visible){
        this.name=name;
        friends = new ArrayList<String>();
        this.visible = visible == 1;
    }

    public void addFriend(String friend){
        friends.add(friend);
    }

    public ArrayList<String> getFriends(){
        return friends;
    }

    public void setVisible(boolean visible){
        this.visible=visible;
    }

    public boolean getVisible(){
        return visible;
    }

    public String getName(){
        return name;
    }
}
