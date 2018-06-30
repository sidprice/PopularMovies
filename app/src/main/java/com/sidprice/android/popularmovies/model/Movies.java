package com.sidprice.android.popularmovies.model;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.util.Log;

import com.sidprice.android.popularmovies.BuildConfig;
import com.sidprice.android.popularmovies.R;
import com.sidprice.android.popularmovies.database.MoviesRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
    This class encapsulates access to the online and local databases and exposes
    the data as an array of Movie objects
 */
public class Movies {
    // private MoviesDatabase  mDb ;
    private static final String TAG = "Movies";
    private ArrayList<Movie> mMovies ;
    private LiveData<List<Movie>> mFavoriteMovies ;
    private static String apiKey = BuildConfig.THE_MOVIE_DATABASE_API_KEY ;
    private static String baseURL = "http://api.themoviedb.org/3/" ;
    private static String imageSize = "w185" ;
    private static String posterImagesBaseUrl = "" ;

    private static String   json_original_title = "original_title" ;
    private static String   json_poster_path = "poster_path" ;
    private static String   json_overview = "overview" ;
    private static String   json_vote_average = "vote_average" ;
    private static String   json_release_date = "release_date" ;
    private static  String  json_page = "page" ;
    private static  String  json_total_results = "total_results" ;
    private static  String  json_id = "id" ;

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
                    String id = movieRoot.getString(json_id) ;
                    String  original_title = movieRoot.getString(json_original_title) ;
                    String  poster_path = movieRoot.getString(json_poster_path) ;
                    String  synopsis = movieRoot.getString(json_overview) ;
                    String  user_rating = movieRoot.getString(json_vote_average) ;
                    String  release_date = movieRoot.getString(json_release_date) ;
                    Movie newMovie = new Movie(id, original_title, poster_path, synopsis, user_rating, release_date, null) ;
                    mMovies.add(newMovie);
                }
                Log.d(TAG, "loadMovies: ... loaded");
            }
            catch (JSONException jsonException) {
                /*
                    Some kind of error so ensure the
                    Movies array is empty and does not
                    have partial data
                 */
                Log.d(TAG, "loadMovies: Failed to load movies from input JSON string. " + jsonException.toString());
                mMovies = null;
            }
        }
    }

    public void updateFromDatabase(Context context, List<Movie> theMovies) {
        Log.d(TAG, "updateFromDatabase: called");
                String  jsonMovies = getMoviesAsJSON(context, theMovies) ;
                loadMovies(context, jsonMovies);
            /*
                This method is called to process favorites and the database holds the
                bitmap images for display. We need to copy those images to the instance
                variable mMovies
             */
                for ( int i = 0 ; i < theMovies.size() ; i++) {
                    /*
                        Iterate over the instance variable movie array and copy
                        images to the matched movie ID
                     */
                    for ( int j = 0 ; j< mMovies.size() ; j++ ) {
                        Movie   srcMovie = theMovies.get(i) ;
                        Movie   dstMovie = mMovies.get(j) ;
                        if ( srcMovie.getID().equals(dstMovie.getID() ) ) {
                            //
                            dstMovie.setPosterWidth(srcMovie.getPosterWidth());
                            dstMovie.setPosterHeight(srcMovie.getPosterHeight());
                            dstMovie.setMoviePoster(srcMovie.getMoviePoster());
                            dstMovie.setFavorite(true) ;
                            break;      // Current movie processed
                        }
                    }
                }
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
            jsonRoot.put(json_page, 1);
            jsonRoot.put(json_total_results, 1000);

            for (int i = 0; i < theMovies.size(); i++) {
                Movie movie = theMovies.get(i);

                JSONObject jsonMovie = new JSONObject();
                jsonMovie.put(json_id,movie.getID());
                jsonMovie.put(json_original_title, movie.getOriginalTitle());
                jsonMovie.put(json_poster_path, "");      // This value will indicate the image is local
                jsonMovie.put(json_overview, movie.getSynopsis());
                jsonMovie.put(json_vote_average, movie.getUserRating());
                jsonMovie.put(json_release_date, movie.getReleaseDate());
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
