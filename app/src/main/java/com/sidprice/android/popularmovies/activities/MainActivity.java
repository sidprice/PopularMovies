package com.sidprice.android.popularmovies.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sidprice.android.popularmovies.R;
import com.sidprice.android.popularmovies.fragments.MoviesFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager() ;
        Fragment        fragment = fragmentManager.findFragmentById(R.id.container) ;

        if ( fragment == null) {
            fragment = new MoviesFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.container, fragment)
                    .commit() ;
        }
    }
}
