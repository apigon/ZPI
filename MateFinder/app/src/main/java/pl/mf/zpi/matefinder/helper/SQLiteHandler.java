package pl.mf.zpi.matefinder.helper;

/**
 * Created by root on 22.03.15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.mf.zpi.matefinder.Friend;
import pl.mf.zpi.matefinder.Group;
import pl.mf.zpi.matefinder.Message;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Tables names
    private static final String TABLE_LOGIN = "login";
    private static final String TABLE_LOCATIONS = "locations";
    private static final String TABLE_FRIENDS = "friends";
    private static final String TABLE_LOCATIONS_FRIENDS = "friends_locations";
    private static final String TABLE_GROUPS = "groups";
    private static final String TABLE_MEMBERS = "members";
    private static final String TABLE_MESSAGES = "messages";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_ID_DATABASE = "userID";
    private static final String KEY_LOGIN = "login";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_NAME = "name";
    private static final String KEY_SURNAME = "surname";
    private static final String KEY_PHOTO = "photo";
    private static final String KEY_LOCATION = "location";              // location table + login table + friends table
    private static final String KEY_LAT = "lat";                        // location table
    private static final String KEY_LNG = "lng";                        // location table

    // Friends table column names
    private static final String KEY_FRIEND_ID = "id";
    private static final String KEY_FRIEND_ID_DATABASE = "userID";
    private static final String KEY_FRIEND_LOGIN = "login";
    private static final String KEY_FRIEND_EMAIL = "email";
    private static final String KEY_FRIEND_PHONE = "phone";
    private static final String KEY_FRIEND_NAME = "name";
    private static final String KEY_FRIEND_SURNAME = "surname";
    private static final String KEY_FRIEND_PHOTO = "photo";

    private static final String KEY_FRIEND_LOCATION_ID = "locationID";
    private static final String KEY_FRIEND_LAT = "lat";
    private static final String KEY_FRIEND_LNG = "lng";

    //Groups table column names
    private static final String KEY_GROUP_ID = "id";
    private static final String KEY_GROUP_NAME = "name";
    private static final String KEY_GROUP_VISIBLE = "visible";

    //Members table column names
    private static final String KEY_MEMBER_ID = "id";
    private static final String KEY_MEMBER_GROUP_ID = "groupID";
    private static final String KEY_MEMBER_USER_ID = "userID";

    //Messages
    private static final String KEY_MESSAGE_ID = "id";
    private static final String KEY_MESSAGE_AUTHOR = "author_login";
    private static final String KEY_MESSAGE_RECIPENT = "recipent_id";
    private static final String KEY_MESSAGE_CONTENT = "content";
    private static final String KEY_MESSAGE_READ = "read";
    private static final String KEY_MESSAGE_TIME = "time";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_ID_DATABASE + " TEXT," + KEY_LOGIN + " TEXT,"
                + KEY_EMAIL + " TEXT," + KEY_PHONE + " TEXT," + KEY_NAME + " TEXT,"
                + KEY_SURNAME + " TEXT," + KEY_PHOTO + " TEXT," + KEY_LOCATION + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        String CREATE_LOCATIONS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_LOCATION + " TEXT," + KEY_LAT
                + " TEXT," + KEY_LNG + " TEXT" + ")";
        db.execSQL(CREATE_LOCATIONS_TABLE);

        String CREATE_FRIENDS_TABLE = "CREATE TABLE " + TABLE_FRIENDS + "("
                + KEY_FRIEND_ID + " INTEGER PRIMARY KEY," + KEY_FRIEND_ID_DATABASE + " INTEGER," + KEY_FRIEND_LOGIN + " TEXT,"
                + KEY_FRIEND_EMAIL + " TEXT," + KEY_FRIEND_PHONE + " TEXT," + KEY_FRIEND_NAME + " TEXT,"
                + KEY_FRIEND_SURNAME + " TEXT," + KEY_FRIEND_PHOTO + " TEXT," + KEY_LOCATION + " TEXT" + ")";
        db.execSQL(CREATE_FRIENDS_TABLE);

        String CREATE_FRIENDS_LOCATIONS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS_FRIENDS + "("
                + KEY_FRIEND_LOCATION_ID + " INTEGER PRIMARY KEY," + KEY_FRIEND_LOGIN + " TEXT," + KEY_FRIEND_LAT
                + " TEXT," + KEY_FRIEND_LNG + " TEXT" + ")";
        db.execSQL(CREATE_FRIENDS_LOCATIONS_TABLE);

        String createGroupsTable = "CREATE TABLE " + TABLE_GROUPS + "(" + KEY_GROUP_ID + " INTEGER PRIMARY KEY, " +
                KEY_GROUP_NAME + " TEXT, " + KEY_GROUP_VISIBLE + " TEXT)";
        db.execSQL(createGroupsTable);

        String createMembersTable = "CREATE TABLE " + TABLE_MEMBERS + " (" + KEY_MEMBER_ID + " INTEGER PRIMARY KEY, " + KEY_MEMBER_GROUP_ID
                + " INTEGER, " + KEY_MEMBER_USER_ID + " INTEGER, FOREIGN KEY (" + KEY_MEMBER_GROUP_ID + ") REFERENCES " +
                TABLE_GROUPS + "(" + KEY_GROUP_ID + "), FOREIGN KEY (" + KEY_MEMBER_USER_ID + ") REFERENCES " +
                TABLE_FRIENDS + "(" + KEY_FRIEND_ID + "))";
        db.execSQL(createMembersTable);

        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "(" + KEY_MESSAGE_ID + " INTEGER PRIMARY KEY," + KEY_MESSAGE_AUTHOR
                + " TEXT," + KEY_MESSAGE_RECIPENT + " INTEGER," + KEY_MESSAGE_CONTENT + " TEXT," + KEY_MESSAGE_READ + " INTEGER,"
                + KEY_MESSAGE_TIME + " DATETIME DEFAULT (DATETIME('NOW','LOCALTIME'))" + ")";
        db.execSQL(CREATE_MESSAGES_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        // Create tables again
        onCreate(db);
    }

    public void addLocation(String locationID, String lat, String lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LOCATION, locationID);
        values.put(KEY_LAT, lat);
        values.put(KEY_LNG, lng);
        long id = db.insert(TABLE_LOCATIONS, null, values);
        //db.close(); // Closing database connection

        Log.d(TAG, "New location inserted into sqlite: " + id);
    }

    public void addFriendLocation(int locationID, String login, String lat, String lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FRIEND_LOCATION_ID, locationID);
        values.put(KEY_FRIEND_LOGIN, login);
        values.put(KEY_FRIEND_LAT, lat);
        values.put(KEY_FRIEND_LNG, lng);
        long id = db.insert(TABLE_LOCATIONS_FRIENDS, null, values);
        //db.close(); // Closing database connection

        Log.d(TAG, "New friend location inserted into sqlite: " + id);
    }

    public void addMember(String groupID, String userID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        //values.put(KEY_MEMBER_ID, getMemberID()+1);
        values.put(KEY_MEMBER_GROUP_ID, groupID);
        values.put(KEY_MEMBER_USER_ID, userID);

        long id = db.insert(TABLE_MEMBERS, null, values);

        Log.d(TAG, "New group member inserted into SQLite: " + id);

        //db.close();
    }

    public void addGroup(String groupID, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_GROUP_ID, groupID);
        values.put(KEY_GROUP_NAME, name);
        values.put(KEY_GROUP_VISIBLE, "1");

        long id = db.insert(TABLE_GROUPS, null, values);
        //db.close();
        Log.d(TAG, "New group inserted into SQLite: " + id);
    }

    public void addGroup(int gid, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_GROUP_ID, gid);
        values.put(KEY_GROUP_NAME, name);
        values.put(KEY_GROUP_VISIBLE, 1);

        long id = db.insert(TABLE_GROUPS, null, values);
        //db.close();
        Log.d(TAG, "New group inserted into SQLite: " + id);
    }

    public void addMessage(String mess_id, String author, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + KEY_ID_DATABASE + " FROM " + TABLE_LOGIN;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        int user_id = Integer.parseInt(c.getString(0));
        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGE_ID, Integer.parseInt(mess_id));
        values.put(KEY_MESSAGE_AUTHOR, author);
        values.put(KEY_MESSAGE_RECIPENT, user_id);
        values.put(KEY_MESSAGE_CONTENT, content);
        values.put(KEY_MESSAGE_READ, 0);

        long id = db.insert(TABLE_MESSAGES, null, values);
        //db.close();
        Log.d(TAG, "New message inserted into SQLite: " + id);
    }

    /**
     * Storing user details in database
     */
    public void addUser(String userID, String login, String email, String phone, String name, String surname, String photo, String location) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID_DATABASE, userID);
        values.put(KEY_LOGIN, login);
        values.put(KEY_EMAIL, email);
        values.put(KEY_PHONE, phone);
        values.put(KEY_NAME, name);
        values.put(KEY_SURNAME, surname);
        values.put(KEY_PHOTO, photo);
        values.put(KEY_LOCATION, location);
        // Inserting Row
        long id = db.insert(TABLE_LOGIN, null, values);
        //db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    public void addFriend(String userID, String login, String email, String phone, String name, String surname, String photo, String location) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FRIEND_ID_DATABASE, userID);
        values.put(KEY_FRIEND_LOGIN, login);
        values.put(KEY_FRIEND_EMAIL, email);
        values.put(KEY_FRIEND_PHONE, phone);
        values.put(KEY_FRIEND_NAME, name);
        values.put(KEY_FRIEND_SURNAME, surname);
        values.put(KEY_FRIEND_PHOTO, photo);
        values.put(KEY_LOCATION, location);
        // Inserting Row
        long id = db.insert(TABLE_FRIENDS, null, values);
        //db.close(); // Closing database connection

        Log.d(TAG, "New friend inserted into sqlite: " + id);
    }

    //Getting groups details from db
    public ArrayList<Group> getGroupsDetails() {
        ArrayList<Group> groups = new ArrayList<Group>();
        String selectQuery = "SELECT * FROM " + TABLE_GROUPS;
        //+ " WHERE " + KEY_GROUP_NAME+" <> 'Znajomi'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Group g = new Group(Integer.parseInt(cursor.getString(0)), cursor.getString(1), Integer.parseInt(cursor.getString(2)));
            groups.add(g);
            cursor.moveToNext();
        }
        cursor.close();
        //db.close();
        // return friends
        Log.d(TAG, "Fetching groups from Sqlite: " + groups.toString());
        return groups;
    }

    //Get all groups ID which member is friend with fID
    public ArrayList<Group> getMemberGroupsID(int fid) {
        ArrayList<Group> groups = new ArrayList<Group>();
        String selectQuery = "SELECT * FROM " + TABLE_MEMBERS + " WHERE " + KEY_MEMBER_USER_ID + " = " + fid;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Group g = new Group(Integer.parseInt(cursor.getString(1)), "", 1);
            groups.add(g);
            cursor.moveToNext();
        }
        cursor.close();
        //db.close();
        // return friends
        Log.d(TAG, "Fetching groups ID which member is fid from Sqlite: " + groups.toString());
        return groups;
    }

    public Group getGroupDetails(int id) {
        String selectQuery = "SELECT * FROM " + TABLE_GROUPS + " WHERE " + KEY_GROUP_ID + " = " + id;
        Group group = null;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            group = new Group(Integer.parseInt(cursor.getString(0)), cursor.getString(1), Integer.parseInt(cursor.getString(2)));
            cursor.moveToNext();
        }
        cursor.close();
        //db.close();
        // return friends
        Log.d(TAG, "Fetching groups from Sqlite: " + group.toString());
        return group;
    }

    //Getting messages from db
    public ArrayList<Message> getMessages() {
        ArrayList<Message> messages = new ArrayList<Message>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + KEY_ID_DATABASE + " FROM " + TABLE_LOGIN;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        int user_id = Integer.parseInt(c.getString(0));
        c.close();

        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + KEY_MESSAGE_RECIPENT
                + " = " + user_id + " ORDER BY DATETIME(" + KEY_MESSAGE_TIME + ") DESC";
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Message mess = new Message(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(3), Integer.parseInt(cursor.getString(4)), cursor.getString(5));
            messages.add(mess);
            cursor.moveToNext();
        }
        cursor.close();
        //db.close();
        // return friends
        Log.d(TAG, "Fetching messages from Sqlite: " + messages.toString() + "USER ID: " + user_id);
        return messages;
    }

    //Checking if all read
    public boolean allMessagesRead() {
        boolean all_read = true;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + KEY_ID_DATABASE + " FROM " + TABLE_LOGIN;
        Cursor c = db.rawQuery(query, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            int user_id = Integer.parseInt(c.getString(0));

            String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + KEY_MESSAGE_RECIPENT
                    + " = " + user_id + " AND " + KEY_MESSAGE_READ + " = " + 0;
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.getCount() > 0)
                all_read = false;
            cursor.close();
            //db.close();
        }
        c.close();
        return all_read;
    }

    //getting members of group specified with id
    public ArrayList<Friend> getMembersDetails(int gid) {
        ArrayList<Friend> members = new ArrayList<Friend>();
        String selectQuery = "SELECT * FROM " + TABLE_MEMBERS + " WHERE " + KEY_MEMBER_GROUP_ID + " = " + gid;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            members.add(getFriendDetails(cursor.getInt(2), db));
            cursor.moveToNext();
        }
        cursor.close();
        //db.close();
        // return friends
        Log.d(TAG, "Fetching members from Sqlite: " + members.toString());
        return members;
    }

    private Friend getFriendDetails(int fid, SQLiteDatabase db) {
        String selectQuery = "SELECT * FROM " + TABLE_FRIENDS + " WHERE " + KEY_FRIEND_ID_DATABASE + " = " + fid;
        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();
        Friend f = new Friend(c.getInt(1), c.getString(2), c.getString(7));
        return f;
    }

    public HashMap<String, String> getFriendLoginAndPhoto(String friendID) {
        HashMap<String, String> singlefriend = new HashMap<String, String>();
        String selectQuery = "SELECT " + KEY_FRIEND_LOGIN + "," + KEY_FRIEND_PHOTO + " FROM " + TABLE_FRIENDS + " WHERE " + KEY_FRIEND_ID_DATABASE + "=" + friendID;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            singlefriend.put("login", cursor.getString(0));
            singlefriend.put("photo", cursor.getString(1));
        }
        Log.d(TAG, "Fetching friend login and photo from database : cursor size + " + cursor.getCount());
        cursor.close();
        //db.close();
        return singlefriend;
    }

    /**
     * Getting friends data from database
     */
    public List<HashMap<String, String>> getFriendsDetails() {
        List<HashMap<String, String>> friends = new ArrayList<HashMap<String, String>>();

        String selectQuery = "SELECT * FROM " + TABLE_FRIENDS + " ORDER BY " + KEY_FRIEND_LOGIN + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            // cursor.moveToFirst();
            do {
                HashMap<String, String> friend = new HashMap<String, String>();
                friend.put("userID", cursor.getString(1));
                friend.put("login", cursor.getString(2));
                friend.put("email", cursor.getString(3));
                friend.put("phone", cursor.getString(4));
                friend.put("name", cursor.getString(5));
                friend.put("surname", cursor.getString(6));
                friend.put("photo", cursor.getString(7));
                friend.put("location", cursor.getString(8));
                friends.add(friend);
            }

            while (cursor.moveToNext());
        }


        cursor.close();
        //db.close();
        // return friends
        Log.d(TAG, "Fetching friends from Sqlite: " + friends.toString());
        return friends;
    }

    public ArrayList<Friend> getFriends(){
        String query = "SELECT * FROM "+ TABLE_FRIENDS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        ArrayList<Friend> friends = new ArrayList<Friend>();
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            friends.add(new Friend(cursor.getInt(1), cursor.getString(2), cursor.getString(7)));

        return friends;

    }

    public ArrayList<Group> getInvisibleGroups(){
        String query = "SELECT * FROM " + TABLE_GROUPS+" WHERE "+ KEY_GROUP_VISIBLE +" = 0";
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Group> invisibleGroups = new ArrayList<Group>();

        Cursor cursor = db.rawQuery(query, null);
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            invisibleGroups.add(new Group(Integer.parseInt(cursor.getString(0)), cursor.getString(1), Integer.parseInt(cursor.getString(2))));

        return invisibleGroups;
    }

    /**
     * Getting user data from database
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_LOGIN;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("userID", cursor.getString(1));
            user.put("login", cursor.getString(2));
            user.put("email", cursor.getString(3));
            user.put("phone", cursor.getString(4));
            user.put("name", cursor.getString(5));
            user.put("surname", cursor.getString(6));
            user.put("photo", cursor.getString(7));
            user.put("location", cursor.getString(8));
        }
        cursor.close();
        //db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    public HashMap<String, String> getLocationDetails() {
        HashMap<java.lang.String, java.lang.String> locations = new HashMap<java.lang.String, java.lang.String>();
        java.lang.String selectQuery = "SELECT  * FROM " + TABLE_LOCATIONS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            locations.put("locationID", cursor.getString(1));
            locations.put("lat", cursor.getString(2));
            locations.put("lng", cursor.getString(3));

        }
        cursor.close();
        //db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + locations.toString());

        return locations;
    }

    public HashMap<String, String> getFriendLocation(int userID) {
        HashMap<String, String> location = new HashMap<String, String>();
        String selectQuery = "SELECT  " + KEY_FRIEND_LAT + "," + KEY_FRIEND_LNG +
                " FROM " + TABLE_LOCATIONS_FRIENDS + " WHERE " + KEY_FRIEND_LOCATION_ID + "=" + userID;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            location.put("lat", cursor.getString(0));
            location.put("lng", cursor.getString(1));

        }
        cursor.close();
        //db.close();
        Log.d(TAG, "Fetching single friend location from Sqlite: " + location.toString());
        return location;
    }

    public List<HashMap<String, String>> getFriendLocationsFromGroups() {
        //pobierz wspolrzedne znajomych
        List<HashMap<java.lang.String, java.lang.String>> locations = new ArrayList();
        java.lang.String selectQuery =
                "SELECT l." + KEY_FRIEND_LOCATION_ID + ",l." + KEY_FRIEND_LOGIN + ",l." + KEY_FRIEND_LAT + ",l." + KEY_FRIEND_LNG
                        + " FROM " + TABLE_MEMBERS
                        + " m INNER JOIN " + TABLE_LOCATIONS_FRIENDS + " l ON l." + KEY_FRIEND_LOCATION_ID + " =m." + KEY_MEMBER_USER_ID
                        + " LEFT OUTER JOIN " + TABLE_GROUPS + " g ON m." + KEY_MEMBER_GROUP_ID + "=g." + KEY_GROUP_ID
                        + " WHERE g." + KEY_GROUP_VISIBLE + "=?";

        String[] nameTab = {"1"};


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, nameTab);

        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            do {
                HashMap<String, String> location = new HashMap<String, String>();
                location.put("locationID", cursor.getString(0));
                location.put("login", cursor.getString(1));
                location.put("lat", cursor.getString(2));
                location.put("lng", cursor.getString(3));
                locations.add(location);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        // return user
        Log.d(TAG, "MEMBERS Fetching friend location from Sqlite: " + locations.toString());

        return locations;
    }

    public List<HashMap<String, String>> getFriendLocationDetails(int userID) {
        List<HashMap<java.lang.String, java.lang.String>> locations = new ArrayList();
        java.lang.String selectQuery = "SELECT  * FROM " + TABLE_LOCATIONS_FRIENDS
                + " WHERE "+ userID+ "=" + KEY_FRIEND_LOCATION_ID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            do {
                HashMap<String, String> location = new HashMap<String, String>();
                location.put("locationID",cursor.getString(0));
                location.put("login", cursor.getString(1));
                location.put("lat", cursor.getString(2));
                location.put("lng", cursor.getString(3));
                locations.add(location);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        // return user
        Log.d(TAG, "Fetching friend location from Sqlite: " + locations.toString());

        return locations;
    }

    /**
     * Getting user login status return true if rows are there in table
     */
    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + TABLE_LOGIN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        //db.close();
        cursor.close();

        // return row count
        return rowCount;
    }

    public boolean deleteMessage(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean deleted = db.delete(TABLE_MESSAGES, KEY_MESSAGE_ID + " = " + id, null) > 0;
        //db.close();

        return deleted;
    }

    public void deleteMembers(int gid, int fid) {
        SQLiteDatabase db = this.getReadableDatabase();
        int i = db.delete(TABLE_MEMBERS, "("+KEY_MEMBER_GROUP_ID + " = " + gid + " AND " + KEY_MEMBER_USER_ID + " = " + fid+")", null);
        System.out.println(i);
        //db.close();
    }

    public void deleteLocations() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_LOCATIONS, null, null);
        //db.close();

        Log.d(TAG, "Deleted all location info from sqlite");
    }

    /**
     * Re crate database Delete all tables and create them again
     */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_LOGIN, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }

    public void deleteGroups() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_GROUPS, null, null);
        //db.close();

        Log.d(TAG, "Deleted all groups info from sqlite");
    }

    public void deleteFriends() {
        SQLiteDatabase db = this.getWritableDatabase();
        //Delete All Rows
        db.delete(TABLE_FRIENDS, null, null);
        //db.close();

        Log.d(TAG, "Deleted all friends info from sqlite");
    }

    public void deleteMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        //Delete All Rows
        db.delete(TABLE_MESSAGES, null, null);
        //db.close();

        Log.d(TAG, "Deleted all messages info from sqlite");
    }

    public void deleteFriendsLocations() {
        SQLiteDatabase db = this.getWritableDatabase();
        //Delete All Rows
        db.delete(TABLE_LOCATIONS_FRIENDS, null, null);
        //db.close();

        Log.d(TAG, "Deleted all friends locations info from sqlite");
    }

    public void deleteMembers() {
        SQLiteDatabase db = this.getWritableDatabase();
        //Delete All Rows
        db.delete(TABLE_MEMBERS, null, null);
        //db.close();

        Log.d(TAG, "Deleted all members locations info from sqlite");
    }

    private int getMemberID() {
        int id = 1;

        String selectQuery = "SELECT  * FROM " + TABLE_MEMBERS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToLast();
        if (cursor.getCount() > 0) {
            id = Integer.parseInt(cursor.getString(0));
        }
        cursor.close();
        //db.close();

        return id;
    }

    public void setMessageRead(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_MESSAGE_READ, 1);
        db.update(TABLE_MESSAGES, cv, KEY_MESSAGE_ID + " = " + id, null);
        //db.close();
        Log.d(TAG, "Updated message read in sqlite" + id);
    }

    public void updateGroup(int gid, String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_GROUP_NAME, name);
        long id = db.update(TABLE_GROUPS, values, KEY_GROUP_ID + "=" + gid, null);
        //db.close();

        Log.d(TAG, "Updated group info in sqlite" + id);
    }

    public void removeFriend(String friendLogin) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FRIENDS, KEY_FRIEND_LOGIN + " = ? ", new String[]{friendLogin});
        //db.close();
    }

    /*
        public void setGroupVisible(int gid, boolean visible) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_GROUP_VISIBLE, visible);
            long id = db.update(TABLE_GROUPS, values, KEY_GROUP_ID + "=" + gid, null);
            db.close();

            Log.d(TAG, "Updated group visibility info in sqlite" + id);
        }*/
    public void setGroupVisible(String name, String visible) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String[] nameTab = {name};
        values.put(KEY_GROUP_VISIBLE, visible);
        long id = db.update(TABLE_GROUPS, values, KEY_GROUP_NAME + "=?", nameTab);
        //db.close();

        Log.d(TAG, "Updated group visibility info in sqlite" + id);
    }

    public void addMember(int gid, int mid) {
        ContentValues values = new ContentValues();
        values.put(KEY_MEMBER_ID, "" + (getMemberID() + 1));
        values.put(KEY_MEMBER_GROUP_ID, "" + gid);
        values.put(KEY_MEMBER_USER_ID, "" + mid);
        SQLiteDatabase db = this.getWritableDatabase();
        long id = db.insert(TABLE_MEMBERS, null, values);

        //db.close();

        Log.d(TAG, "New Member added in sqlite " + id);
    }

    public ArrayList<Friend> getFriends(int gid) {
        ArrayList<Friend> friends = new ArrayList<Friend>();

        String selectQuery = "SELECT * FROM " + TABLE_FRIENDS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            // cursor.moveToFirst();
            do {
                Friend friend = new Friend(cursor.getInt(1), cursor.getString(2), cursor.getString(7));
                friends.add(friend);
            }
            while (cursor.moveToNext());
        }

        return friends;
    }

    public void deleteGroup(int gid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GROUPS, KEY_GROUP_ID + " = ? ", new String[]{gid + ""});
        deleteMemers(gid, db);
        //db.close();
        //
    }

    private void deleteMemers(int gid, SQLiteDatabase db) {
        db.delete(TABLE_MEMBERS, KEY_MEMBER_GROUP_ID + " = ? ", new String[]{gid + ""});
    }
}