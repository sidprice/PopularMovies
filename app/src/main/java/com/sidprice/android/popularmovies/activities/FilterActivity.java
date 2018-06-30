package com.sidprice.android.popularmovies.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sidprice.android.popularmovies.R;

import static com.sidprice.android.popularmovies.fragments.MoviesFragment.preference_file_key;
import static com.sidprice.android.popularmovies.fragments.MoviesFragment.request_popular;
import static com.sidprice.android.popularmovies.fragments.MoviesFragment.request_top_rated;
import static com.sidprice.android.popularmovies.fragments.MoviesFragment.request_user_favorites;
import static com.sidprice.android.popularmovies.fragments.MoviesFragment.saved_filter_default_key;
import static com.sidprice.android.popularmovies.fragments.MoviesFragment.saved_filter_key;

public class FilterActivity extends AppCompatActivity {
    /*
        UI components
     */
    private RadioGroup  radioGroupFilter ;
    private RadioButton radioButtonPopular ;
    private RadioButton radioButtonTopRated ;
    private RadioButton radioUserFavorites ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        radioButtonPopular = findViewById(R.id.radioFilterPopular) ;
        radioButtonTopRated = findViewById(R.id.radioFilterTopRated) ;
        radioUserFavorites = findViewById(R.id.radioFilterFavorites) ;
       /*
            Save the string resource ID of the request text to be sent to the server
            in the RadioButton Tag. It is used to format the request in the
            OnCheckedChanged method.
         */
        radioButtonPopular.setTag(request_popular);
        radioButtonTopRated.setTag(request_top_rated);
        radioUserFavorites.setTag(request_user_favorites);
        /*
            Ensure the correct radio button is checked
         */
        SharedPreferences   sharedPreferences = getApplicationContext().getSharedPreferences(preference_file_key, Context.MODE_PRIVATE) ;

        String filterValue = sharedPreferences.getString(saved_filter_key, saved_filter_default_key) ;

        switch(filterValue) {
            default:
            case request_popular: {
                radioButtonPopular.setChecked(true);
                break;
            }

            case request_top_rated: {
                radioButtonTopRated.setChecked(true );
                break ;
            }

            case request_user_favorites: {
                radioUserFavorites.setChecked(true);
                break;
            }
        }
        /*
            Add a listener for the filter radio group
         */
        radioGroupFilter = findViewById(R.id.radioFilterGroup) ;
        radioGroupFilter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Context context = getApplicationContext() ;
                RadioButton radioButtonChanged = findViewById(checkedId) ;
                /*
                    Save the new selection in preferences, the UI will update
                    when the main activity resumes.

                    The resource ID of the filter string is in the RadioButton tag
                 */
                String filterSelection = (String)radioButtonChanged.getTag() ;
                SharedPreferences   sharedPreferences = context.getSharedPreferences(preference_file_key, Context.MODE_PRIVATE) ;
                SharedPreferences.Editor editor = sharedPreferences.edit() ;
                editor.putString(saved_filter_key, filterSelection) ;

                editor.commit() ;
                finish();
            }
        });
    }
}
