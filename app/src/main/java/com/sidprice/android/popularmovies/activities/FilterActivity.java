package com.sidprice.android.popularmovies.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sidprice.android.popularmovies.R;

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
        radioButtonPopular.setTag(R.string.request_popular);
        radioButtonTopRated.setTag(R.string.request_top_rated);
        radioUserFavorites.setTag(R.string.request_user_favorites);
        /*
            Ensure the correct radio button is checked
         */
        SharedPreferences   sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE) ;
        int defaultFilterValue = R.string.saved_filter_default_key ;
        int filterValue = sharedPreferences.getInt(getString(R.string.saved_filter_key), R.string.saved_filter_default_key) ;

        switch(filterValue) {
            default:
            case (int)R.string.request_popular: {
                radioButtonPopular.setChecked(true);
                break;
            }

            case (int)R.string.request_top_rated: {
                radioButtonTopRated.setChecked(true );
                break ;
            }

            case (int)R.string.request_user_favorites: {
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
                int filterSelection = (int)radioButtonChanged.getTag() ;
                SharedPreferences   sharedPreferences = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE) ;
                SharedPreferences.Editor editor = sharedPreferences.edit() ;
                editor.putInt(getString(R.string.saved_filter_key), filterSelection) ;

                editor.commit() ;
                finish();
            }
        });
    }
}
