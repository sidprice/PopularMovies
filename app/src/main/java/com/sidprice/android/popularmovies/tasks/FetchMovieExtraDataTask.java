package com.sidprice.android.popularmovies.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.sidprice.android.popularmovies.model.Movies;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchMovieExtraDataTask extends AsyncTask<Integer, Void, String[]> {
    /*
        Create the interface that allows the background thread to
        call the MainActivity thread with the result of the
        movie data reading process
     */
    public interface AsyncResponse {
        void processMovieData(String [] jsonMoveData) ;
    }

    public FetchMovieExtraDataTask.AsyncResponse callback = null ;
    /*
        Constructor to be used by the host activity to
        set up the callback method
     */
    public FetchMovieExtraDataTask( FetchMovieExtraDataTask.AsyncResponse callback) {
        this.callback = callback ;
    }
    private final Integer MAX_BUFFER_SIZE = 4096 ;
    /*
        There is no pre-run code that needs to be executed
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    /*
        This AsyncTask fetches the reviews and trailers of movies from The Movie Database.

        The only parameter is the ID of the movie data to be loaded.

        The string array returned by this method has two JSON
        strings; the first is the reviews and the second is
        the trailer data.
     */
    @Override
    protected String[] doInBackground(Integer... integers) {
        try {
            String[] jsonStringsReturned = new String[2];
            byte[] resultBytes ;
                /*
                    Build the URI for the reviews
                 */
            String url = Uri.parse(Movies.tmdBaseUrl() + "movie/" + integers[0] + "/reviews")
                    .buildUpon()
                    .appendQueryParameter("api_key", Movies.tmdApiKey())
                    .build().toString() ;
            resultBytes = getHTTPBytesFromURL(url) ;
            if ( resultBytes != null )
            {
                jsonStringsReturned[0] = new String(resultBytes) ;
                  /*
                    Build the URI for the trailers.
                 */
                url = Uri.parse(Movies.tmdBaseUrl() + "movie/" + integers[0] + "/videos")
                        .buildUpon()
                        .appendQueryParameter("api_key", Movies.tmdApiKey())
                        .build().toString() ;
                resultBytes = getHTTPBytesFromURL(url) ;
                if ( resultBytes != null ) {
                    jsonStringsReturned[1] = new String(resultBytes) ;
                    return  jsonStringsReturned ;
                } else {
                    return null ;
                }
            } else {
                return null ;
            }
        }
        catch (IOException ex) {
            Log.d("SJP002: ",ex.toString() + "\n") ;
        }
        return null;
    }
    /*
        If the string received is not null then attempt to
        process it as a JSON string. It should be converted to
        a JSON object and the object used to initialize the
        Movies object.
     */
    @Override
    protected void onPostExecute(String[] s) {
        /*
            make sure the callback is valie
         */
        if ( callback != null ) {
            callback.processMovieData(s);
        }
    }

    private byte[] getHTTPBytesFromURL (String urlInput) throws IOException {
        URL url = new URL(urlInput) ;
        HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream bytesFromServer = new ByteArrayOutputStream() ;
            InputStream input = myConnection.getInputStream() ;

            if ( myConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(myConnection.getResponseMessage()) ;
            }

            Integer numberOfBytes = 0 ;
            byte[] inputBuffer = new byte[MAX_BUFFER_SIZE] ;
            while ( (numberOfBytes = input.read(inputBuffer)) > 0) {
                bytesFromServer.write(inputBuffer, 0 , numberOfBytes) ;
            }
            return bytesFromServer.toByteArray() ;
        } catch (IOException ex) {
            Log.e("something", ex.toString()) ;
            myConnection.disconnect();
            return null ;
        }
        finally {
            myConnection.disconnect(); ;
        }
    }
}
