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
    private static final String TABLE_SETTINGS = "settings";
    private static final String TABLE_FRIENDS = "friends";
    private static final String TABLE_LOCATIONS_FRIENDS = "friends_locations";
    private static final String TABLE_GROUPS = "groups";
    private static final String TABLE_MEMBERS = "members";

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

    //settings table columns name
    private static final String KEY_INTERNET_LIMIT = "internet";
    private static final String KEY_NOTIFICATION_SOUND = "sound";
    private static final String KEY_USER_NAVIGATION = "navigation";
    private static final String KEY_LAYOUT = "layout";
    private static final String KEY_SEARCH_RADIUS = "radius";

    // Friends table column names
    private static final String KEY_FRIEND_ID = "id";
    private static final String KEY_FRIEND_ID_DATABASE = "userID";
    private static final String KEY_FRIEND_LOGIN = "login";
    private static final String KEY_FRIEND_EMAIL = "email";
    private static final String KEY_FRIEND_PHONE = "phone";
    private static final String KEY_FRIEND_NAME = "name";
    private static final String KEY_FRIEND_SURNAME = "surname";
    private static final String KEY_FRIEND_PHOTO = "photo";

    private static final String KEY_FRIEND_LOCATION_ID = "id";
    private static final String KEY_FRIEND_LOCATION_ID_DATABASE = "locationID";
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

        String CREATE_SETTINGS_TABLE = "CREATE TABLE " + TABLE_SETTINGS + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_INTERNET_LIMIT + " INTEGER, " +
                KEY_NOTIFICATION_SOUND + " INTEGER, " + KEY_USER_NAVIGATION + " INTEGER, " + KEY_LAYOUT + " INTEGER, " + KEY_SEARCH_RADIUS + " INTEGER)";
        db.execSQL(CREATE_SETTINGS_TABLE);

        String CREATE_FRIENDS_TABLE = "CREATE TABLE " + TABLE_FRIENDS + "("
                + KEY_FRIEND_ID + " INTEGER PRIMARY KEY," + KEY_FRIEND_ID_DATABASE + " TEXT," + KEY_FRIEND_LOGIN + " TEXT,"
                + KEY_FRIEND_EMAIL + " TEXT," + KEY_FRIEND_PHONE + " TEXT," + KEY_FRIEND_NAME + " TEXT,"
                + KEY_FRIEND_SURNAME + " TEXT," + KEY_FRIEND_PHOTO + " TEXT," + KEY_LOCATION + " TEXT" + ")";
        db.execSQL(CREATE_FRIENDS_TABLE);

        String CREATE_FRIENDS_LOCATIONS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS_FRIENDS + "("
                + KEY_FRIEND_LOCATION_ID + " INTEGER PRIMARY KEY," + KEY_FRIEND_LOCATION_ID_DATABASE + " TEXT," + KEY_FRIEND_LAT
                + " TEXT," + KEY_FRIEND_LNG + " TEXT" + ")";
        db.execSQL(CREATE_FRIENDS_LOCATIONS_TABLE);

        String createGroupsTable = "CREATE TABLE " + TABLE_GROUPS + "(" + KEY_GROUP_ID + " INTEGER PRIMARY KEY, " +
                KEY_GROUP_NAME + " TEXT, " + KEY_GROUP_VISIBLE + " INTEGER)" ;
        db.execSQL(createGroupsTable);

        String createMembersTable = "CREATE TABLE " + TABLE_MEMBERS + " (" + KEY_MEMBER_ID + " INTEGER PRIMARY KEY, " + KEY_MEMBER_GROUP_ID
                + " INTEGER, " + KEY_MEMBER_USER_ID + " INTEGER, FOREIGN KEY (" + KEY_MEMBER_GROUP_ID + ") REFERENCES " +
                TABLE_GROUPS + "(" + KEY_GROUP_ID + "), FOREIGN KEY (" + KEY_MEMBER_USER_ID + ") REFERENCES " +
                TABLE_FRIENDS + "(" + KEY_FRIEND_ID + "))";
        db.execSQL(createMembersTable);


        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
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
        db.close(); // Closing database connection

        Log.d(TAG, "New location inserted into sqlite: " + id);
    }
    public void addFriendLocation(String locationID, String lat, String lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FRIEND_LOCATION_ID_DATABASE, locationID);
        values.put(KEY_FRIEND_LAT, lat);
        values.put(KEY_FRIEND_LNG, lng);
        long id = db.insert(TABLE_LOCATIONS_FRIENDS, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New friend location inserted into sqlite: " + id);
    }

    public void addSettings(String internet, String notification, String navigation, String layout, String radius) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_INTERNET_LIMIT, internet);
        values.put(KEY_NOTIFICATION_SOUND, notification);
        values.put(KEY_USER_NAVIGATION, navigation);
        values.put(KEY_LAYOUT, layout);
        values.put(KEY_SEARCH_RADIUS, radius);
        long id = db.insert(TABLE_LOCATIONS, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New location inserted into sqlite: " + id);
    }
    private void addMember(int gid, int uid){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_MEMBER_ID, getMemberID()+1);
        values.put(KEY_MEMBER_GROUP_ID, gid);
        values.put(KEY_MEMBER_USER_ID, uid);

        long id = db.insert(TABLE_MEMBERS, null, values);

        Log.d(TAG, "New grop member inserted into SQLite: " + id);

        db.close();
    }
    public void addGroup(String name){
        int gid = getGroupID();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_GROUP_ID, gid + 1);
        values.put(KEY_GROUP_NAME, name);
        values.put(KEY_GROUP_VISIBLE, 1);

        long id = db.insert(TABLE_GROUPS, null, values);
        db.close();
        Log.d(TAG, "New group inserted into SQLite: " + id);
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
        db.close(); // Closing database connection

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
        db.close(); // Closing database connection

        Log.d(TAG, "New friend inserted into sqlite: " + id);
    }
    //Getting groups details from db
    public List<HashMap<String, String>> getGroupsDetails(){
        List<HashMap<String, String>> groups = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT * FROM " + TABLE_GROUPS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        int i=0;
        while(!cursor.isAfterLast()) {
            HashMap<String, String> g = new HashMap<String, String>(3);
            g.put("groupID", cursor.getString(0));
            g.put("name", cursor.getString(1));
            g.put("visible", cursor.getString(2));
            groups.add(g);
            cursor.moveToNext();
            i++;
        }
        cursor.close();
        db.close();
        // return friends
        Log.d(TAG, "Fetching groups from Sqlite: " + groups.toString());
        return groups;
    }
    //getting members of group specified with id
    public List<HashMap<String, String>> getMembersDetails(String gid){
        List<HashMap<String, String>> members = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT * FROM " + TABLE_MEMBERS + "WHERE " + KEY_MEMBER_GROUP_ID + " = " + gid;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);
        // Move to first row
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            HashMap<String, String> m = new HashMap<String, String>(3);
            m.put("memberID", cursor.getString(0));
            m.put("groupID", cursor.getString(1));
            m.put("userID", cursor.getString(2));
            members.add(m);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        // return friends
        Log.d(TAG, "Fetching members from Sqlite: " + members.toString());
        return members;
    }

    /**
     * Getting friends data from database
     */
    public List<HashMap<String, String>> getFriendsDetails() {
        List<HashMap<String, String>> friends = new ArrayList<HashMap<String, String>>();

        String selectQuery = "SELECT * FROM " + TABLE_FRIENDS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();

        if (cursor.getCount() > 0)
        {
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
                friends.add(friend);            }

            while (cursor.moveToNext());
        }


        cursor.close();
        db.close();
        // return friends
        Log.d(TAG, "Fetching friends from Sqlite: " + friends.toString());
        return friends;
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
        db.close();
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
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + locations.toString());

        return locations;
    }

    public List<HashMap<String, String>> getFriendLocationDetails() {
        List<HashMap<java.lang.String, java.lang.String>> locations = new ArrayList();
        java.lang.String selectQuery = "SELECT  * FROM " + TABLE_LOCATIONS_FRIENDS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            do {
                HashMap<String,String> location = new HashMap<String,String>();
                location.put("locationID", cursor.getString(1));
                location.put("lat", cursor.getString(2));
                location.put("lng", cursor.getString(3));
                locations.add(location);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching friend location from Sqlite: " + locations.toString());

        return locations;
    }
    public HashMap<String, String> getSettings() {
        HashMap<String, String> settings = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_SETTINGS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            settings.put("internet", cursor.getString(1));
            settings.put("notification", cursor.getString(2));
            settings.put("navigation", cursor.getString(3));
            settings.put("layout", cursor.getString(4));
            settings.put("radius", cursor.getString(5));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + settings.toString());

        return settings;
    }

    /**
     * Getting user login status return true if rows are there in table
     */
    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + TABLE_LOGIN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        // return row count
        return rowCount;
    }

    public void deleteLocations() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_LOCATIONS, null, null);
        db.close();

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


    public void deleteSettings() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_SETTINGS, null, null);
        db.close();

        Log.d(TAG, "Deleted all location info from sqlite");
    }

    public void deleteGroups() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_GROUPS, null, null);
        db.close();

        Log.d(TAG, "Deleted all groups info from sqlite");
    }

    public void deleteFriends(){
        SQLiteDatabase db = this.getWritableDatabase();
        //Delete All Rows
        db.delete(TABLE_FRIENDS,null,null);
        db.close();

        Log.d(TAG, "Deleted all friends info from sqlite");
    }
    public void deleteFriendsLocations(){
        SQLiteDatabase db = this.getWritableDatabase();
        //Delete All Rows
        db.delete(TABLE_LOCATIONS_FRIENDS,null,null);
        db.close();

        Log.d(TAG, "Deleted all friends locations info from sqlite");
    }
    private int getGroupID(){
        int id = 1;

        String selectQuery = "SELECT  * FROM " + TABLE_GROUPS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToLast();
        if (cursor.getCount() > 0) {
            id = Integer.parseInt(cursor.getString(0));
        }
        cursor.close();
        db.close();

        return id;
    }

    private int getMemberID(){
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
        db.close();

        return id;
    }

    public void updateGroup(int gid, String name){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_GROUP_NAME, name);
        long id = db.update(TABLE_GROUPS, values, KEY_GROUP_ID + "=" + gid, null);
        db.close();

        Log.d(TAG, "Updated group info in sqlite" + id);
    }

    public void removeFriend(String friendLogin){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FRIENDS,KEY_FRIEND_LOGIN + " = ? ", new String[]{friendLogin});
        db.close();
    }

}