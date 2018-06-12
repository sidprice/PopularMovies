package com.sidprice.android.popularmovies.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;

import com.sidprice.android.popularmovies.R;
import com.sidprice.android.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import static com.sidprice.android.popularmovies.R.string.json_id;

public class MoviesDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "MoviesDatabaseHelper";
    private final static int    DATABASE_VERSION = 3 ;

    private final static String DATABASE_NAME = "MoviesDatabase" ;
    private final static String MOVIES_TABLE_NAME = "MoviesTable" ;
    /*
        Columns
     */
    private final static String ID = "ID" ;                 // Database Row ID and Primary, autoincrement key
    private final static String MOVIE_ID = "Move_ID" ;      // The ID in the online database
    private final static String MOVIE_TITLE = "Title" ;
    private final static String MOVIE_SYNOPSIS = "Synopsis" ;
    private final static String MOVIE_POSTER    = "Poster" ;
    private final static String MOVIE_POSTER_HEIGHT = "PosterHeight" ;
    private final static String MOVIE_POSTER_WIDTH = "PosterWidth" ;
    private final static String MOVIE_RELEASE_DATE = "Release_Date" ;
    private final static String MOVIE_RATING = "Rating";
    /*
        Query string to create the database
     */
    private final static String MOVIE_CREATE_DATABASE =
            "CREATE TABLE " + MOVIES_TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MOVIE_ID + " VARCHAR(255), "
                + MOVIE_TITLE + " VARCHAR(255), "
                + MOVIE_SYNOPSIS + " VARCHAR(1024), "
                + MOVIE_POSTER + " BLOB, "
                + MOVIE_POSTER_HEIGHT + " INTEGER, "
                + MOVIE_POSTER_WIDTH + " INTEGER, "
                + MOVIE_RELEASE_DATE + " DATETIME, "
                + MOVIE_RATING + " VARCHAR(32)"
                + ")" ;

    public MoviesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(MOVIE_CREATE_DATABASE);
        } catch (Exception e) {
            Log.d(TAG, "onCreate: " + e.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MOVIES_TABLE_NAME);
        onCreate(db);
    }
    /*
        Add the passed movie to the database
     */
    public long Insert(SQLiteDatabase db, Movie movie, Bitmap posterBitmap) {
        int posterHeight, posterWidth ;
        posterHeight = posterBitmap.getHeight() ;
        posterWidth = posterBitmap.getWidth() ;
        ContentValues   contentValues = new ContentValues() ;
        contentValues.put(MOVIE_ID, movie.getID()) ;
        contentValues.put(MOVIE_TITLE, movie.getOriginalTitle()) ;
        contentValues.put(MOVIE_SYNOPSIS, movie.getSynopsis()) ;
        /*
            Convert the input Bitmap to a byte array for addition to Database
         */
        ByteBuffer  byteBuffer = ByteBuffer.allocate(posterBitmap.getByteCount()) ;
        posterBitmap.copyPixelsToBuffer(byteBuffer);
        byteBuffer.rewind() ;
        byte[]  data = new byte[byteBuffer.remaining()] ;
        byteBuffer.get(data) ;
        contentValues.put(MOVIE_POSTER, data) ;
        contentValues.put(MOVIE_POSTER_HEIGHT, posterHeight) ;
        contentValues.put(MOVIE_POSTER_WIDTH, posterWidth) ;
        contentValues.put(MOVIE_RELEASE_DATE, movie.getReleaseDate()) ;
        contentValues.put(MOVIE_RATING, movie.getUserRating()) ;
        long id = db.insert(MOVIES_TABLE_NAME, null, contentValues);
        return id ;
    }
    /*
        Delete the passed movie from the database
     */
    public int Delete( SQLiteDatabase db, Movie movie ) {
        String [] whereArgs = { String.valueOf(movie.getID()) } ;
        return db.delete(MOVIES_TABLE_NAME, MOVIE_ID + "=?", whereArgs) ;
    }
    /*
        Is the movie with the given ID a favorite
     */
    public boolean IsFavorite( SQLiteDatabase db,  int ID) {
        boolean     result = false ;
        String[]    columns = { MOVIE_ID } ;
        String[]    selectionArgs = { String.valueOf(ID) } ;

        try {
            Cursor  cursor =  db.query(MOVIES_TABLE_NAME, columns, MOVIE_ID + "=?", selectionArgs, null, null, null) ;
            if ( cursor != null ) {
                if ( cursor.getCount() != 0 ) {
                    result = true ;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "IsFavorite: " + e.toString());
        }
        return result ;
    }
    /*
        Recover the poster Bitmap for the passed movie
     */
    public Bitmap GetPosterBitmap( SQLiteDatabase db,  Movie movie) {
        Bitmap      posterImage = null ;
        String[]    columns = { MOVIE_POSTER_HEIGHT, MOVIE_POSTER_WIDTH, MOVIE_POSTER } ;
        String[]    selectionArgs = { String.valueOf(movie.getID()) } ;

        Cursor  cursor =  db.query(MOVIES_TABLE_NAME, columns, MOVIE_ID + "=?", selectionArgs, null, null, null) ;
        if ( cursor != null ) {
            if (cursor.getCount() != 0) {
                int columnIndex = cursor.getColumnIndex(MOVIE_POSTER) ;
                int posterHeightIndex = cursor.getColumnIndex(MOVIE_POSTER_HEIGHT) ;
                int posterWidthIndex = cursor.getColumnIndex(MOVIE_POSTER_WIDTH) ;
                cursor.moveToFirst();
                try {
                    byte[]  data = cursor.getBlob(columnIndex) ;
                    int     posterWidth, posterHeight ;
                    posterWidth = cursor.getInt(posterWidthIndex) ;
                    posterHeight = cursor.getInt(posterHeightIndex) ;
                    posterImage = Bitmap.createBitmap((DisplayMetrics)null, posterWidth, posterHeight, Bitmap.Config.ARGB_8888) ;
                    ByteBuffer  buffer = ByteBuffer.wrap(data) ;
                    posterImage.copyPixelsFromBuffer(buffer);
                } catch (Exception e) {
                    Log.d(TAG, "GetPosterBitmap: " + e.toString());
                }
            }
        }
        return posterImage ;
    }
    /*
        The following method reads the movies from the database and returns a JSON string describing
        the movies in the same format as the online movie requests. Allowing the
        the same adapter to be used for botj local and on;ine data
     */
    public String getMoviesAsJSON( Context context, SQLiteDatabase db ) {
        String  returnString = null ;

        Cursor  cursor = db.query(MOVIES_TABLE_NAME, null, null, null, null, null, null) ;
        if ( cursor != null ) {
            if (cursor.getCount() != 0) {
                int index_movie_ID, index_movie_title, index_movie_synopsis, index_movie_poster, index_movie_poster_height, index_movie_poster_width ;
                int index_release_date, index_rating ;
                index_movie_ID = cursor.getColumnIndex(MOVIE_ID) ;
                index_movie_title = cursor.getColumnIndex(MOVIE_TITLE) ;
                index_movie_synopsis = cursor.getColumnIndex(MOVIE_SYNOPSIS) ;
                index_release_date = cursor.getColumnIndex(MOVIE_RELEASE_DATE) ;
                index_rating = cursor.getColumnIndex(MOVIE_RATING) ;
                /*
                    The JSON root object
                 */
                JSONObject jsonRoot = new JSONObject() ;
                JSONArray   jsonResults = new JSONArray() ;

                /*
                    Parse the query results and add JSON obejcts to the JSONArray for
                    each movie
                 */
                try {
                    /*
                        Fake the root element key/value pairs
                     */
                    jsonRoot.put(context.getString(R.string.json_page), 1) ;
                    jsonRoot.put( context.getString(R.string.json_total_results), 1000) ;
                    while( cursor.moveToNext() ) {
                        JSONObject jsonMovie = new JSONObject();
                        jsonMovie.put(context.getString(R.string.json_id), cursor.getString(index_movie_ID)) ;
                        jsonMovie.put(context.getString(R.string.json_original_title), cursor.getString(index_movie_title));
                        jsonMovie.put(context.getString(R.string.json_poster_path), "") ;      // This value will indicate the image is local
                        jsonMovie.put( context.getString(R.string.json_overview), cursor.getString(index_movie_synopsis)) ;
                        jsonMovie.put( context.getString(R.string.json_vote_average), index_rating) ;
                        jsonMovie.put( context.getString(R.string.json_release_date), index_release_date ) ;
                        /*
                            Add movie object to results array
                         */
                        jsonResults.put(jsonMovie) ;
                    }
                    /*
                        Add the array to the root object
                     */
                    jsonRoot.put("results", jsonResults) ;
                    /*
                        Convert the JSON object tp string for return to caller
                     */
                    returnString = jsonRoot.toString() ;
                } catch ( JSONException ex) {
                    Log.d(TAG, "getMoviesAsJSON: exception -> " + ex.toString());
                }
            }
        }
        return returnString;
    }
}
