package de.hawlandshut.studyla;

import android.app.Application;

import de.hawlandshut.studyla.roomfinder.model.RoomDatabase;

public class StudyLa extends Application {
    private RoomDatabase mDatabase;

    @Override
    public void onCreate() {
        mDatabase = new RoomDatabase(this);
        super.onCreate();
    }

    public RoomDatabase getRoomDatabase() {
        return mDatabase;
    }
}
