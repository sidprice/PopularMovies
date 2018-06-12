package com.sidprice.android.popularmovies.activities;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
/*
    This class is used to disable the inner ConstraintLayout of the Details
    view from scrolling and allowing the out ConsytraintLayout
    to scroll normally.

    Ref: StackOverflow:
        https://stackoverflow.com/questions/30222310/disable-scrolling-in-child-recyclerview-android/30222721
 */
public class CustomLinearLayout extends LinearLayoutManager {
    public CustomLinearLayout(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public boolean canScrollVertically() {
        return false ;      // Disable vertical scrolling
    }
}
