package com.sidprice.android.popularmovies.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

import com.sidprice.android.popularmovies.model.Movie;

@Database(entities = {Movie.class}, version = 1, exportSchema = false)
public abstract class MoviesDatabase extends RoomDatabase {
    private static final String TAG = MoviesDatabase.class.getSimpleName() ;
    private static final Object LOCK = new Object() ;
    private static final String DATABASE_NAME = "movies" ;
    private static MoviesDatabase sInstance ;

    public static MoviesDatabase getInstance(Context context) {
        if ( sInstance == null ) {
            synchronized (LOCK) {
                Log.d(TAG, "getInstance: Creating new database");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        MoviesDatabase.class, MoviesDatabase.DATABASE_NAME)
                        .allowMainThreadQueries()
                        .build() ;
            }
        }
        Log.d(TAG, "getInstance: Getting the database instance");
        return sInstance ;
    }

    public abstract MoviesDao moviesDao() ;
}
