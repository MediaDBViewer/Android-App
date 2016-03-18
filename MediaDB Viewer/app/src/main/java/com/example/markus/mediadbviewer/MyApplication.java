package com.example.markus.mediadbviewer;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MyApplication extends Application {

    public String rootCoverFolder;
    public String sdCardName = null;
    public String externalAppFolder = "";
    public String internalAppFolder = "/data/data/com.example.markus.mediadbviewer/";
    public SharedPreferences preferences;
    public SharedPreferences cache;
    public SharedPreferences cacheTimestamps;
    public ArrayList<String> movieFilterShown;
    public ArrayList<String> valuesMovieShown;
    public ArrayList<String> valuesEpisodeShown;
    public ArrayList<String> availableStatistics;
    public HashMap<String, String> detailedListNames = new HashMap<String, String>();
    public HashMap<String, String> statisticNames = new HashMap<String, String>();
    public HashMap<String, String> orderNames = new HashMap<String, String>();
    public HashMap<String, Spinner> filterSpinner = new HashMap<String, Spinner>();
    public HashMap<String, EditText> absoluteFilters = new HashMap<String, EditText>();
    public HashMap<String, EditText> lowerBoundFilters = new HashMap<String, EditText>();
    public HashMap<String, EditText> upperBoundFilters = new HashMap<String, EditText>();
    public HashMap<String, LinearLayout> filterContents = new HashMap<String, LinearLayout>();
    public HashMap<String, TextView> filterHeaders = new HashMap<String, TextView>();
    public String[] movieFilterSpinner = new String[]{"dimension", "Genre", "language", "fsk", "acodecger", "acodeceng", "vcodec", "resolution", "channelsger", "channelseng", "hdd", "checked", "views", "youtube"};
    public String[] movieComplexFilter = new String[]{"year", "size", "duration", "added", "lastview"};
    public String[] defaultValuesMovie = new String[]{"name", "fsk", "imdbID", "3d", "year", "rating", "youtube", "resolution", "duration", "size", "added",
            "Genre", "summary", "Schauspieler"};
    public String[] defaultValuesEpisode = new String[]{"episodenumber", "name", "source", "duration", "size"};
    public String defaultMovieFilter[] = new String[]{"dimension", "Genre", "language", "fsk", "resolution", "year", "size", "duration"};
    private Bundle savedFilterBundle;

    // Variablen für root Layout
    public Menu menu = null;
    public ActionBarDrawerToggle toggle;
    public DrawerLayout drawerRootLayout;
    public ListView navigationListView;
    public LinearLayout filterLayout;
    public LinearLayout navigationLayout;
    public NavigationView navigationRootLayout;
    public DrawerLayout mDrawerLayout;
    public ArrayList<NavigationbarIcon> navigationIcons = new ArrayList<NavigationbarIcon>();

    // fallback keys, falls Fragmente zerstört werden
    public String seasonListFragmentseries_nr;
    public String episodeListFragmentseason_nr;

    //cover Folder Variablen
    public String coverFolderMoviesLow = this.rootCoverFolder + "Filme/low/";
    public String coverFolderSeriesLow = this.rootCoverFolder + "Serien/low/";
    public String coverFolderSeasonsLow = this.rootCoverFolder + "Staffeln/low/";
    public String coverFolderMoviesHigh = this.rootCoverFolder + "Filme/high/";
    public String coverFolderSeriesHigh = this.rootCoverFolder + "Serien/high/";
    public String coverFolderSeasonHigh = this.rootCoverFolder + "Staffeln/high/";

    public void configureDefaults() {

        // hole Rechte
        MovieQueryFactory factory = new MovieQueryFactory(this.preferences);
        JSONParser parser = new JSONParser(null, this);
        try{parser.execute(new String[]{factory.getRightsURL()}).get();} catch (Exception e) {}
        HashMap<String, ArrayList<String>> rightsHashMap = parser.getRightsMap();

        // setze Standard Spalten Filme
        ArrayList<String> columns = rightsHashMap.get("SpaltenFilme");
        ArrayList<String> availableColumns = new ArrayList<>();
        for (String column : this.defaultValuesMovie) {
            if (columns.contains(column)) {
                availableColumns.add(column);
            }
        }
        this.saveArrayList("valuesMovieShown", availableColumns);

        // setze Standard Spalten Episoden
        columns = rightsHashMap.get("SpaltenEpisoden");
        availableColumns = new ArrayList<>();
        for (String column : this.defaultValuesEpisode) {
            if (columns.contains(column)) {
                availableColumns.add(column);
            }
        }
        this.saveArrayList("valuesEpisodeShown", availableColumns);

        // setze Standard Filter für Film
        ArrayList<String> filters = new ArrayList<>();
        for (String filter : this.defaultMovieFilter) {
            if (this.valuesMovieShown.contains(filter) || filter.equals("language") || (filter.equals("dimension") && this.valuesMovieShown.contains("3d"))) {
                filters.add(filter);
            }
        }
        this.saveArrayList("movieFilterShown", filters);

        // setze Updaterecht
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean("updateRight", parser.getUpdateRight());
        editor.commit();
    }

    public void saveArrayList(String key, ArrayList<String> values) {
        SharedPreferences.Editor editor = this.preferences.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.commit();
        if (key.equals("valuesMovieShown")) {
            this.valuesMovieShown = values;
        } else if (key.equals("valuesEpisodeShown")) {
            this.valuesEpisodeShown = values;
        } else if (key.equals("movieFilterShown")) {
            this.movieFilterShown = values;
        } else if (key.equals("availableStatistics")) {
            this.availableStatistics = values;
        }
    }

    public ArrayList<String> getArrayList(String key) {
        String json = this.preferences.getString(key, null);
        ArrayList<String> values = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String value = a.optString(i);
                    values.add(value);
                }
            } catch (JSONException e) {
            }
        }
        return values;
    }

    private void initializeHashMaps() {

        this.detailedListNames.put("name", "Name");
        this.detailedListNames.put("size", "Größe");
        this.detailedListNames.put("duration", "Laufzeit");
        this.detailedListNames.put("width", "Bildbreite");
        this.detailedListNames.put("height", "Bildhöhe");
        this.detailedListNames.put("vcodec", "Videocodec");
        this.detailedListNames.put("totalbitrate", "Gesamtbitrate");
        this.detailedListNames.put("resolution", "Auflösung");
        this.detailedListNames.put("acodecger", "Deutscher Audiocodec");
        this.detailedListNames.put("abitrateger", "Deutsche Audiobitrate");
        this.detailedListNames.put("channelsger", "Deutsche Audiokanäle");
        this.detailedListNames.put("acodeceng", "Englischer Audiocodec");
        this.detailedListNames.put("abitrateeng", "Englische Audiobitrate");
        this.detailedListNames.put("channelseng", "Englische Audiokanäle");
        this.detailedListNames.put("hdd", "Festplatte");
        this.detailedListNames.put("checked", "Geprüft");
        this.detailedListNames.put("views", "Gesehen (Anzahl)");
        this.detailedListNames.put("added", "Hinzugefügt");
        this.detailedListNames.put("md5", "MD5");
        this.detailedListNames.put("dimension", "3D");
        this.detailedListNames.put("comment", "Kommentar");
        this.detailedListNames.put("imdbID", "imdbID");
        this.detailedListNames.put("3d", "3D");
        this.detailedListNames.put("year", "Jahr");
        this.detailedListNames.put("rating", "Rating");
        this.detailedListNames.put("Genre", "Genre");
        this.detailedListNames.put("genre", "Genre");
        this.detailedListNames.put("language", "Englisch");
        this.detailedListNames.put("3d", "3D");
        this.detailedListNames.put("lastView", "Gesehen (Datum)");
        this.detailedListNames.put("summary","Inhalt");
        this.detailedListNames.put("youtube", "Trailer");
        this.detailedListNames.put("Schauspieler", "Schauspieler");
        this.detailedListNames.put("episodenumber", "Folge");
        this.detailedListNames.put("source", "Quelle");
        this.detailedListNames.put("fsk", "Altersfreigabe");
        this.detailedListNames.put("titelDeutsch", "Deutscher Titel");
        this.detailedListNames.put("titelOriginal", "Originaltitel");
        this.detailedListNames.put("tagline", "Zusatztitel");
        this.orderNames.put("Name", "name");
        this.orderNames.put("Jahr", "year");
        this.orderNames.put("Laufzeit", "duration");
        this.orderNames.put("Rating", "rating");
        this.orderNames.put("Hinzugefügt", "added");
        this.orderNames.put("Größe", "size");
        this.orderNames.put("Views", "views");
        this.orderNames.put("Gesehen", "lastView");
        this.orderNames.put("name", "Name");
        this.orderNames.put("year", "Jahr");
        this.orderNames.put("duration", "Laufzeit");
        this.orderNames.put("rating", "Rating");
        this.orderNames.put("added", "Hinzugefügt");
        this.orderNames.put("size", "Größe");
        this.orderNames.put("views", "Views");
        this.orderNames.put("lastView", "Gesehen");
        this.statisticNames.put("DBStatistik","Datenbank Statistik");
        this.statisticNames.put("GenreFilmanzahl","Genre Filmanzahl");
        this.statisticNames.put("SchauspielerFilmanzahl","Schauspieler Filmanzahl");
        this.statisticNames.put("lastMD5Check","Letzte MD5-Überprüfung");
        this.statisticNames.put("belegterSpeicher","Belegter Speicher");
        this.statisticNames.put("freierSpeicher","Freier Speicher");
        this.statisticNames.put("watchStatistic","Monatsstatistik");
        this.statisticNames.put("defekteEpisoden","Defekte Episoden");
        this.statisticNames.put("defekteFilme","Defekte Filme");
        this.statisticNames.put("laufzeitGesehen","Laufzeit des gesehenen Materials");
        this.statisticNames.put("lastUpdate","Letzter Datenaustausch");
        this.statisticNames.put("michiNewData","Michi - neues Datenvolumen");
        this.statisticNames.put("michiNewDataPerHDD","Michi - neues Datenvolumen pro HDD");
        this.statisticNames.put("michiNewMovies","Michi - neue Filme");
        this.statisticNames.put("michiNewSeasons","Michi - neue Staffeln");
        this.statisticNames.put("prozentualGesehen","Prozentual gesehen");
        this.statisticNames.put("prozentualDefekt","Prozentual defekt");

    }

    public void initializeFilterBar(Activity activity) {

        // lösche alle Filter aus den Holdern
        this.absoluteFilters.clear();
        this.filterSpinner.clear();


        ///// initialisiere alle Spinner
        // befülle Spinner für Sortierrichtung
        final Spinner directionSpinner = (Spinner) this.filterLayout.findViewById(R.id.movieDirectionSpinner);
        ArrayAdapter<CharSequence> directionSpinnerAdapter = ArrayAdapter.createFromResource(activity, R.array.directionSpinner, R.layout.filter_spinner_layout);
        directionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directionSpinner.setAdapter(directionSpinnerAdapter);

        // befülle Spinner für Sortierkategorie
        final Spinner categorySpinner = (Spinner) this.filterLayout.findViewById(R.id.movieCategorySpinner);
        ArrayList<String> availableCategories = new ArrayList<>();
        // prüfe auf welche Spalten der Zugriff erlaubt ist
        for (String category : getResources().getStringArray(R.array.categorySpinner)) {
            if (this.valuesMovieShown.contains(category)) {
                availableCategories.add(this.orderNames.get(category));
            }
        }
        ArrayAdapter<CharSequence> categorySpinnerAdapter = new ArrayAdapter<CharSequence>(activity, R.layout.filter_spinner_layout, availableCategories.toArray(new String[0]));
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categorySpinnerAdapter);

        TableLayout table = (TableLayout) this.filterLayout.findViewById(R.id.spinnerTable);
        table.removeAllViews();// lösche alle Spinner
        Spinner spinner;
        ArrayAdapter<CharSequence> spinnerAdapter;
        TableRow row;
        TextView text;

        for (String currentSpinner : this.movieFilterSpinner) {

            if (this.movieFilterShown.contains(currentSpinner)) {

                // erstelle neue Tabellenzeile
                //row.inflate(activity, R.layout.simple_spinner, null);
                row = (TableRow) activity.getLayoutInflater().inflate(R.layout.simple_spinner, null);
                spinner = (Spinner) row.findViewById(R.id.filterSpinner);

                // erstelle Beschreibung
                text = (TextView) row.findViewById(R.id.filterSpinnerDescription);
                text.setText(getResources().getText(activity.getResources().getIdentifier(currentSpinner + "SpinnerDescription", "string", activity.getPackageName())));

                // erstelle Spinner
                spinnerAdapter = ArrayAdapter.createFromResource(activity, activity.getResources().getIdentifier(currentSpinner + "SpinnerArray", "array", activity.getPackageName()), R.layout.filter_spinner_layout);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);

                // füge Spinner zur Tabelle der Spinner hinzu
                table.addView(row);
                this.filterSpinner.put(currentSpinner, spinner);
            }

        }



        //// initialisiere komplexe Filter

        LinearLayout filterRootLayout = (LinearLayout) activity.findViewById(R.id.complexFilterRootLayout);
        filterRootLayout.removeAllViews(); // löschen alle komplexen Filter
        LinearLayout layout;
        EditText absoluteFilter;
        EditText lowerBoundFilter;
        EditText upperBoundFilter;
        TextView header;
        LinearLayout content;

        for (String complexFilter : this.movieComplexFilter) {

            if (this.movieFilterShown.contains(complexFilter)) {

                layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.complex_filter, null);

                // setze Header Text und onClick Methode
                content = (LinearLayout) layout.findViewById(R.id.filterContent);
                this.filterContents.put(complexFilter, content);
                header = (TextView) layout.findViewById(R.id.filterHeader);
                this.filterHeaders.put(complexFilter, header);
                header.setText("[+] " + getResources().getString(activity.getResources().getIdentifier(complexFilter + "HeaderName", "string", activity.getPackageName())));
                header.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        LinearLayout parent = (LinearLayout) v.getParent();
                        LinearLayout content = (LinearLayout) parent.findViewById(R.id.filterContent);
                        TextView header = (TextView) parent.findViewById(R.id.filterHeader);

                        if (content.getVisibility() == RelativeLayout.INVISIBLE) {
                            header.setText(((String) header.getText()).replace("[+]", "[-]"));
                            content.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                            content.setVisibility(RelativeLayout.VISIBLE);
                            content.invalidate(); // für Android 5 notwendig
                            content.requestLayout(); // für Android 5 notwendig
                        } else {
                            content.setVisibility(RelativeLayout.INVISIBLE);
                            header.setText(((String) header.getText()).replace("[-]", "[+]"));
                            content.getLayoutParams().height = 0;
                            content.invalidate(); // für Android 5 notwendig
                            content.requestLayout(); // für Android 5 notwendig
                        }

                    }
                });

                // setze Hints der EditText Felder
                absoluteFilter = (EditText) layout.findViewById(R.id.filterAbsoluteField);
                absoluteFilter.setHint(activity.getResources().getIdentifier(complexFilter + "AbsoluteHint", "string", activity.getPackageName()));
                this.absoluteFilters.put(complexFilter, absoluteFilter);
                lowerBoundFilter = (EditText) layout.findViewById(R.id.filterLowerBoundField);
                lowerBoundFilter.setHint(activity.getResources().getIdentifier(complexFilter + "LowerBoundHint", "string", activity.getPackageName()));
                this.lowerBoundFilters.put(complexFilter, lowerBoundFilter);
                upperBoundFilter = (EditText) layout.findViewById(R.id.filterUpperBoundField);
                upperBoundFilter.setHint(activity.getResources().getIdentifier(complexFilter + "UpperBoundHint", "string", activity.getPackageName()));
                this.upperBoundFilters.put(complexFilter, upperBoundFilter);

                // setze Wertebereich für Eingabefelder
                if (complexFilter.equals("duration") || complexFilter.equals("year")) {
                    absoluteFilter.setInputType(InputType.TYPE_CLASS_NUMBER);
                    lowerBoundFilter.setInputType(InputType.TYPE_CLASS_NUMBER);
                    upperBoundFilter.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else if (complexFilter.equals("size")) {
                    absoluteFilter.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    lowerBoundFilter.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    upperBoundFilter.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                }

                // setze Text der TextViews
                ((TextView) layout.findViewById(R.id.filterAbsoluteText)).setText(activity.getResources().getIdentifier(complexFilter + "AbsoluteText", "string", activity.getPackageName()));
                ((TextView) layout.findViewById(R.id.filterLowerBoundText)).setText(activity.getResources().getIdentifier("lowerBoundText", "string", activity.getPackageName()));
                ((TextView) layout.findViewById(R.id.filterUpperBoundText)).setText(activity.getResources().getIdentifier("upperBoundText", "string", activity.getPackageName()));

                filterRootLayout.addView(layout);

            }

        }

    }

    public void saveFilterSettings() {

        this.savedFilterBundle = new Bundle();
        for (String key : this.filterSpinner.keySet()) {
            this.savedFilterBundle.putInt(key, this.filterSpinner.get(key).getSelectedItemPosition());
        }
        for (String key : this.absoluteFilters.keySet()) {
            this.savedFilterBundle.putString(key + "absolute", this.absoluteFilters.get(key).getText().toString());
            this.savedFilterBundle.putString(key + "lowerBound", this.lowerBoundFilters.get(key).getText().toString());
            this.savedFilterBundle.putString(key + "upperBound", this.lowerBoundFilters.get(key).getText().toString());
        }

    }

    public void restoreFilterSettings() {

        if (this.savedFilterBundle != null) {

            for (String key : this.filterSpinner.keySet()) {
                this.filterSpinner.get(key).setSelection(this.savedFilterBundle.getInt(key));
            }
            for (String key : this.absoluteFilters.keySet()) {
                this.absoluteFilters.get(key).setText(this.savedFilterBundle.getString(key + "absolute"));
                this.lowerBoundFilters.get(key).setText(this.savedFilterBundle.getString(key + "lowerBound"));
                this.upperBoundFilters.get(key).setText(this.savedFilterBundle.getString(key + "upperBound"));
            }

        }

    }

    public void setCoverRootFolder() {

        if (this.preferences.getString("storageType", getResources().getStringArray(R.array.storageType)[0]).equals(getResources().getStringArray(R.array.storageType)[0])) {
            this.rootCoverFolder = this.internalAppFolder + "cover/";
        } else {
            this.rootCoverFolder = this.externalAppFolder + "cover/";
        }

        //cover Folder Variablen
        coverFolderMoviesLow = this.rootCoverFolder + "Filme/low/";
        coverFolderSeriesLow = this.rootCoverFolder + "Serien/low/";
        coverFolderSeasonsLow = this.rootCoverFolder + "Staffeln/low/";
        coverFolderMoviesHigh = this.rootCoverFolder + "Filme/high/";
        coverFolderSeriesHigh = this.rootCoverFolder + "Serien/high/";
        coverFolderSeasonHigh = this.rootCoverFolder + "Staffeln/high/";

    }

    private void setExternalStoragePath() {

        String[] blacklistedDevices = new String[]{"sdcard0", "usbdisk", "self", "0", "asec", "obb", "secure"};
        ArrayList<String> devicesFound = new ArrayList<>();
        File mountRoot = null;

        // setze Mount-Root-Folder bei älteren Versionen "/mnt/" statt "/storage/"
        if (new File("/storage/").exists()) {
            mountRoot = new File("/storage/");
        } else if (new File("/mnt/").exists()) {
            mountRoot = new File("/mnt/");
        }

        // ermittle Speicherkarten Mountpoint nur, wenn einer der Root-Mountpoints existiert
        if (mountRoot != null) {

            if (Arrays.asList(mountRoot.list()).contains("sdcard1")) {
                this.sdCardName = "sdcard1";
                this.externalAppFolder = mountRoot.getAbsolutePath() + "/sdcard1/Android/data/com.example.markus.mediadbviewer/files/";
            } else if (Arrays.asList(mountRoot.list()).contains("extSdCard")) {
                this.sdCardName = "extSdCard";
                this.externalAppFolder = mountRoot.getAbsolutePath() + "/extSdCard/Android/data/com.example.markus.mediadbviewer/files/";
            } else {

                for (String device : mountRoot.list()) {
                    if (!Arrays.asList(blacklistedDevices).contains(device) && !device.toUpperCase().contains("USB") && !device.contains("emulated") && new File("/storage/" + device).isDirectory()) {
                        devicesFound.add(device);
                    }
                    if (devicesFound.size() == 1) {
                        this.sdCardName = devicesFound.get(0);
                        this.externalAppFolder = mountRoot.getAbsolutePath() + "/" + this.sdCardName + "/Android/data/com.example.markus.mediadbviewer/files/";
                    }
                }

            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.preferences = getSharedPreferences("settings", 0);
        this.cache = getSharedPreferences("cache", 0);
        this.cacheTimestamps = getSharedPreferences("cacheTimestamps", 0);
        this.valuesEpisodeShown = this.getArrayList("valuesEpisodeShown");
        this.valuesMovieShown = this.getArrayList("valuesMovieShown");
        this.movieFilterShown = this.getArrayList("movieFilterShown");
        this.availableStatistics = this.getArrayList("availableStatistics");
        this.setExternalStoragePath();
        this.setCoverRootFolder();
        this.initializeHashMaps();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }



}