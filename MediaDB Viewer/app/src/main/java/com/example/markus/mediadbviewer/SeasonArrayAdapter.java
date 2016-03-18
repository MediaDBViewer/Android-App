package com.example.markus.mediadbviewer;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class SeasonArrayAdapter extends ArrayAdapter<MediaObject> {

    private MyApplication application;

    public SeasonArrayAdapter (Context context, List mediaList, MyApplication application) {

        super(context, -1, mediaList);
        this.application = application;

    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        // Titel wird nur gesetzt, wenn das entsprechende Fragment im Vordergrund ist, sonst wird er sowie bei onResume des Fragments gesetzt
        if (((MainActivity) getContext()).getSupportFragmentManager().getBackStackEntryAt(((MainActivity) getContext()).getSupportFragmentManager().getBackStackEntryCount() - 1).getName().equals("seasonListFragment")) {
            ((MainActivity) getContext()).setTitle("Staffeln (" + this.getCount() + ")");
        }
    }

    private void setValues(ViewHolder holder, int position) {

        // setzt season_nr für ViewHolder
        holder.season_nr = this.getItem(position).simpleValues.get("season_nr");

        // Setze Texte des Views
        holder.firstLine.setText("Staffel " + this.getItem(position).simpleValues.get("season") + " (" + this.getItem(position).simpleValues.get("resolution") + ") (" + this.getItem(position).simpleValues.get("sound") + ") (" + this.getItem(position).simpleValues.get("source") + ")");
        if (this.getItem(position).simpleValues.keySet().contains("Size")) {
            holder.secondLine.setText("Größe:    " + String.valueOf(Math.round(Long.valueOf(this.getItem(position).simpleValues.get("Size")) / Math.pow(1024, 3) * 100) / 100.) + " GB");
        } else {
            holder.secondLine.setVisibility(TextView.GONE);
        }
        holder.thirdLine.setText("Laufzeit: " + String.valueOf(Integer.valueOf(this.getItem(position).simpleValues.get("Duration"))/60) + " Min");

        if (new File(this.application.coverFolderSeasonsLow + this.getItem(position).simpleValues.get("season_nr") + ".jpg").exists()) {
            holder.cover.setImageDrawable(Drawable.createFromPath(this.application.coverFolderSeasonsLow + this.getItem(position).simpleValues.get("season_nr") + ".jpg"));
        } else if (new File(this.application.coverFolderSeriesLow + this.getItem(position).simpleValues.get("series_nr") + ".jpg").exists()) {
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

        // setzte view count
        if (this.getItem(position).simpleValues.keySet().contains("Views")) {
            holder.viewCount.setText(String.valueOf(Math.round(Double.valueOf(this.getItem(position).simpleValues.get("Views")) * 100) / 100.));
        } else {
            holder.viewCount.setText("");
        }

        // setzte episode count
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
            holder.cover = (ImageView) convertView.findViewById(R.id.cover);
            holder.checkedImage = (ImageView) convertView.findViewById(R.id.checkedImage);
            holder.viewCount = (TextView) convertView.findViewById(R.id.viewCount);
            holder.episodeCount = (TextView) convertView.findViewById(R.id.itemCount);

            // führe Funktion zum Anpassen des Views aus
            setValues(holder, position);

            // setze onClickListener ... ändert sich nie, deshalb nur beim Initialisieren
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ViewHolder holder = (ViewHolder) v.getTag();
                    FragmentManager fragmentManager = ((MainActivity) getContext()).getSupportFragmentManager();
                    EpisodeListFragment episodeListFragment = new EpisodeListFragment();
                    episodeListFragment.setSeason_nr(holder.season_nr);

                    fragmentManager.beginTransaction().replace(R.id.mainContent, episodeListFragment, "episodeListFragment").addToBackStack("episodeListFragment").commit();

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
        public ImageView cover;
        public ImageView checkedImage;
        public TextView viewCount;
        public TextView episodeCount;
        public String season_nr;

    }

}