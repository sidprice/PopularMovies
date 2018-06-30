package com.sidprice.android.popularmovies.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

import com.sidprice.android.popularmovies.model.Movie;

import java.util.List;


@Dao
public interface MoviesDao {
    @Query("SELECT * from movies")
    LiveData<List<Movie>> loadAllMovies() ;

    @Query("SELECT * FROM movies WHERE ID = :movieID")
    public  Movie getMovieById(String movieID) ;

    @Insert(onConflict = REPLACE)
    void insertMovie(Movie movie) ;

    @Update(onConflict = REPLACE)
    void updateMovie(Movie movie) ;

    @Delete
    void deleteMovie(Movie movie) ;
}
