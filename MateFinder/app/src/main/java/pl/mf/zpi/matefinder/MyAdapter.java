package pl.mf.zpi.matefinder;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import pl.mf.zpi.matefinder.helper.SQLiteHandler;

/**
 * Created by root on 12.04.15.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private static final int TYPE_HEADER = 0;  // Declaring Variable to Understand which View is being worked on
    // IF the view under inflation and population is header or Item
    private static final int TYPE_ITEM = 1;

    private String mNavTitles[]; // String Array to store the passed titles Value from MainActivity.java
    private int mIcons[];       // Int Array to store the passed icons resource value from MainActivity.java

    private String name;        //String Resource for header View Name
    private String email;       //String Resource for header view email

    private static Context context;


    // Creating a ViewHolder which extends the RecyclerView View Holder
    // ViewHolder are used to to store the inflated views in order to recycle them

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        int Holderid;

        TextView textView;
        ImageView imageView;
        ImageView profile;
        TextView Name;
        TextView email;
        View item;

        public ViewHolder(View itemView, int ViewType) {                 // Creating ViewHolder Constructor with View and viewType As a parameter
            super(itemView);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);


            // Here we set the appropriate view in accordance with the the view type as passed when the holder object is created

            if (ViewType == TYPE_ITEM) {
                textView = (TextView) itemView.findViewById(R.id.rowText); // Creating TextView object with the id of textView from item_row.xml
                imageView = (ImageView) itemView.findViewById(R.id.rowIcon);// Creating ImageView object with the id of ImageView from item_row.xml
                Holderid = 1;// setting holder id as 1 as the object being populated are of type item row
                item = itemView;
            } else {


                Name = (TextView) itemView.findViewById(R.id.name);         // Creating Text View object from header.xml for name
                email = (TextView) itemView.findViewById(R.id.email);       // Creating Text View object from header.xml for email
                profile = (ImageView) itemView.findViewById(R.id.circleView);// Creating Image view object from header.xml for profile pic
                Holderid = 0;                                                // Setting holder id = 0 as the object being populated are of type header view
            }
        }

        @Override
        public void onClick(View v) {

            switch (getPosition()) {
                case 1:
                    main();
                    break;
                case 2:
                    maps();
                    break;
                case 3:
                    editProfile();
                    break;
                case 4:
                    settings();
            }

        }


    }


    MyAdapter(Context appContext, SQLiteHandler db) { // MyAdapter Constructor with titles and icons parameter
        // titles, icons, name, email, profile pic are passed from the main activity as we
        String[] titles = {"Grupy", "Mapa", "Konto", "Ustawienia"};
        mNavTitles = titles;
        int[] icons = {R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher};
        mIcons = icons;

        HashMap<String, String> user = db.getUserDetails();
        name = user.get("name") + " " + user.get("surname");
        email = user.get("email");

        this.context = appContext;

    }


    //Below first we ovverride the method onCreateViewHolder which is called when the ViewHolder is
    //Created, In this method we inflate the item_row.xml layout if the viewType is Type_ITEM or else we inflate header.xml
    // if the viewType is TYPE_HEADER
    // and pass it to the view holder

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false); //Inflating the layout

            ViewHolder vhItem = new ViewHolder(v, viewType); //Creating ViewHolder and passing the object of type view

            return vhItem; // Returning the created object

            //inflate your layout and pass it to view holder

        } else if (viewType == TYPE_HEADER) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header, parent, false); //Inflating the layout

            ViewHolder vhHeader = new ViewHolder(v, viewType); //Creating ViewHolder and passing the object of type view

            return vhHeader; //returning the object created


        }
        return null;

    }

    //Next we override a method which is called when the item in a row is needed to be displayed, here the int position
    // Tells us item at which position is being constructed to be displayed and the holder id of the holder object tell us
    // which view type is being created 1 for item row
    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
        int i = 1;
        if (context instanceof MapsActivity)
            i = 2;
        else if (context instanceof EditProfileActivity)
            i = 3;
        else if (context instanceof SettingsActivity)
            i = 4;
        if (holder.Holderid == 1) {                              // as the list view is going to be called after the header view so we decrement the
            // position by 1 and pass it to the holder while setting the text and image
            holder.textView.setText(mNavTitles[position - 1]); // Setting the Text with the array of our Titles
            holder.imageView.setImageResource(mIcons[position - 1]);// Settimg the image with array of our icons
            if (position == i)
                holder.item.setBackgroundColor(context.getResources().getColor(R.color.kol6));
        } else {

            holder.profile.setImageBitmap(readProfileImage());           // Similarly we set the resources for header view
            holder.Name.setText(name);
            holder.email.setText(email);
        }
    }

    // This method returns the number of items present in the list
    @Override
    public int getItemCount() {
        return mNavTitles.length + 1; // the number of items in the list will be +1 the titles including the header view.
    }


    // Witht the following method we check what type of view is being passed
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }


    private Bitmap readProfileImage() {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File my_path = new File(directory, "profile.jpg");
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            fis = new FileInputStream(my_path);
            bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    // Edytuj profil
    private static void editProfile() {
        // Launching the login activity
        if (!(context instanceof EditProfileActivity)) {
            Intent intent = new Intent(context, EditProfileActivity.class);
            context.startActivity(intent);
        }

    }

    // Ustawienia
    private static void settings() {
        // Launching the login activity
        if (!(context instanceof SettingsActivity)) {
            Intent intent = new Intent(context, SettingsActivity.class);
            context.startActivity(intent);
        }
        //finish(); NIE DAWAJCIE TEGO FINISH() bo potem przy przycisku powrotu
        // wychodzi z aplikacji zamiast wracac do poprzedniego ekranu!!!
    }

    private static void maps() {
        if (!(context instanceof MapsActivity)) {
            Intent intent = new Intent(context, MapsActivity.class);
            context.startActivity(intent);
            ((Activity) context).finish(); //tutaj ma byc
        }
    }

    private static void main() {
        if (!(context instanceof MainActivity)) {
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
        }
    }

}