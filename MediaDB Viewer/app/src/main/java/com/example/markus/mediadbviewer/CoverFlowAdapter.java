package com.example.markus.mediadbviewer;


import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class CoverFlowAdapter extends RecyclerView.Adapter<CoverFlowAdapter.ViewHolder> {

    private ArrayList<MediaObject> mDataset;
    private MainActivity activity;
    private MyApplication application;
    public HashMap<String, Bitmap> bitmaps = new HashMap<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView mImageView;
        public String imdbID;

        public ViewHolder(ImageView v) {
            super(v);
            this.mImageView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CoverFlowAdapter(ArrayList<MediaObject> myDataset, MainActivity activity, MyApplication application) {
        mDataset = myDataset;
        this.activity = activity;
        this.application = application;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CoverFlowAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        // create a new view
        ImageView imageView = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.cover_flow_image, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(imageView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {


        if (this.mDataset.get(position).simpleValues.get("imdbID") != holder.imdbID) {

            /*
            if (holder.imdbID != null) {
                this.bitmaps.get(holder.imdbID).recycle();
                this.bitmaps.remove(holder.imdbID);
            }*/

            File cover = new File(this.application.coverFolderMoviesHigh + mDataset.get(position).simpleValues.get("imdbID") + ".jpg");

            //Bearbeite Bild des Layouts
            if (cover.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                /*
                if (this.activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    options.inSampleSize = 2;
                } else {
                    options.inSampleSize = 3;
                }*/
                Bitmap coverBitmap = BitmapFactory.decodeFile(cover.getAbsolutePath(), options);
                //this.bitmaps.put(this.mDataset.get(position).simpleValues.get("imdbID"), coverBitmap);
                holder.mImageView.setImageBitmap(coverBitmap);
            }

            // setze onClickListener
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    // schließe alle offenen Fragmente
                    for (int i=fragmentManager.getBackStackEntryCount(); i>0; i--) {
                        fragmentManager.popBackStack();
                    }
                    // erstelle detailedMovieFragment
                    MovieDetailedFragment movieDetailedFragment = new MovieDetailedFragment();
                    movieDetailedFragment.setImdbID(mDataset.get(position).simpleValues.get("imdbID"));
                    if (mDataset.get(position).simpleValues.get("3d").length() == 0) {
                        movieDetailedFragment.setDimensions(0);
                    } else {
                        movieDetailedFragment.setDimensions(1);
                    }
                    fragmentManager.beginTransaction().replace(R.id.mainContent, movieDetailedFragment, "movieDetailedFragment").addToBackStack("movieDetailedFragment").commit();
                }
            });
            holder.imdbID = this.mDataset.get(position).simpleValues.get("imdbID");

        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }




}