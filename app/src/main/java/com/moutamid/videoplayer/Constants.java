package com.moutamid.videoplayer;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Constants {

    /*public static FirebaseAuth auth() {
        return FirebaseAuth.getInstance();
    }*/

    public static DatabaseReference databaseReference() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("VideoPlayer");
        db.keepSynced(true);
        return db;
    }
}
