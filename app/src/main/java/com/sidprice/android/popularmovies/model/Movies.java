package com.sidprice.android.popularmovies.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.sidprice.android.popularmovies.BuildConfig;
import com.sidprice.android.popularmovies.R;
import com.sidprice.android.popularmovies.database.MoviesDatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
    This class encapsulates access to the online database and exposes
    the data as an array of Movie objects
 */
public class Movies {
    private static final String TAG = "Movies";
    private ArrayList<Movie> mMovies ;
    private static String apiKey = BuildConfig.THE_MOVIE_DATABASE_API_KEY ;
    private static String baseURL = "http://api.themoviedb.org/3/" ;
    private static String imageSize = "w185" ;
    private static String posterImagesBaseUrl = "" ;
    /*
        This method recieves a JSON string that is the reply from the Online
        Movie Database and initializes the array of Movie objects using it.
     */
    public void loadMovies (Context context, String jsonStringMovies) {
        /*
            Double check that the input is not a null string
         */
        if ( jsonStringMovies != null) {

            try {
                mMovies = new ArrayList<Movie>() ;
                JSONObject rootObject = new JSONObject(jsonStringMovies) ;
                /*
                    Query the root object for the "results" array
                 */
                JSONArray resultsArray = rootObject.getJSONArray("results") ;
                /*
                    Iterate over the array creating Movie objects into the
                    Movies array
                 */
                for ( int i = 0 ; i < resultsArray.length() ; i++) {
                    /*
                        Get the data we need from the current json object
                     */
                    JSONObject movieRoot = resultsArray.getJSONObject(i) ;
                    Integer id = movieRoot.getInt(context.getString(R.string.json_id)) ;
                    String  original_title = movieRoot.getString(context.getString(R.string.json_original_title)) ;
                    String  poster_path = movieRoot.getString(context.getString(R.string.json_poster_path)) ;
                    String  synopsis = movieRoot.getString(context.getString(R.string.json_overview)) ;
                    String  user_rating = movieRoot.getString(context.getString(R.string.json_vote_average)) ;
                    String  release_date = movieRoot.getString(context.getString(R.string.json_release_date)) ;
                    MoviesDatabaseHelper dbHelper = new MoviesDatabaseHelper(context) ;
                    SQLiteDatabase db =  dbHelper.getWritableDatabase() ;
                    Boolean isFavorite = dbHelper.IsFavorite(db, id) ;
                    db.close();
                    Movie newMovie = new Movie(id, original_title, poster_path, synopsis, user_rating, release_date, isFavorite) ;
                    mMovies.add(newMovie);
                }
                Log.d(TAG, "loadMovies: ... loaded from online data source");
            }
            catch (JSONException jsonException) {
                /*
                    Some kind of error so ensure the
                    Movies array is empty and does not
                    have partial data
                 */
                Log.d(TAG, "loadMovies: Failed to load movies from online source. " + jsonException.toString());
                mMovies = null;
            }
        }
    }
    /*
        This method is used to load the movies from the local database of user favorites
     */
    public void loadMovies( Context context) {
        Log.d(TAG, "loadMovies: Load the movies from the local user favorite database");
        MoviesDatabaseHelper dbHelper = new MoviesDatabaseHelper(context) ;
        SQLiteDatabase db =  dbHelper.getWritableDatabase() ;
        String  jsonMovies = dbHelper.getMoviesAsJSON(context, db) ;
        loadMovies(context, jsonMovies);
        db.close();
    }
    /*
        This method receives the configuration json string that
        was loaded from the Online Movie Database
     */
    public void configureMovies( String jsonStringConfiguration) {
        /*
            Check for non-null input
         */
        if (jsonStringConfiguration != null) {
            try {
                JSONObject  rootObject = new JSONObject(jsonStringConfiguration) ;
                JSONObject  imagesObject = rootObject.getJSONObject("images") ;
                posterImagesBaseUrl = imagesObject.getString("base_url") ;
            }
            catch (JSONException jsonException) {
                /*
                    Some kind of error ... will leave config in defaults
                 */
            }
        }
    }
    /*
        Return the base URL for access of the Online Movie Database
     */
    public static String tmdBaseUrl() {
        return baseURL ;
    }
    /*
        Return the API key for the Online Movie Database
     */
    public static String tmdApiKey() {
        return apiKey ;
    }
    /*
        Return the nase url for image loading
     */
    public static String tmdPosterBaseUrl() {
        return posterImagesBaseUrl ;
    }
    /*
        Get the array of movies
     */
    public ArrayList<Movie> getMovies() {
        return mMovies ;
    }
    /*
        Get tne number of movies
     */
    public int getCount() {
        if ( mMovies == null ) {
            return 0 ;
        }
        return mMovies.size();
    }
    /*
        Get movie at the given position
     */
    public Movie getMovie(Integer position) {
        if ( position < mMovies.size()) {
            return mMovies.get(position) ;
        } else {
            return null ;
        }
    }
}
