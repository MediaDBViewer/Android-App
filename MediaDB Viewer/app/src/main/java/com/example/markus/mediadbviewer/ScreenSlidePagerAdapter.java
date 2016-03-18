package com.example.markus.mediadbviewer;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

    private static final int NUM_PAGES = 4;
    public ScreenSlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }
    private android.support.v4.app.Fragment fragments[] = new android.support.v4.app.Fragment[]{null, null, null, null};
    private MainActivity activity;
    private Boolean serverAvailable = null;
    private MyApplication application;
    private HashMap<String, ArrayList<String>> rightsHashMap;



    public void setContext(MyApplication application, MainActivity activity) {
        this.activity = activity;
        this.application = application;
    }

    @Override
    public Fragment getItem(int position) {
        if (fragments[position] == null) {
            if (this.rightsHashMap == null) {
                MovieQueryFactory factory = new MovieQueryFactory(this.application.preferences);
                JSONParser parser = new JSONParser(this.activity, this.application);
                try{parser.execute(new String[]{factory.getRightsURL()}).get();} catch (Exception e) {}
                this.rightsHashMap = parser.getRightsMap();
                if (this.application.preferences.getBoolean("updateRight", !parser.getUpdateRight()) != parser.getUpdateRight()) {
                    SharedPreferences.Editor editor = this.application.preferences.edit();
                    editor.putBoolean("updateRight", parser.getUpdateRight());
                    editor.commit();
                }
                if (!this.application.preferences.getString("apikey", "").equals("")) { // sonst würde App bei ersten Start abstürzen

                    // Abfragen der Rechte vom Server war erfolgreich
                    if (this.rightsHashMap.keySet().contains("StatistikViews")) {
                        this.serverAvailable = true;
                        if (this.application.availableStatistics.size() != this.rightsHashMap.get("StatistikViews").size()) {
                            this.application.saveArrayList("availableStatistics", this.rightsHashMap.get("StatistikViews"));
                        }

                    } else {
                        this.serverAvailable = false;
                    }
                }
            }

            // Server ist nicht verfügbar
            if (serverAvailable != null && !serverAvailable) {
                fragments[position] = new DummySettingsFragment();
            } else {
                if (position == 0) {
                    fragments[position] = new GeneralSettingsFragment();
                } else if (position == 1) {
                    DetailedSettingsFragment fragment = new DetailedSettingsFragment();
                    ArrayList<String> allFilter = new ArrayList<>();
                    if (!this.application.preferences.getString("apikey", "").equals("")) {
                        for (String x : this.application.movieFilterSpinner) {
                            if (x.equals("dimension")) {
                                x = "3d";
                            }
                            if (this.rightsHashMap.get("SpaltenFilme").contains(x)) {
                                if (x.equals("3d")) {
                                    x = "dimension";
                                }
                                allFilter.add(x);
                            }
                        }
                        for (String x : this.application.movieComplexFilter) {
                            if (this.rightsHashMap.get("SpaltenFilme").contains(x)) {
                                allFilter.add(x);
                            }
                        }
                    }
                    if (!this.application.preferences.getString("apikey", "").equals("")) {
                        allFilter.add("language");
                    }
                    fragment.setValues(allFilter, application.movieFilterShown, "movieFilterShown");
                    fragments[position] = fragment;
                } else if (position == 2) {
                    DetailedSettingsFragment fragment = new DetailedSettingsFragment();
                    fragment.setValues(this.rightsHashMap.get("SpaltenFilme"), application.valuesMovieShown, "valuesMovieShown");
                    fragments[position] = fragment;
                } else if (position == 3) {
                    DetailedSettingsFragment fragment = new DetailedSettingsFragment();
                    fragment.setValues(this.rightsHashMap.get("SpaltenEpisoden"), application.valuesEpisodeShown, "valuesEpisodeShown");
                    fragments[position] = fragment;
                }
            }
        }
        return fragments[position];

    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Allgemein";
            case 1:
                return "Filter Filme";
            case 2:
                return "Details Filme";
            case 3:
                return "Details Episoden";
        }

        return null;
    }
}