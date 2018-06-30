package com.sidprice.android.popularmovies.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.sidprice.android.popularmovies.model.Movie;

import java.util.List;

public class MoviesRepository {
    private static final String TAG = "MovieRepositories";
    private MoviesDao               mMoviesDao ;
    private LiveData<List<Movie>>   mMovies ;

    public MoviesRepository(Application application) {
        MoviesDatabase db = MoviesDatabase.getInstance(application.getApplicationContext()) ;
        mMoviesDao = db.moviesDao() ;
        mMovies = mMoviesDao.loadAllMovies() ;
    }

    public LiveData<List<Movie>>   loadAllMovies() {
        return mMovies ;
    }

    public void insert ( Movie movie) {
        new insertAsyncTask(mMoviesDao) ;
    }

    public void delete ( Movie movie ) {

    }
    private static class insertAsyncTask extends AsyncTask<Movie, Void, Void> {

        private MoviesDao mAsyncTaskDao;

        insertAsyncTask(MoviesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Movie... params) {
            mAsyncTaskDao.insertMovie(params[0]);
            return null;
        }
    }
    private static class deleteAsyncTask extends AsyncTask<Movie, Void, Void> {

        private MoviesDao mAsyncTaskDao;

        deleteAsyncTask(MoviesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Movie... params) {
            mAsyncTaskDao.deleteMovie(params[0]);
            return null;
        }
    }
}
