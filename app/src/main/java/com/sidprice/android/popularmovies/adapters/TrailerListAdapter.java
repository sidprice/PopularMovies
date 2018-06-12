package com.sidprice.android.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sidprice.android.popularmovies.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TrailerListAdapter extends RecyclerView.Adapter<TrailerListAdapter.ViewHolder> {
    private static final String TAG = "TrailerListAdapter";
    private ArrayList<String>   mImageUrls = new ArrayList<>() ;
    private Context             mContext ;

    public TrailerListAdapter( Context Context, ArrayList<String> ImageUrls) {
        this.mImageUrls =ImageUrls;
        this.mContext = Context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trailer_list_item, parent, false) ;
        ViewHolder  viewHolder = new ViewHolder(view) ;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called");
        final int videoPosition = position ;
        /*
            Load the image
         */
        String  moveImageUrl = "http://img.youtube.com/vi/" + mImageUrls.get(position) + "/0.jpg";
        Picasso.get().load(moveImageUrl).into(holder.image) ;

        holder.parent_view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /*
                    Create Intent to play the selected trailer
                 */
                Intent  intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + mImageUrls.get(videoPosition)) ) ;
                mContext.startActivity(intent);
            }
        }) ;
    }

    @Override
    public int getItemCount() {
        return mImageUrls.size() ;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView   image ;
        android.support.constraint.ConstraintLayout parent_view ;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_trailer) ;
            parent_view = itemView.findViewById(R.id.trailer_image_parent );
        }
    }

}
