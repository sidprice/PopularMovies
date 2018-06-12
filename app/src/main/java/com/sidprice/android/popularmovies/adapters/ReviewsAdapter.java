package com.sidprice.android.popularmovies.adapters;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.TagTechnology;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.TintTypedArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sidprice.android.popularmovies.R;
import com.sidprice.android.popularmovies.model.Movie;
import com.sidprice.android.popularmovies.model.ReviewData;

import java.util.ArrayList;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
    private static final String TAG = "ReviewsAdapter";
    private Movie   mMovie ;

    public ReviewsAdapter(Context context, Movie movie) {
        this.mMovie = movie;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called");
        View    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_reviews_item, parent, false) ;
        ReviewsAdapter.ViewHolder viewHolder = new ReviewsAdapter.ViewHolder(view) ;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called");
        holder.tv_name.setText(mMovie.getReview(position).getAuthor()) ;
        holder.tv_content.setText(mMovie.getReview(position).getReview());
    }

    @Override
    public int getItemCount() {
        return mMovie.getReviews().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        android.support.constraint.ConstraintLayout parent_view ;
        TextView    tv_name ;
        TextView    tv_content ;
        public ViewHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_review_name) ;
            tv_content = itemView.findViewById(R.id.tv_review_content) ;
            parent_view = itemView.findViewById(R.id.rv_reviews_parent );
        }
    }
}
