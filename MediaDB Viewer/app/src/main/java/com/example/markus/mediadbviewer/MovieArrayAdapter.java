package com.example.markus.mediadbviewer;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MovieArrayAdapter extends ArrayAdapter<MediaObject> {

    private String coverFolder;
    private MyApplication application;
    private boolean preview;
    private boolean previewInitialized = false;
    public String previewImdbID = null;
    public int previewDimension = 2;

    public MovieArrayAdapter(Context context, List mediaList, MyApplication application, boolean landscape) {
        super(context, -1, mediaList);
        this.application = application;
        this.coverFolder  = this.application.rootCoverFolder + "Filme/low/";
        this.preview = landscape;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        ((MainActivity) getContext()).setTitle("Filme (" + this.getCount() + ")");

        // initialisiere Preview nachdem Daten geladen wurden
        if ((this.getCount() > 0) && this.preview && !this.previewInitialized) {
            FragmentManager fragmentManager = ((MainActivity) getContext()).getSupportFragmentManager();
            MovieDetailedFragment movieDetailedFragment = new MovieDetailedFragment();
            movieDetailedFragment.setPreview();
            if (this.previewImdbID == null) {
                movieDetailedFragment.setImdbID(this.getItem(0).simpleValues.get("imdbID"));
                if (this.getItem(0).simpleValues.get("3d").length() == 0) {
                    movieDetailedFragment.setDimensions(0);
                } else {
                    movieDetailedFragment.setDimensions(1);
                }
            } else {
                movieDetailedFragment.setImdbID(this.previewImdbID);
                movieDetailedFragment.setDimensions(this.previewDimension);
            }
            try {
                fragmentManager.beginTransaction().replace(R.id.listDetailedPreview, movieDetailedFragment, "movieDetailedFragmentPreview").commit();
            } catch (Exception e) {
                // JSON Parser erst fertig, nachdem Bildschirm gedreht wurde
            }
            this.previewInitialized = true;
        }
    }

    /*
    public ArrayList<MediaObject> getMovieList() {

        return this.movieList;

    }
    */

    private void setValues(ViewHolder holder, int position) {

        // setzt imdbID für ViewHolder
        holder.imdbID = this.getItem(position).simpleValues.get("imdbID");
        holder.dimensions = this.getItem(position).simpleValues.get("3d");

        // Setze Texte des Views
        holder.firstLine.setText(this.getItem(position).simpleValues.get("name") + " (" + this.getItem(position).simpleValues.get("year") + ")");
        holder.secondLine.setText("Rating:   " + this.getItem(position).simpleValues.get("rating"));
        holder.thirdLine.setText("Laufzeit: " + Integer.valueOf(this.getItem(position).simpleValues.get("duration"))/60 + " Min");

        File cover = new File(this.coverFolder + this.getItem(position).simpleValues.get("imdbID") + ".jpg");

        //Bearbeite Bild des Layouts
        if (cover.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inSampleSize = 4;
            Bitmap coverBitmap = BitmapFactory.decodeFile(cover.getAbsolutePath(), options);
            holder.cover.setImageBitmap(coverBitmap);
        } else {
            holder.cover.setImageResource(R.drawable.default_cover);
        }

        // verstecke oder aktiviere 3D Logo
        if (this.getItem(position).simpleValues.get("3d").length() == 0) {
            holder.dimensionsLogo.setImageDrawable(null);
        } else {
            holder.dimensionsLogo.setImageResource(R.drawable.logo3d);
        }

        // setze checked Bild
        if (this.getItem(position).simpleValues.keySet().contains("checked")) {
            if (this.getItem(position).simpleValues.get("checked").equals("1")) {
                holder.checkedImage.setImageResource(R.drawable.correct);
            } else if (this.getItem(position).simpleValues.get("checked").equals("0")) {
                holder.checkedImage.setImageResource(R.drawable.wrong);
            } else {
                holder.checkedImage.setImageDrawable(null);
            }
        }

        // setzte view count
        if (this.getItem(position).simpleValues.keySet().contains("views")) {
            holder.viewCount.setText(String.valueOf(this.getItem(position).simpleValues.get("views")));
        } else {
            holder.viewCount.setText("");
        }

    }

    // iteriert über jedes Element des String-Arrays (position) ist Array-Index
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); //hole den LayoutInflater
            convertView = inflater.inflate(R.layout.list_element, null);//inflate Element

            // erstelle ViewHolder, setze Klassenvariablen und füge ihn dem View hinzu
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.firstLine = (TextView) convertView.findViewById(R.id.firstLine);
            holder.secondLine = (TextView) convertView.findViewById(R.id.secondLine);
            holder.thirdLine = (TextView) convertView.findViewById(R.id.thirdLine);
            holder.cover = (ImageView) convertView.findViewById(R.id.cover);
            holder.dimensionsLogo = (ImageView) convertView.findViewById(R.id.movie3dLogo);
            holder.checkedImage = (ImageView) convertView.findViewById(R.id.checkedImage);
            holder.viewCount = (TextView) convertView.findViewById(R.id.viewCount);

            // führe Funktion zum Anpassen des Views aus
            setValues(holder, position);

            // setze onClickListener ... ändert sich nie, deshalb nur beim Initialisieren
            if (this.preview) {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewHolder holder = (ViewHolder) v.getTag();
                        FragmentManager fragmentManager = ((MainActivity) getContext()).getSupportFragmentManager();
                        MovieDetailedFragment movieDetailedFragment = new MovieDetailedFragment();
                        if (preview) {
                            movieDetailedFragment.setPreview();
                        }
                        movieDetailedFragment.setImdbID(holder.imdbID);
                        if (holder.dimensions.length() == 0) {
                            movieDetailedFragment.setDimensions(0);
                            previewDimension = 0;
                        } else {
                            movieDetailedFragment.setDimensions(1);
                            previewDimension = 1;
                        }
                        // zerstöre Preview Fagment, falls es existiert
                        Fragment movieDetailedFragmentPreview = fragmentManager.findFragmentByTag("movieDetailedFragmentPreview");
                        if (movieDetailedFragmentPreview != null) {
                            fragmentManager.beginTransaction().remove(movieDetailedFragmentPreview).commit();
                        }
                        fragmentManager.beginTransaction().replace(R.id.listDetailedPreview, movieDetailedFragment, "movieDetailedFragmentPreview").commit();
                        previewImdbID = holder.imdbID;
                    }
                });
            } else {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ViewHolder holder = (ViewHolder) v.getTag();
                        FragmentManager fragmentManager = ((MainActivity) getContext()).getSupportFragmentManager();
                        MovieDetailedFragment movieDetailedFragment = new MovieDetailedFragment();
                        movieDetailedFragment.setImdbID(holder.imdbID);
                        if (holder.dimensions.length() == 0) {
                            movieDetailedFragment.setDimensions(0);
                        } else {
                            movieDetailedFragment.setDimensions(1);
                        }
                        fragmentManager.beginTransaction().replace(R.id.mainContent, movieDetailedFragment, "movieDetailedFragment").addToBackStack("movieDetailedFragment").commit();

                    }
                });
            }


        } else {

            holder = (ViewHolder) convertView.getTag();
            this.setValues(holder, position);

        }

        return convertView;
    }

    private class ViewHolder {

        public TextView firstLine;
        public TextView secondLine;
        public TextView thirdLine;
        public ImageView cover;
        public ImageView dimensionsLogo;
        public ImageView checkedImage;
        public TextView viewCount;
        public String imdbID; // wird gebraucht, damit bei onClick event die imdbID ausgelesen werden kann
        public String dimensions; // wird gebraucht, damit bei onClick event die imdbID ausgelesen werden kann

    }

}
