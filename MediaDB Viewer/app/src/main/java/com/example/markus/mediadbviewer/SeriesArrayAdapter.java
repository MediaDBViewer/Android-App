package com.example.markus.mediadbviewer;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SeriesArrayAdapter extends ArrayAdapter<MediaObject> {

    private MyApplication application;

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        // Titel wird nur gesetzt, wenn das entsprechende Fragment im Vordergrund ist, sonst wird er sowie bei onResume des Fragments gesetzt
        if (((MainActivity) getContext()).getSupportFragmentManager().getBackStackEntryAt(((MainActivity) getContext()).getSupportFragmentManager().getBackStackEntryCount() - 1).getName().equals("seriesListFragment")) {
            ((MainActivity) getContext()).setTitle("Serien (" + this.getCount() + ")");
        }
    }

    public SeriesArrayAdapter (Context context, List mediaList, MyApplication application) {
        super(context, -1, mediaList);
        this.application = application;
    }

    private void setValues(ViewHolder holder, int position) {

        // setzt imdbID für ViewHolder
        holder.series_nr = this.getItem(position).simpleValues.get("series_nr");

        // Setze Texte des Views
        holder.firstLine.setText(this.getItem(position).simpleValues.get("name"));
        if (this.getItem(position).simpleValues.keySet().contains("Size")) {
            holder.secondLine.setText("Größe:    " + String.valueOf(Math.round(Long.valueOf(this.getItem(position).simpleValues.get("Size")) / Math.pow(1024, 3) * 100) / 100.) + " GB");
        } else {
            holder.secondLine.setVisibility(TextView.GONE);
        }
        holder.thirdLine.setText("Laufzeit: " + String.valueOf(Integer.valueOf(this.getItem(position).simpleValues.get("Duration"))/60) + " Min");

        if (new File(this.application.coverFolderSeriesLow + this.getItem(position).simpleValues.get("series_nr") + ".jpg").exists()) {
            holder.cover.setImageDrawable(Drawable.createFromPath(this.application.coverFolderSeriesLow + this.getItem(position).simpleValues.get("series_nr") + ".jpg"));
        } else {
            holder.cover.setImageResource(R.drawable.default_cover);
        }

        // setze checked Bild
        if (this.getItem(position).simpleValues.keySet().contains("Checked")) {
            if (this.getItem(position).simpleValues.get("Checked").equals("1.0000")) {
                holder.checkedImage.setImageResource(R.drawable.correct); // alles ganz
            } else if (this.getItem(position).simpleValues.get("Checked").equals("0.0000")) {
                holder.checkedImage.setImageResource(R.drawable.wrong); // alles defekt
            } else if (this.getItem(position).simpleValues.get("Checked").length() == 0) {
                holder.checkedImage.setImageDrawable(null); // noch nicht geprüft
            } else {
                holder.checkedImage.setImageResource(R.drawable.yellowdot); // teilweise defekt
            }
        }

        // setze abgeschlossen / laufend Bild
        if (this.application.preferences.getBoolean("showFinished", false) && this.getItem(position).simpleValues.keySet().contains("finished")) {
            if (this.getItem(position).simpleValues.get("finished").equals("0")) {
                holder.finishedImage.setImageResource(R.drawable.ic_play);
            } else {
                holder.finishedImage.setImageResource(R.drawable.ic_stop);
            }
            holder.finishedImage.setAlpha((float) 0.5);
        }

        // setzte view count
        if (this.getItem(position).simpleValues.keySet().contains("Views")) {
            holder.viewCount.setText(String.valueOf(Math.round(Double.valueOf(this.getItem(position).simpleValues.get("Views")) * 100) / 100.));
        }

        // setze episode count
        holder.episodeCount.setText(this.getItem(position).simpleValues.get("Count"));

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
            holder.viewCount = (TextView) convertView.findViewById(R.id.viewCount);
            holder.episodeCount = (TextView) convertView.findViewById(R.id.itemCount);
            holder.cover = (ImageView) convertView.findViewById(R.id.cover);
            holder.checkedImage = (ImageView) convertView.findViewById(R.id.checkedImage);
            holder.finishedImage = (ImageView) convertView.findViewById(R.id.seriesFinishedIcon);

            // führe Funktion zum Anpassen des Views aus
            setValues(holder, position);

            // setze onClickListener ... ändert sich nie, deshalb nur beim Initialisieren
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ViewHolder holder = (ViewHolder) v.getTag();
                    FragmentManager fragmentManager = ((MainActivity) getContext()).getSupportFragmentManager();
                    SeasonListFragment seasonListFragment = new SeasonListFragment();
                    seasonListFragment.setSeries_nr(holder.series_nr);
                    fragmentManager.beginTransaction().replace(R.id.mainContent, seasonListFragment, "seasonListFragment").addToBackStack("seasonListFragment").commit();

                }
            });


        } else {

            holder = (ViewHolder) convertView.getTag();

            setValues(holder, position);

        }

        return convertView;
    }

    private class ViewHolder {

        public TextView firstLine;
        public TextView secondLine;
        public TextView thirdLine;
        public TextView viewCount;
        public TextView episodeCount;
        public ImageView checkedImage;
        public ImageView finishedImage;
        public ImageView cover;
        public String series_nr;

    }

}
