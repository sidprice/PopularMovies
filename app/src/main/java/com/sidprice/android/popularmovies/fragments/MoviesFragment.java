package com.sidprice.android.popularmovies.fragments;

import android.app.Application;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.sidprice.android.popularmovies.model.MainViewModel;
import com.sidprice.android.popularmovies.model.Movie;
import com.sidprice.android.popularmovies.tasks.FetchMovieDataTask;
import com.sidprice.android.popularmovies.activities.FilterActivity;
import com.sidprice.android.popularmovies.model.Movies;
import com.sidprice.android.popularmovies.adapters.MoviesAdapter;
import com.sidprice.android.popularmovies.R;

import java.security.PublicKey;
import java.util.List;

public class MoviesFragment extends Fragment {
    private static final String TAG = "MoviesFragment";
    /*
        Preference file access keys
     */
    public  static  final   String  preference_file_key = "com.sidprice.android.popularmovies.PREF_FILE_KEY" ;
    public  static  final   String  saved_filter_key = "savedFilterKey" ;
    public  static  final   String  saved_filter_default_key = "popular" ;
    public  static  final   String  saved_intial_startup = "initial_startup" ;
    public  static  final   String  state_main_scroll_position = "state_main_scroll_position" ;
    public  static  final   String  state_details_scroll_position = "state_details_scroll_position" ;
    /*
        TMDB access keys
     */
    public  static  final   String  request_popular = "popular" ;
    public  static  final   String  request_top_rated = "top_rated" ;
    public  static  final   String  request_user_favorites = "user_favorites" ;

    private Movies theMovies ;
    private MainViewModel   favoritesViewModel ;
    private MenuItem filterMenuItem ;
    private GridView gridView ;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
            Assert that shared preference flag that indicates initial startup
         */
        saveInitialStartupFlag(true) ;
        favoritesViewModel = ViewModelProviders.of(this).get(MainViewModel.class) ;
        favoritesViewModel.getMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                Log.d(TAG, "onChanged: called");
                /*
                    If this is the first change after startup AND
                    the current filter selcetion is NOT favorites ... do nothing
                 */
                String currentFilterValue = getFilterValue(getContext()) ;
                Boolean initialSartupFlag = getIsInitialStartup(getContext()) ;
                if ( (currentFilterValue.equals(request_user_favorites))) {
                    theMovies.updateFromDatabase(getContext(), movies);
                    SetupGridView();
                }
            }
        });
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

    private void saveInitialStartupFlag(Boolean state) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(preference_file_key, Context.MODE_PRIVATE) ;
        SharedPreferences.Editor editor = sharedPreferences.edit() ;
        editor.putBoolean(saved_intial_startup, state) ;
        editor.apply();
    }


    /*
            Return the current filter request string
     */
    private String getFilterValue(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preference_file_key, Context.MODE_PRIVATE) ;
        return sharedPreferences.getString(saved_filter_key, request_popular);
    }

    private boolean getIsInitialStartup(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preference_file_key, Context.MODE_PRIVATE) ;
        return sharedPreferences.getBoolean(saved_intial_startup, true);
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
            String filterRequestID = getFilterValue(context);

            switch(filterRequestID) {
                default:
                case request_popular: {
                    theIcon = ContextCompat.getDrawable(context, R.drawable.ic_popular) ;
                    break;
                }

                case request_top_rated: {
                    theIcon = ContextCompat.getDrawable(context, R.drawable.ic_top_rated) ;
                    break;
                }

                case request_user_favorites: {
                    theIcon = ContextCompat.getDrawable(context, android.R.drawable.btn_star_big_on) ;
                    break ;
                }
            }
            filterMenuItem.setIcon(theIcon) ;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        int gridScrollPosition = gridView.getFirstVisiblePosition() ;
        outState.putInt(state_main_scroll_position, gridScrollPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if ( savedInstanceState != null ) {
            int gridScrollPosition = savedInstanceState.getInt(state_main_scroll_position) ;
            gridView.setSelection(gridScrollPosition);
        }
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: called");
        Context context = getContext() ;
        Application application = getActivity().getApplication() ;
        Boolean     initialStartupFlag = getIsInitialStartup(context) ;
        saveInitialStartupFlag(false); ;
        GridView theGridView = getActivity().findViewById(R.id.gv_movies);
        theGridView.setAdapter(null);

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
        String filterValue = getFilterValue(context);
        /*
            Check if the filter option is for user favorites of requires
            online access
         */
        switch(filterValue) {
            default:
            case request_user_favorites: {
                //theMovies.loadMovies(application);
                if (!initialStartupFlag) {
                    List<Movie> myMovies = favoritesViewModel.getMovies().getValue() ;
                    if (myMovies != null ) {
                        theMovies.updateFromDatabase(context, favoritesViewModel.getMovies().getValue());
                    }
                }
                SetupGridView() ;
                break ;
            }

            case request_top_rated:
            case request_popular: {
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
                                Toast.makeText(getContext(), R.string.error_network_access_failed, Toast.LENGTH_LONG).show();
                        }
                    }
                }) ;
                myTask.execute(filterValue) ;    // Run the online access task
                break ;
            }
        }
        super.onResume();
    }
    //
    private void SetupGridView() {
        GridView theGridView = getActivity().findViewById(R.id.gv_movies);
        final MoviesAdapter moviesAdapter = new MoviesAdapter(getContext(), theMovies);
        theGridView.setAdapter(null);
        theGridView.setAdapter(moviesAdapter);
        if ( getFilterValue(getContext()).equals(R.string.radio_filter_favorite)) {
            if ( theMovies.getCount() == 0 ) {
                Toast.makeText(getContext(), R.string.error_no_favorites, Toast.LENGTH_LONG).show();
            }
        }
    }
}
