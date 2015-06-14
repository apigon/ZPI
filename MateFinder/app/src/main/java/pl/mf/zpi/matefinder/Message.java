package pl.mf.zpi.matefinder;

/**
 * Klasa przechowująca informacje na temat wiadomosci, jej unikalny identyfikator, login autora, tresc, date otrzymania oraz czy zostala przeczytana, czy nie.
 */
public class Message {

    private String content;
    private String author;
    private int id;
    private int read;
    private String date;

    /**
     * Konstruktor przypisujacy wartosci poszczegolnych parametrow wiadomosci.
     *
     * @param id      identyfikator wiadomosci
     * @param author  login autor wiadomosci
     * @param content treść wiadomosci
     * @param read    parametr okreslający, czy wiadomosc zostala przeczytana
     * @param date    data dostarczenia wiadomosci
     */
    public Message(int id, String author, String content, int read, String date) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.read = read;
        this.date = date;
    }

    /**
     * Metoda odpowiedzialna za pobranie oraz zwrocenie tresci wiadomosci.
     *
     * @return tresc wiadomosci
     */
    public String getText() {
        return content;
    }

    /**
     * Metoda odpowiedzialna za pobranie oraz zwrocenie loginu autora wiadomosci.
     *
     * @return login autora wiadomosci
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Metoda odpowiedzialna za pobranie oraz zwrocenie identyfikatora wiadomości.
     *
     * @return unikalny identyfikator wiadomosci
     */
    public int getId() {
        return id;
    }

    /**
     * Metoda odpowiedzialna za pobranie oraz zwrocenie daty otrzymania wiadomosci.
     *
     * @return data otrzymania wiadomosci
     */
    public String getDate() {
        return date;
    }

    /**
     * Metoda odpowiedzialna za pobranie oraz zwrocenie informacji na temat tego, czy wiadomosc zostala przeczytana.
     *
     * @return TRUE, gdy wiadomosc zostala przeczytana, FALSE w przeciwnym wypadku
     */
    public boolean isRead() {
        return read == 1;
    }
}
