package com.sidprice.android.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.sidprice.android.popularmovies.activities.DetailActivity;
import com.sidprice.android.popularmovies.R;
import com.sidprice.android.popularmovies.database.MoviesDatabaseHelper;
import com.sidprice.android.popularmovies.model.Movie;
import com.sidprice.android.popularmovies.model.Movies;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MoviesAdapter extends BaseAdapter {
    private final Context context;
    private final Movies movies ;
    private final String baseURL = "http://api.themoviedb.org/3/movie/" ;
    private final String imageSize = "w185/" ;
    private final Integer MAX_BUFFER_SIZE = 4096 ;

    public MoviesAdapter (Context context, Movies movies) {
        this.context = context ;
        this.movies = movies ;
    }
    @Override
    public int getCount() {

        return movies.getCount();
    }

    @Override
    public Object getItem(int position) {
        if ( position < movies.getCount()) {
            return movies.getMovie(position) ;
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ArrayList<Movie> theMovies = movies.getMovies();
        final Movie   theMovie = theMovies.get(position) ;
        /*
            If we are passed a View use it, otherwise create a new one
         */
        if ( convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.rv_movies_item, null);
            /*
                Get the poster imageview for the item view
             */
            ImageView imageView = convertView.findViewById(R.id.iv_Poster) ;
            ImageView imageFavorite = convertView.findViewById(R.id.iv_Favorite) ;
            final ViewHolder viewHolder = new ViewHolder( imageView, imageFavorite, theMovie.getID()) ;
            convertView.setTag(viewHolder);
        }
        /*
            If the movie has a poster URL then it is online, use Picasso to load it
         */
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        if ( theMovie.getPosterUrl().equals("")) {
            MoviesDatabaseHelper    dbHelper = new MoviesDatabaseHelper(context) ;
            SQLiteDatabase db =  dbHelper.getReadableDatabase() ;
            viewHolder.imageView.setImageBitmap(dbHelper.GetPosterBitmap(db, theMovie));
            db.close();
        } else {
            String moviePosterPath = Movies.tmdPosterBaseUrl() + imageSize + theMovie.getPosterUrl() + "?api_key=" + Movies.tmdApiKey();
            Picasso.get().load(moviePosterPath).into(viewHolder.imageView) ;
        }
        /*
            Set the favorite star image according to move favorite state
         */
        if (theMovie.getFavorite()) {
            viewHolder.imageFavorite.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            viewHolder.imageFavorite.setImageResource(android.R.drawable.btn_star_big_off);
        }
        /*
            set an onClick handler for the image
         */
        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /*
                    Show the clicked movie details
                 */
                final Intent  intent = new Intent(context, DetailActivity.class) ;
                /*
                    Assemble the extra data to send to the activity
                 */
                intent.putExtra("movie", theMovie) ;
                String moviePosterPath = Movies.tmdPosterBaseUrl() + imageSize + theMovie.getPosterUrl() + "?api_key=" + Movies.tmdApiKey();
                intent.putExtra(context.getString(R.string.intent_poster_url), moviePosterPath) ;
                v.getContext().startActivity(intent) ;
            }
        });
        return convertView;
    }


    private class ViewHolder {
        private final ImageView imageView, imageFavorite ;
        private int movieID ;

        public ViewHolder( ImageView imageView, ImageView imageFavorite, int movieID) {
            this.movieID = movieID ;
            this.imageView = imageView ;
            this.imageFavorite = imageFavorite ;
        }
    }
}
