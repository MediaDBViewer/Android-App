package com.example.markus.mediadbviewer;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.app.ListFragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.ArrayList;

public class EpisodeListFragment extends OwnListFragment {

    private boolean firstCreate = true;
    private boolean contentChanged = false;
    private ArrayList<MediaObject> episodeList = new ArrayList<MediaObject>();
    private String season_nr = "0";
    private EpisodeArrayAdapter adapter;
    private ListView episodeListView;
    private MyApplication application;
    private MainActivity activity;

    public void setContentChanged() {

        this.contentChanged = true;

    }

    public void setSeason_nr(String season_nr) {
        this.season_nr = season_nr;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("season_nr", this.season_nr);
    }

    // wird bei jedem resume aufgerufen !!!
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            this.season_nr = savedInstanceState.getString("season_nr");
        }

        if (this.season_nr.equals("0")) {
            this.season_nr = this.application.episodeListFragmentseason_nr;
        }

        // hole Daten für episodeListView
        if (this.firstCreate) {

            // lade entsprechendes Layout
            if (this.activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                this.fragment = inflater.inflate(R.layout.episode_list_fragment_landscape, container, false);
            } else {
                this.fragment = inflater.inflate(R.layout.episode_list_fragment, container, false); //wendet layout an
            }
            this.fragment.setBackgroundColor(getResources().getColor(R.color.background));

            this.episodeListView = (ListView) fragment.findViewById(android.R.id.list); //hole Referenz auf ListView
            // setze Cover vor ListView
            ImageView cover = (ImageView) fragment.findViewById(R.id.seasonCover);
            if (new File(this.application.coverFolderSeasonHigh + this.season_nr + ".jpg").exists()) {
                cover.setImageDrawable(Drawable.createFromPath(this.application.coverFolderSeasonHigh + this.season_nr + ".jpg"));
            } else {
                cover.setImageResource(R.drawable.default_cover);
            }
            SeriesQueryFactory episodeQueryFactory = new SeriesQueryFactory("Episoden", (this.application.preferences));
            updateEpisodeListView(episodeQueryFactory, episodeListView);
            this.firstCreate = false;
        } else if (this.contentChanged) {
            SeriesQueryFactory episodeQueryFactory = new SeriesQueryFactory("Episoden", (this.application.preferences));
            updateEpisodeListView(episodeQueryFactory, episodeListView);
            this.contentChanged = false;
        }

        return fragment;

    }

    private void updateEpisodeListView (SeriesQueryFactory episodeQueryFactory, ListView listView) {

        // notwendige Spalten für ListView können zentral hier geändert werden
        episodeQueryFactory.addColumns(new String[]{"episodenumber", "name", "series_nr", "season_nr", "checked", "views"});
        episodeQueryFactory.addConditions(new String[]{"season_nr=" + this.season_nr});
        JSONParser parser = new JSONParser((MainActivity) getActivity(), this.application);
        this.adapter = new EpisodeArrayAdapter(getActivity(), this.episodeList);
        listView.setAdapter(this.adapter);
        parser.setArrayAdapter(this.adapter);
        parser.setFragment(this); // ermöglicht das Setzen eines Hintergrunghinweises, wenn nötig
        parser.execute(new String[]{episodeQueryFactory.getUrl()});
        this.episodeList = parser.getMediaList();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.adapter != null) {
            getActivity().setTitle("Episoden (" + this.adapter.getCount() + ")");
        } else {
            getActivity().setTitle("Episoden");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.application.episodeListFragmentseason_nr = this.season_nr;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.application = (MyApplication) getActivity().getApplication();
        this.activity = (MainActivity) getActivity();
    }
}
