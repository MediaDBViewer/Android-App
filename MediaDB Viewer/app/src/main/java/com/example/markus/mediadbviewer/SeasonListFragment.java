package com.example.markus.mediadbviewer;

import android.content.Context;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

public class SeasonListFragment extends OwnListFragment {

    private boolean firstCreate = true;
    private boolean contentChanged = false;
    private ArrayList<MediaObject> seasonList = new ArrayList<MediaObject>();
    private String series_nr = "0";
    private SeasonArrayAdapter adapter;
    private ListView seasonListView;
    private MyApplication application;

    public void setSeries_nr(String series_nr) {
        this.series_nr = series_nr;
    }

    public void setContentChanged() {

        this.contentChanged = true;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("series_nr", this.series_nr);
    }

    // wird bei jedem resume aufgerufen !!!
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            this.series_nr = savedInstanceState.getString("series_nr");
        }

        if (this.series_nr.equals("0")) {
            this.series_nr = this.application.seasonListFragmentseries_nr;
        }

        // hole Daten für seasonListView
        if (this.firstCreate) {
            Log.d("DEBUG", "firstCreate");
            this.fragment = inflater.inflate(R.layout.list_fragment, container, false); //wendet layout an
            this.fragment.setBackgroundColor(getResources().getColor(R.color.background));
            this.seasonListView = (ListView) fragment.findViewById(android.R.id.list); //hole Referenz auf ListView
            SeriesQueryFactory seasonQueryFactory = new SeriesQueryFactory("Staffeln", (this.application.preferences));
            updateSeasonListView(seasonQueryFactory, this.seasonListView);
            this.firstCreate = false;
        } else if (this.contentChanged) {
            SeriesQueryFactory seasonQueryFactory = new SeriesQueryFactory("Staffeln", (this.application.preferences));
            updateSeasonListView(seasonQueryFactory, this.seasonListView);
            this.contentChanged = false;
        }

        return fragment;

    }

    private void updateSeasonListView (SeriesQueryFactory seasonQueryFactory, ListView listView) {

        // notwendige Spalten für ListView können zentral hier geändert werden
        seasonQueryFactory.addColumns(new String[]{"season", "resolution", "sound", "source", "season_nr", "series_nr", "Statistik"});
        seasonQueryFactory.addConditions(new String[]{"series_nr=" + this.series_nr});
        seasonQueryFactory.setOrder("season", "ASC");
        JSONParser parser = new JSONParser((MainActivity) getActivity(), this.application);
        // setzen des ArrayAdapters
        this.adapter = new SeasonArrayAdapter(getActivity(), new ArrayList<MediaObject>(), this.application);
        listView.setAdapter(this.adapter);
        parser.setArrayAdapter(this.adapter);
        parser.setFragment(this); // ermöglicht das Setzen eines Hintergrunghinweises, wenn nötig
        parser.execute(new String[]{seasonQueryFactory.getUrl()});
        this.seasonList = parser.getMediaList();

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Staffeln (" + this.adapter.getCount() + ")");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.application = (MyApplication) getActivity().getApplication();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.application.seasonListFragmentseries_nr = this.series_nr;
    }
}
