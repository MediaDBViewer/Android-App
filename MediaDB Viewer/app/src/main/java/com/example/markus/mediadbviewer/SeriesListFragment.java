package com.example.markus.mediadbviewer;

import android.content.Context;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

public class SeriesListFragment extends OwnListFragment {

    private boolean firstCreate = true;
    private boolean contentChanged = false;
    private SeriesArrayAdapter adapter;
    private ArrayList<MediaObject> seriesList = new ArrayList<MediaObject>();
    private ListView seriesListView;
    private MyApplication application;

    public void setContentChanged() {

        this.contentChanged = true;

    }

    // wird bei jedem resume aufgerufen !!!
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (this.firstCreate) {
            this.fragment = inflater.inflate(R.layout.list_fragment, container, false); //wendet layout an
            this.seriesListView = (ListView) fragment.findViewById(android.R.id.list); //hole Referenz auf ListView
            SeriesQueryFactory seriesQueryFactory = new SeriesQueryFactory("Serien", this.application.preferences);
            updateSeriesListView(seriesQueryFactory, seriesListView);
            this.firstCreate = false;
            this.fragment.setBackgroundColor(getResources().getColor(R.color.background));
        } else if (this.contentChanged) {
            SeriesQueryFactory seriesQueryFactory = new SeriesQueryFactory("Serien", this.application.preferences);
            updateSeriesListView(seriesQueryFactory, this.seriesListView);
            this.contentChanged = false;
        }

        return this.fragment;

    }

    private void updateSeriesListView (SeriesQueryFactory seriesQueryFactory, ListView listView) {

        // notwendige Spalten für ListView können zentral hier geändert werden
        seriesQueryFactory.addColumns(new String[]{"name", "finished", "series_nr", "Statistik"});
        seriesQueryFactory.setOrder("name", "ASC");
        JSONParser parser = new JSONParser((MainActivity) getActivity(), this.application);
        this.adapter = new SeriesArrayAdapter(getActivity(), new ArrayList<MediaObject>(), this.application);
        listView.setAdapter(this.adapter);
        parser.setArrayAdapter(this.adapter);
        parser.setFragment(this); // ermöglicht das Setzen eines Hintergrunghinweises, wenn nötig
        parser.execute(new String[]{seriesQueryFactory.getUrl()});

    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.adapter != null) {
            getActivity().setTitle("Serien (" + this.adapter.getCount() + ")");
        } else {
            getActivity().setTitle("Serien");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.application = (MyApplication) getActivity().getApplication();
    }
}
