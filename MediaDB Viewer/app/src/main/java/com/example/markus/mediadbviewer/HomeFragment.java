package com.example.markus.mediadbviewer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HomeFragment extends DetailedFragment {

    private View fragment = null;
    private ArrayList<MediaObject> movieList = new ArrayList<MediaObject>();
    public RecyclerView coverFlow;
    public ScrollingLinearLayoutManager layoutManager;
    public CoverFlowAdapter adapter;
    private MyApplication application;
    private MainActivity activity;
    private JSONParser parser;

    public final void closeAllFragments() {

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager(); //Manager zum Verwalten der Fragmente
        // schließe alle offenen Fragmente
        for (int i=fragmentManager.getBackStackEntryCount(); i>0; i--) {
            fragmentManager.popBackStack();
        }

    }

    @Override
    public void updateData() {

        // sortiere Filme aus, zu denen kein Cover existiert
        boolean foundMissingCover = false;
        URLChecker urlChecker;

        // Key ist gültig
        if ((parser.isKeyValid() == null) || (parser.isKeyValid() == true)) {

            ArrayList<MediaObject> coverFlowMovies = new ArrayList<MediaObject>();
            ArrayList<MediaObject> fullList = this.parser.getMediaList();
            String url = "";
            if (fullList.size() > 0) {
                for (MediaObject media : fullList) {
                    File f = new File(this.application.coverFolderMoviesHigh + media.simpleValues.get("imdbID") + ".jpg");
                    if (f.exists()) {
                        coverFlowMovies.add(media);
                    } else if (!foundMissingCover) {
                        urlChecker = new URLChecker();
                        if (this.application.preferences.getString("coverServer", "").length() > 0) {
                            url = (this.application.preferences.getString("coverServer", null));
                        } else {
                            url = (this.application.preferences.getString("server", null));
                        }
                        url += "cover/Filme/high/" + media.simpleValues.get("imdbID") + ".jpg";
                        try {
                            urlChecker.execute(new String[]{url}).get();
                        } catch (Exception e) {
                        }
                        if (urlChecker.getResult()) {
                            foundMissingCover = true;
                        }
                    }
                }
            }

            // zeige neue Cover verfügbar Dialog
            if (foundMissingCover) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                AlertDialog dialog = builder.setMessage(R.string.newCoversAvailableMessage).setTitle(R.string.newCoversAvailableTitle).create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }

            // erstelle RecyclerView mit Adapter und LayoutOut Manager
            if (coverFlowMovies.size() > 0) {
                this.layoutManager = new ScrollingLinearLayoutManager(this.application, LinearLayoutManager.HORIZONTAL, false, parser.getMediaList().size() * 100 * (41 - 2 * Integer.valueOf(this.application.preferences.getString("scrollSpeed", "5")))); // setze Scrolldauer
                this.coverFlow = (RecyclerView) this.fragment.findViewById(R.id.coverFlow);
                this.coverFlow.setItemViewCacheSize(0);
                this.coverFlow.setLayoutManager(this.layoutManager);
                this.adapter = new CoverFlowAdapter(coverFlowMovies, ((MainActivity) getActivity()), this.application);
                this.coverFlow.setAdapter(this.adapter);
                coverFlow.post(new Runnable() {
                    @Override
                    public void run() {
                        //call smooth scroll
                        try {
                            layoutManager.smoothScrollToPosition(coverFlow, null, adapter.getItemCount());
                        } catch (Exception e) {
                        }
                    }
                });
            }

        // wenn Key ungültig, dann lösche Key und öffne Einstellungen
        } else {
            this.application.preferences.edit().putString("apikey", "removed").commit();
            this.activity.settingsMissing();
        }
    }

    // wird bei jedem resume aufgerufen !!!
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (this.fragment == null) {

            if (this.activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                this.fragment = inflater.inflate(R.layout.home_fragment_landscape, container, false);
            } else {
                this.fragment = inflater.inflate(R.layout.home_fragment, container, false); //wendet layout an
            }
            this.fragment.setBackgroundColor(getResources().getColor(R.color.background));

            // hole Liste mit Filmen für den CoverFlow
            MovieQueryFactory movieQueryFactory = new MovieQueryFactory(this.application.preferences);
            movieQueryFactory.addColumns(new String[]{"imdbID", "3d"});
            movieQueryFactory.addConditions(new String[]{"Gesehenzaehler=" + (this.application.preferences.getBoolean("coverFlowHideSeen", false) ? "0" : ">0")});
            movieQueryFactory.setOrder(this.application.preferences.getString("coverFlowOrder", "added"), "DESC");
            movieQueryFactory.addConditions(new String[]{"Anzahl=" + this.application.preferences.getString("coverFlowItemCount", "30")});
            this.parser = new JSONParser(((MainActivity) getActivity()), this.application);
            this.parser.setFragment(this);
            this.parser.execute(new String[]{movieQueryFactory.getUrl()});

            // setze onClickListener für SplashButton
            LinearLayout movieButton = (LinearLayout) this.fragment.findViewById(R.id.splashButtonMovieLayout);
            LinearLayout seriesButton = (LinearLayout) this.fragment.findViewById(R.id.splashButtonSeriesLayout);
            LinearLayout statisticButton = (LinearLayout) this.fragment.findViewById(R.id.splashButtonStatisticLayout);
            LinearLayout settingsButton = (LinearLayout) this.fragment.findViewById(R.id.splashButtonSettingsLayout);
            movieButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeAllFragments();
                    MovieListFragment fragment = new MovieListFragment(); // eigenes Fragment initialisieren
                    ((MainActivity) getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, fragment, "movieListFragment").addToBackStack("movieListFragment").commit();
                }
            });
            seriesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeAllFragments();
                    SeriesListFragment fragment = new SeriesListFragment(); // eigenes Fragment initialisieren
                    ((MainActivity) getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, fragment, "seriesListFragment").addToBackStack("seriesListFragment").commit();
                }
            });
            statisticButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeAllFragments();
                    StatisticFragment fragment = new StatisticFragment(); // eigenes Fragment initialisieren
                    ((MainActivity) getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, fragment, "statisticFragment").addToBackStack("statisticFragment").commit();
                }
            });
            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeAllFragments();
                    SettingsFragment fragment = new SettingsFragment(); // eigenes Fragment initialisieren
                    ((MainActivity) getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, fragment, "settingsFragment").addToBackStack("settingsFragment").commit();
                }
            });

        }

        return this.fragment;

    }

    @Override
    public void onResume() {
        super.onResume();
        //((MainActivity) getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((MainActivity) getActivity()).setTitle("Home");
    }

    @Override
    public void onPause() {
        //((MainActivity) getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (MainActivity) getActivity();
        this.application =  (MyApplication) getActivity().getApplication();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*
        this.coverFlow.setAdapter(null);
        Iterator it = this.adapter.bitmaps.entrySet().iterator();
        while (it.hasNext())
        {
            Bitmap bitmap = (Bitmap) ((HashMap.Entry) it.next()).getValue();
            bitmap.recycle();
            it.remove();
        }*/

    }
}
