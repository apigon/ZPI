package pl.mf.zpi.matefinder;

/**
 * Klasa przechowująca informacje na temat wiadomości, jej unikalny identyfikator, login autora, treść, datę otrzymania oraz czy została przeczytana, czy nie.
 */
public class Message {

    private String content;
    private String author;
    private int id;
    private int read;
    private String date;

    /**
     * Konstruktor przypisujący wartości poszczególnych parametrów wiadomości.
     *
     * @param id      identyfikator wiadomości
     * @param author  login autor wiadomości
     * @param content treść wiadomości
     * @param read    parametr określający, czy wiadomość została przeczytana
     * @param date    data dostarczenia wiadomości
     */
    public Message(int id, String author, String content, int read, String date) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.read = read;
        this.date = date;
    }

    /**
     * Metoda odpowiedzialna za pobranie oraz zwrócenie treści wiadomości.
     *
     * @return treść wiadomości
     */
    public String getText() {
        return content;
    }

    /**
     * Metoda odpowiedzialna za pobranie oraz zwrócenie loginu autora wiadomości.
     *
     * @return login autora wiadomości
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Metoda odpowiedzialna za pobranie oraz zwrócenie identyfikatora wiadomości.
     *
     * @return unikalny identyfikator wiadomości
     */
    public int getId() {
        return id;
    }

    /**
     * Metoda odpowiedzialna za pobranie oraz zwrócenie daty otrzymania wiadomości.
     *
     * @return data otrzymania wiadomości
     */
    public String getDate() {
        return date;
    }

    /**
     * Metoda odpowiedzialna za pobranie oraz zwrócenie informacji na temat tego, czy wiadomość została przeczytana.
     *
     * @return TRUE, gdy wiadomość została przeczytana, FALSE w przeciwnym wypadku
     */
    public boolean isRead() {
        return read == 1;
    }
}
