package com.sidprice.android.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
    Class to hold the data describing a movie
 */
public class Movie implements Parcelable {
    private final   Integer ID ;
    private Boolean favorite ;
    private final   String  originalTitle ;
    private final   String  posterUrl;
    private final   String  synopsis ;
    private final   String  userRating ;
    private final   String  releaseDate ;
    private ArrayList<ReviewData>   reviews = new ArrayList<ReviewData>() ;
    private ArrayList<String>       trailers = new ArrayList<String>() ;

    public Movie(Integer ID, String originalTitle, String posterUrl, String synopsis, String userRating, String releaseDate, Boolean favorite) {
        this.ID = ID ;
        this.originalTitle = originalTitle ;
        this.posterUrl = posterUrl ;
        this.synopsis = synopsis ;
        this.userRating = userRating ;
        this.releaseDate = releaseDate ;
        this.favorite = favorite;
    }

    protected Movie(Parcel in) {
        if (in.readByte() == 0) {
            ID = null;
        } else {
            ID = in.readInt();
        }
        originalTitle = in.readString();
        posterUrl = in.readString();
        synopsis = in.readString();
        userRating = in.readString();
        releaseDate = in.readString();
        favorite = false;
        if ( in.readInt() != 0 ) {
            favorite = true  ;
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
        if (ID == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(ID);
        }
        dest.writeString(originalTitle);
        dest.writeString(posterUrl);
        dest.writeString(synopsis);
        dest.writeString(userRating);
        dest.writeString(releaseDate);
        int favoriteAsInt = 0 ;
        if (favorite) {
            favoriteAsInt = 1 ;
        }
        dest.writeInt(favoriteAsInt);
    }

    //
    public String getPosterUrl() { return posterUrl ;} ;
    //
    public int getID() { return ID ;}
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
    public void setFavorite(Boolean favorite) { this.favorite = favorite; }
    //
    public Boolean getFavorite() { return favorite ; }

}
