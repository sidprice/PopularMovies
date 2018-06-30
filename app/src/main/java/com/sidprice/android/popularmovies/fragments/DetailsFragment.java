package com.sidprice.android.popularmovies.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.sidprice.android.popularmovies.activities.CustomLinearLayout;
import com.sidprice.android.popularmovies.activities.MainActivity;
import com.sidprice.android.popularmovies.adapters.ReviewsAdapter;
import com.sidprice.android.popularmovies.database.MoviesDatabase;
import com.sidprice.android.popularmovies.tasks.FetchMovieExtraDataTask;
import com.sidprice.android.popularmovies.model.Movie;
import com.sidprice.android.popularmovies.R;
import com.sidprice.android.popularmovies.adapters.TrailerListAdapter;
import com.squareup.picasso.Picasso;

import java.nio.ByteBuffer;

import static com.sidprice.android.popularmovies.fragments.MoviesFragment.state_details_scroll_position;

public class DetailsFragment extends Fragment {
    private static final String TAG = "DetailsFragment";
    private ImageView mImageView ;
    private TextView mtv_title, mtv_release_date, mtv_user_rating, mtv_synopsis ;
    private ScrollView  mscroll_view ;
    private int[] mscrollview_position = new int[2];
    private Button  mbtn_favorite ;
    private ImageView   mImageStar ;
    private String  mtitle, mrelease_date, muser_rating, msynopsis, mposter_url ;
    private Movie mmovie ;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View    view = inflater.inflate(R.layout.fragment_details, container, false) ;

        mscroll_view = view.findViewById(R.id.details_scroll_view) ;
        /*
            Get the UI elements
         */
        mtv_title = view.findViewById(R.id.tv_title) ;
        mImageView = view.findViewById(R.id.iv_details_image) ;
        mImageStar = view.findViewById(R.id.image_favorite) ;
        mbtn_favorite = view.findViewById(R.id.btn_favorite) ;
        mtv_release_date = view.findViewById(R.id.tv__details_release_date) ;
        mtv_user_rating = view.findViewById(R.id.tv_details_user_rating) ;
        mtv_synopsis = view.findViewById(R.id.tv_details_synopsis) ;
        /*
            Get the intent so we can recover the details to display
         */
        Intent intent = getActivity().getIntent() ;
        /*
            Recover details
         */
        mmovie = intent.getParcelableExtra("movie") ;
        mtitle = mmovie.getOriginalTitle() ;
        mrelease_date = mmovie.getReleaseDate() ;
        muser_rating = mmovie.getUserRating() ;
        msynopsis = mmovie.getSynopsis() ;
        mposter_url = intent.getStringExtra( getString(R.string.intent_poster_url)) ;
       /*
            Create the task to get reviews and trailers for the movie
         */
        FetchMovieExtraDataTask fetchMovieExtraDataTask = new FetchMovieExtraDataTask(new FetchMovieExtraDataTask.AsyncResponse() {

            @Override
            public void processMovieData(String[] jsonMoveData) {
                Boolean noExtraData = true ;
                /*
                    If this movie is being displayed from the local database, don't
                    attempt to get the extra data as we may be offline
                 */
                if ( !mmovie.getPosterUrl().equals("")) {
                    noExtraData = false ;
                /*
                    Pass the loaded data to the movie object to parse it
                 */
                    mmovie.loadExtraData(jsonMoveData[0], jsonMoveData[1]);
                /*
                    Output to UI
                 */
                /*
                    Setup the Recycler View of trailers
                 */
                    RecyclerView rv_trailers = view.findViewById(R.id.rv_trailer_images) ;
                    TrailerListAdapter adapter = new TrailerListAdapter(getContext(), mmovie.getTrailers()) ;
                    rv_trailers.setAdapter(adapter);
                    rv_trailers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false ));
                /*
                    Setup recycler view of reviews
                 */
                    RecyclerView rv_reviews = view.findViewById(R.id.rv_reviews) ;
                    ReviewsAdapter reviewsAdapter = new ReviewsAdapter(getContext(), mmovie) ;
                    rv_reviews.setAdapter(reviewsAdapter);
                    rv_reviews.setLayoutManager(new CustomLinearLayout(getContext(), LinearLayoutManager.VERTICAL, false));
                }
                mtv_title.setText(mtitle);
                //
                if ( !mmovie.getPosterUrl().equals("")) {
                    Picasso.get().load(mposter_url).into(mImageView) ;
                } else {
                    mImageView.setImageBitmap(mmovie.getMoviePosterBitmap());
                }
                /*
                    Set favorite image and button text according to movie favorite state
                 */
                processMovieFavorite( mmovie.getFavorite());
                mtv_release_date.setText(mrelease_date);
                mtv_user_rating.setText(muser_rating);
                mtv_synopsis.setText(msynopsis);
                mscroll_view.post(new Runnable() {
                    @Override
                    public void run() {
                        mscroll_view.scrollTo(mscrollview_position[0], mscrollview_position[1]);
                    }
                });

                /*
                    Set up a click handler for the favorite button
                 */
                mbtn_favorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bitmap  posterBitmap ;
                        boolean isFavorite = !(mmovie.getFavorite()) ;
                        processMovieFavorite(isFavorite);
                        /*
                            Update the local database of favorite movies
                         */
                        final MoviesDatabase moviesDb = MoviesDatabase.getInstance(getContext()) ;
                        /*
                            If movie is favorite, add to database
                         */
                        if ( isFavorite ) {
                            /*
                                Get the poster bitmap to be added to the database
                             */
                            posterBitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap() ;
                            /*
                                Convert the input Bitmap to a byte array for addition to Database
                            */
                            ByteBuffer byteBuffer = ByteBuffer.allocate(posterBitmap.getByteCount()) ;
                            posterBitmap.copyPixelsToBuffer(byteBuffer);
                            byteBuffer.rewind() ;
                            byte[]  data = new byte[byteBuffer.remaining()] ;
                            byteBuffer.get(data) ;

                            mmovie.setMoviePoster(data);
                            mmovie.setPosterHeight(posterBitmap.getHeight() ) ;
                            mmovie.setPosterWidth(posterBitmap.getWidth()) ;
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    moviesDb.moviesDao().insertMovie(mmovie);
                                }
                            }) ;
                            thread.start();
                            Toast.makeText(getContext(), "Added favorite movie", Toast.LENGTH_SHORT).show() ;
                        } else {
                            /*
                                Remove from local database
                             */
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    moviesDb.moviesDao().deleteMovie(mmovie);
                                }
                            }) ;
                            thread.start();
                            Toast.makeText(getContext(), "Deleted favorite movie", Toast.LENGTH_SHORT).show() ;
                        }

                    }
                });
            }
        }) ;
        /*
            Execute the task passing the movie ID
         */
        fetchMovieExtraDataTask.execute(mmovie.getID()) ;
        return view ;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        mscrollview_position[0] = mscroll_view.getScrollX() ;
        mscrollview_position[1] = mscroll_view.getScrollY() ;
        outState.putIntArray( state_details_scroll_position, mscrollview_position);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mscrollview_position = savedInstanceState.getIntArray( state_details_scroll_position) ;
        }
        super.onViewStateRestored(savedInstanceState);
    }

    private void processMovieFavorite(Boolean isFavorite) {
        if (isFavorite) {
            mImageStar.setImageResource(android.R.drawable.btn_star_big_on);
            mbtn_favorite.setText(R.string.btn_remove_favorite);
        } else {
            mImageStar.setImageResource(android.R.drawable.btn_star_big_off);
            mbtn_favorite.setText(R.string.btn_add_favorite) ;
        }
    }
}
