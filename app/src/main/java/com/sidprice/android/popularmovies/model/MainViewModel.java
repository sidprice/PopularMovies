package com.sidprice.android.popularmovies.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.sidprice.android.popularmovies.database.MoviesRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private static final String TAG = "MainViewModel";
    private MoviesRepository mRepository ;
    private LiveData<List<Movie>>   mMovies ;

    public MainViewModel(@NonNull Application application) {
        super(application);
        mRepository = new MoviesRepository(application) ;
        mMovies = mRepository.loadAllMovies() ;
    }

    public void insert(Movie movie ) {
        mRepository.insert(movie);
    }

    public void delete(Movie movie) {
        mRepository.delete(movie);
    }

    public LiveData<List<Movie>> getMovies() {
        return mMovies;
    }
}
