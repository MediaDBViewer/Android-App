package com.example.markus.mediadbviewer;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EpisodeArrayAdapter extends ArrayAdapter<MediaObject> {

    public EpisodeArrayAdapter (Context context, List mediaList) {

        super(context, -1, mediaList);

    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        // Titel wird nur gesetzt, wenn das entsprechende Fragment im Vordergrund ist, sonst wird er sowie bei onResume des Fragments gesetzt
        if (((MainActivity) getContext()).getSupportFragmentManager().getBackStackEntryAt(((MainActivity) getContext()).getSupportFragmentManager().getBackStackEntryCount() - 1).getName().equals("episodeListFragment")) {
            ((MainActivity) getContext()).setTitle("Episoden (" + this.getCount() + ")");
        }
    }

    private void setValues(ViewHolder holder, int position) {

        // setzt imdbID für ViewHolder
        holder.season_nr = this.getItem(position).simpleValues.get("season_nr");
        holder.episodenumber = Integer.valueOf(this.getItem(position).simpleValues.get("episodenumber"));

        // Setze Texte des Views
        holder.firstLine.setText(String.valueOf(this.getItem(position).simpleValues.get("episodenumber")) + ") " + this.getItem(position).simpleValues.get("name"));
        //holder.secondLine.setText("Rating: " + this.seriesList.get(position).finished);

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
            holder.checkedImage = (ImageView) convertView.findViewById(R.id.checkedImage);
            holder.viewCount = (TextView) convertView.findViewById(R.id.viewCount);
            holder.cover = (ImageView) convertView.findViewById(R.id.cover);
            // entferne Cover view
            ((ViewManager) holder.cover.getParent()).removeView(holder.cover);
            //entferne second line
            ((ViewManager) holder.secondLine.getParent()).removeView(holder.secondLine);

            // führe Funktion zum Anpassen des Views aus
            setValues(holder, position);

            // setze onClickListener ... ändert sich nie, deshalb nur beim Initialisieren
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ViewHolder holder = (ViewHolder) v.getTag();
                    FragmentManager fragmentManager = ((MainActivity) getContext()).getSupportFragmentManager();
                    EpisodeDetailedFragment episodeDetailedFragment = new EpisodeDetailedFragment();
                    episodeDetailedFragment.setSeason_nr(holder.season_nr);
                    episodeDetailedFragment.setEpisodenumber(holder.episodenumber);

                    fragmentManager.beginTransaction().replace(R.id.mainContent, episodeDetailedFragment, "episodeDetailedFragment").addToBackStack("episodeDetailedFragment").commit();

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
        public ImageView checkedImage;
        public ImageView cover;
        public TextView viewCount;
        public String season_nr;
        public int episodenumber;

    }



}