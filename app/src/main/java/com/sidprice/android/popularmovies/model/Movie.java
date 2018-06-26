package com.sidprice.android.popularmovies.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;

import com.sidprice.android.popularmovies.database.MoviesDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/*
    Class to hold the data describing a movie

 */
@Entity(tableName = "movies")
public class Movie implements Parcelable {
    private static final String TAG = "Movie";
    @PrimaryKey
    @NonNull
    private final   String ID ;        // Movie ID from online Db
//    private Boolean favorite ;
    private final   String  originalTitle ;
    private final   String  posterUrl;
    private final   String  synopsis ;
    private final   String  userRating ;
    private final   String  releaseDate ;
    @ColumnInfo( typeAffinity = ColumnInfo.BLOB)
    private byte[]  moviePoster ;
    private int     posterHeight = 0 ;
    private int     posterWidth = 0 ;
    @Ignore
    private ArrayList<ReviewData>   reviews = new ArrayList<ReviewData>() ;
    @Ignore
    private ArrayList<String>       trailers = new ArrayList<String>() ;

    public Movie(String ID, String originalTitle, String posterUrl, String synopsis, String userRating, String releaseDate, byte[] moviePoster) {
        this.ID = ID ;
        this.originalTitle = originalTitle ;
        this.posterUrl = posterUrl ;
        this.synopsis = synopsis ;
        this.userRating = userRating ;
        this.releaseDate = releaseDate ;
        //this.favorite = favorite;
        this.moviePoster = moviePoster;
    }

    @Ignore
    protected Movie(Parcel in) {
        ID = in.readString() ;
        originalTitle = in.readString();
        posterUrl = in.readString();
        synopsis = in.readString();
        userRating = in.readString();
        releaseDate = in.readString();
        posterWidth = in.readInt() ;
        posterHeight = in.readInt() ;
        /*
            Get the movie poster bitmap
         */
        int arraySize = in.readInt() ;
        if ( arraySize > 0) {
            moviePoster = new byte[arraySize] ;
            in.readByteArray(moviePoster);
        }
    }
    /*
        This method recieves the json strings read from the online database
        for the reviews and the trailers and adds the data to this object
     */
    public void loadExtraData(String reviewsData, String trailersData) {
        if ( (reviewsData != null) && (trailersData != null)) {     // Only proceed with good data
            try {
                JSONObject rootObject = new JSONObject(reviewsData) ;
                /*
                    Query the root object for the "results" array
                 */
                JSONArray resultsArray = rootObject.getJSONArray("results") ;
                /*
                    Iterate over the array of results building the reviews and authors array lists
                 */
                for ( int i = 0 ; i < resultsArray.length() ; i++) {
                    JSONObject reviewRoot = resultsArray.getJSONObject(i) ;
                    ReviewData reviewData = new ReviewData() ;
                    reviewData.setReview(reviewRoot.getString("content"));
                    reviewData.setAuthor(reviewRoot.getString("author"));
                    reviews.add(reviewData);
                }
            }
            catch (JSONException jsonException) {
                //
                // Reviews array lists will be empty
                //
            }
            try {
                JSONObject rootObject = new JSONObject(trailersData) ;
                /*
                    Query the root object for the "results" array
                 */
                JSONArray resultsArray = rootObject.getJSONArray("results") ;
                /*
                    Iterate over the array of results building the reviews and authors array lists
                 */
                for ( int i = 0 ; i < resultsArray.length() ; i++) {
                    JSONObject trailerRoot = resultsArray.getJSONObject(i) ;
                    trailers.add(trailerRoot.getString("key"));
                }
            }
            catch (JSONException jsonException) {
                //
                // Trailers array lists will be empty
                //
            }
        }
    }
    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
//        if (ID == null) {
//            dest.writeByte((byte) 0);
//        } else {
//            dest.writeByte((byte) 1);
//            dest.writeString(ID);
//        }
        dest.writeString(ID);
        dest.writeString(originalTitle);
        dest.writeString(posterUrl);
        dest.writeString(synopsis);
        dest.writeString(userRating);
        dest.writeString(releaseDate);
        dest.writeInt(posterWidth);
        dest.writeInt(posterHeight);
        if ( moviePoster != null ) {
       /*
            Write the bitmap byte[] length
         */
            dest.writeInt(moviePoster.length);
            dest.writeByteArray(moviePoster);
        } else {
            dest.writeInt(0);
        }
    }

    //
    public String getPosterUrl() { return posterUrl ;} ;
    //
    public String getID() { return ID ;}
    //
    public String getSynopsis() { return synopsis ; }
    //
    public String getUserRating() { return  userRating ;}
    //
    public String getOriginalTitle() { return originalTitle;  }
    //
    public String getReleaseDate() { return releaseDate ;}
    //
    public ArrayList<String> getTrailers() {
        return trailers ;
    }
    //
    public ArrayList<ReviewData> getReviews() { return reviews ; }
    //
    public  ReviewData getReview(int position) {
        return reviews.get(position) ;
    }
    //
    public Boolean getFavorite(Context context) {
        MoviesDatabase  mDb = MoviesDatabase.getInstance(context) ;
        Movie   moveFromDb = mDb.moviesDao().getMovieById(ID) ;
        return (moveFromDb != null) ;
    }

    public Bitmap getMoviePosterBitmap() {
        Bitmap posterImage = null ;
        try {
            byte[]  data = moviePoster ;
            posterImage = Bitmap.createBitmap((DisplayMetrics)null, posterWidth, posterHeight, Bitmap.Config.ARGB_8888) ;
            ByteBuffer buffer = ByteBuffer.wrap(data) ;
            posterImage.copyPixelsFromBuffer(buffer);
        } catch (Exception e) {
            Log.d(TAG, "GetPosterBitmap: " + e.toString());
        }
        return posterImage ;
    }
    public byte[] getMoviePoster() { return moviePoster; }

    public void setMoviePoster( byte[] moviePoster ) {
        this.moviePoster = moviePoster ;
    }

    public int getPosterHeight() { return posterHeight; }
    public void setPosterHeight(int posterHeight) {
        this.posterHeight = posterHeight;
    }

    public int getPosterWidth() { return posterWidth; }
    public void setPosterWidth(int posterWidth) {
        this.posterWidth = posterWidth;
    }
}
