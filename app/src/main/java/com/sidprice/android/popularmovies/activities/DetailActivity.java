package com.sidprice.android.popularmovies.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.sidprice.android.popularmovies.R;
import com.sidprice.android.popularmovies.fragments.DetailsFragment;

public class DetailActivity extends AppCompatActivity {
    private ImageView   mImageView ;
    private TextView    mtv_title, mtv_release_date, mtv_user_rating, mtv_synopsis ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        FragmentManager fragmentManager = getSupportFragmentManager() ;
        Fragment fragment = fragmentManager.findFragmentById(R.id.details_container) ;

        if ( fragment == null) {
            fragment = new DetailsFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.details_container, fragment)
                    .commit() ;
        }
    }
}
