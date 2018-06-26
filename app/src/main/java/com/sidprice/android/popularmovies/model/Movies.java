package com.sidprice.android.popularmovies.model;

import android.content.Context;
import android.util.Log;

import com.sidprice.android.popularmovies.BuildConfig;
import com.sidprice.android.popularmovies.R;
import com.sidprice.android.popularmovies.database.MoviesDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
    This class encapsulates access to the online database and exposes
    the data as an array of Movie objects
 */
public class Movies {
    private MoviesDatabase  mDb ;
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
                    String id = movieRoot.getString(context.getString(R.string.json_id)) ;
                    String  original_title = movieRoot.getString(context.getString(R.string.json_original_title)) ;
                    String  poster_path = movieRoot.getString(context.getString(R.string.json_poster_path)) ;
                    String  synopsis = movieRoot.getString(context.getString(R.string.json_overview)) ;
                    String  user_rating = movieRoot.getString(context.getString(R.string.json_vote_average)) ;
                    String  release_date = movieRoot.getString(context.getString(R.string.json_release_date)) ;
                    Movie newMovie = new Movie(id, original_title, poster_path, synopsis, user_rating, release_date, null) ;
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
        /*
            If we do not have a adatabase instance, get one
         */
        if ( mDb == null ) {
            mDb = MoviesDatabase.getInstance(context) ;
        }
        List<Movie> theMovies = mDb.moviesDao().loadAllMovies() ;
        String  jsonMovies = getMoviesAsJSON(context, theMovies) ;
        loadMovies(context, jsonMovies);
//        db.close();
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
    /*
    The following method reads the movies from the passed array and returns a JSON string describing
    the movies in the same format as the online movie requests. Allowing the
    the same adapter to be used for botj local and on;ine data
     */
    public String getMoviesAsJSON( Context context,  List<Movie> theMovies ) {
        String returnString = null;
        JSONObject jsonRoot = new JSONObject();
        JSONArray jsonResults = new JSONArray();
        try {
            /*
                Fake the root element key/value pairs
             */
            jsonRoot.put(context.getString(R.string.json_page), 1);
            jsonRoot.put(context.getString(R.string.json_total_results), 1000);

            for (int i = 0; i < theMovies.size(); i++) {
                Movie movie = theMovies.get(i);

                JSONObject jsonMovie = new JSONObject();
                jsonMovie.put(context.getString(R.string.json_id),movie.getID());
                jsonMovie.put(context.getString(R.string.json_original_title), movie.getOriginalTitle());
                jsonMovie.put(context.getString(R.string.json_poster_path), "");      // This value will indicate the image is local
                jsonMovie.put(context.getString(R.string.json_overview), movie.getSynopsis());
                jsonMovie.put(context.getString(R.string.json_vote_average), movie.getUserRating());
                jsonMovie.put(context.getString(R.string.json_release_date), movie.getReleaseDate());
                    /*
                        Add movie object to results array
                     */
                jsonResults.put(jsonMovie);
            }
            /*
                Add the array to the root object
             */
            jsonRoot.put("results", jsonResults);
            /*
                Convert the JSON object tp string for return to caller
             */
            returnString = jsonRoot.toString();
        } catch (JSONException ex) {
            Log.d(TAG, "getMoviesAsJSON: exception -> " + ex.toString());
        }
        return returnString;
    }
}
