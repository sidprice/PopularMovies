package com.sidprice.android.popularmovies.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.sidprice.android.popularmovies.tasks.FetchMovieDataTask;
import com.sidprice.android.popularmovies.activities.FilterActivity;
import com.sidprice.android.popularmovies.model.Movies;
import com.sidprice.android.popularmovies.adapters.MoviesAdapter;
import com.sidprice.android.popularmovies.R;

public class MoviesFragment extends Fragment {
    private Movies theMovies ;
    private MenuItem filterMenuItem ;
    private GridView gridView ;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View    view = inflater.inflate(R.layout.fragment_main, container, false) ;
        theMovies = new Movies() ;
        gridView = view.findViewById(R.id.gv_movies) ;
        setHasOptionsMenu(true);
        return view ;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_filter_movies, menu);
        filterMenuItem = menu.getItem(0) ;
        /*
            Set the correct menu item icon
         */
        setFilterOptionIcon() ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        /*
            Check the item is what we expect
         */
        if (id == R.id.action_filter) {
            startActivity(new Intent(getContext(), FilterActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
            Return the current filter request string ID
         */
    private int getFilterValue(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE) ;
        return sharedPreferences.getInt(getString(R.string.saved_filter_key), R.string.request_popular);
    }

    /*
    Set the icon for the options menu according to the current filter selection
 */
    private void setFilterOptionIcon() {
        Drawable theIcon ;
        Context     context = getContext() ;
        if ( filterMenuItem != null ) {
         /*
            Get the filter preference
         */
            int filterRequestID = getFilterValue(context);

            switch(filterRequestID) {
                default:
                case R.string.request_popular: {
                    theIcon = ContextCompat.getDrawable(context, R.drawable.ic_popular) ;
                    break;
                }

                case R.string.request_top_rated: {
                    theIcon = ContextCompat.getDrawable(context, R.drawable.ic_top_rated) ;
                    break;
                }

                case R.string.request_user_favorites: {
                    theIcon = ContextCompat.getDrawable(context, android.R.drawable.btn_star_big_on) ;
                    break ;
                }
            }
            filterMenuItem.setIcon(theIcon) ;
        }
    }

    @Override
    public void onResume() {
        Context context = getContext() ;

        // gridView.setAdapter(null);
        theMovies = new Movies() ;
        /*
            Set the correct menu item icon
         */
        setFilterOptionIcon() ;
        /*
            Load the data using the selected filter
         */
          /*
            Get the filter preference
         */
        int filterValue = getFilterValue(context);
        String  fileString = context.getString(filterValue) ;
        int test = (int)R.string.request_popular ;
        /*
            Check if the filer option is for user favorites of requires
            online access
         */
        switch(filterValue) {
            default:
            case (int)R.string.request_user_favorites: {
                theMovies.loadMovies(context);
                SetupGridView() ;
                break ;
            }

            case (int)R.string.request_top_rated:
            case (int)R.string.request_popular: {
            /*
                start the movie data fetcher
            */
                FetchMovieDataTask myTask = new FetchMovieDataTask(new FetchMovieDataTask.AsyncResponse() {
                    @Override
                    public void processMovieData(String[] jsonMovieData) {
                        if (jsonMovieData != null) {
                            /*
                                Initialize the Movies object from the first received json string
                            */
                            theMovies.loadMovies(getActivity().getBaseContext(), jsonMovieData[0]);
                            /*
                             Configure the movies object using the second (Configuraton)
                                json string
                             */
                            theMovies.configureMovies(jsonMovieData[1]);
                            /*
                                Set up the GridView
                             */
                            SetupGridView() ;
                        } else {
                                Toast.makeText(getContext(), R.string.error_network_access_failed, Toast.LENGTH_LONG).show(); ;
                        }
                    }
                }) ;
                myTask.execute(getString(filterValue)) ;    // Runb the online access task
                break ;
            }
        }
        super.onResume();
    }
    //
    private void SetupGridView() {
        GridView theGridView = getActivity().findViewById(R.id.gv_movies);
        final MoviesAdapter moviesAdapter = new MoviesAdapter(getContext(), theMovies);
        theGridView.setAdapter(moviesAdapter);
    }
}
