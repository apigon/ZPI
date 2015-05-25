package pl.mf.zpi.matefinder;

/**
 * Created by Adam on 2015-05-23.
 */
public class Message {

    private String content;
    private String author;
    private int id;
    private int read;
    private String date;

    public Message(int id, String author, String content, int read, String date){
        this.id = id;
        this.content = content;
        this.author = author;
        this.read = read;
        this.date = date;
    }

    public String getText(){
        return content;
    }

    public String getAuthor(){
        return author;
    }

    public int getId(){
        return id;
    }

    public String getDate(){
        return date;
    }
}
