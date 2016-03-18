package com.example.markus.mediadbviewer;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v4.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MovieListFragment extends OwnListFragment {

    private boolean initialized = false;
    private boolean hideFilter = false;
    private HashMap<String, String> complexFilterNames = new HashMap<String, String>();
    private HashMap<String, String> directionNames = new HashMap<String, String>();
    private ListView listView;
    private MovieArrayAdapter adapter;
    private MainActivity activity;
    private MyApplication application;
    private HashMap<String, String> currentValues = new HashMap<>();
    private String lastURL;
    private boolean landscape = false;
    private String previewImdbID = null;
    private int previewDimension;

    // wenn diese Funktion vor der onCreate-Funktion aufgerufen wird, kann eine URL gesetzt werden, die zum Abruf der Liste genutzt wird
    public void setURL(String url) {
        this.lastURL = url;
    }

    public void hideFilter () {
        this.hideFilter = true;
    }

    private void initializeHashMaps() {

        this.complexFilterNames.put("year", "Jahr");
        this.complexFilterNames.put("size", "Groesse");
        this.complexFilterNames.put("duration", "Laufzeit");
        this.complexFilterNames.put("added", "Hinzugefuegt");
        this.complexFilterNames.put("lastview", "Gesehen");
        this.directionNames.put("ab", "DESC");
        this.directionNames.put("auf", "ASC");

    }

    private String normalizeValue(String complexFilter, String value) {

        if (complexFilter.equals("size")) {
            value = String.valueOf(Double.valueOf(value)*Math.pow(1024, 3));
        } else if (complexFilter.equals("duration")) {
            value = String.valueOf(Integer.valueOf(value)*60);
        }

        return value;

    }

    public ListView getListView() {

        return this.listView;

    }

    public void updateMovieListView (MovieQueryFactory movieQueryFactory) {

        this.adapter = new MovieArrayAdapter(getActivity(), new ArrayList<MediaObject>(), this.application, this.landscape);
        if (this.previewImdbID != null) {
            this.adapter.previewImdbID = this.previewImdbID;
            this.adapter.previewDimension = this.previewDimension;
        }
        this.listView.setAdapter(this.adapter);
        JSONParser parser = new JSONParser((MainActivity) getActivity(), this.application);
        parser.setArrayAdapter(this.adapter);
        parser.setFragment(this); // ermöglicht Setzen des Hints, falls notwendig

        if (movieQueryFactory == null) {
            parser.execute(new String[]{this.lastURL});
        } else {
            // notwendige Spalten für ListView können zentral hier geändert werden
            movieQueryFactory.addColumns(new String[]{"name", "year", "rating", "imdbID", "3d", "checked", "views", "duration"});
            this.lastURL = movieQueryFactory.getUrl();
            parser.execute(new String[]{this.lastURL});
        }
        getActivity().setTitle("Filme");
    }

    // setze alle Filter auf Default
    private void resetFilter(MainActivity activity) {

        // setze Sortierung zurück
        ((Spinner) activity.findViewById(R.id.movieDirectionSpinner)).setSelection(0);
        ((Spinner) activity.findViewById(R.id.movieCategorySpinner)).setSelection(0);

        // setze Suchfelder komplett zurück
        ((EditText) activity.findViewById(R.id.movieSearchField)).setText(null);
        ((EditText) activity.findViewById(R.id.movieActorSearchField)).setText(null);

        // setze Spinner auf erstes Element
        for (String spinner : this.application.movieFilterSpinner) {

            if (this.application.movieFilterShown.contains(spinner) && this.application.filterSpinner.containsKey(spinner)) {
                this.application.filterSpinner.get(spinner).setSelection(0);
            }
        }

        TextView header;
        LinearLayout content;

        // setze komplexe Filter zurück
        for (String complexFilter : this.application.movieComplexFilter) {

            if (this.application.movieFilterShown.contains(complexFilter) && this.application.absoluteFilters.containsKey(complexFilter)) {

                this.application.absoluteFilters.get(complexFilter).setText(null);
                this.application.lowerBoundFilters.get(complexFilter).setText(null);
                this.application.lowerBoundFilters.get(complexFilter).setText(null);

                content = this.application.filterContents.get(complexFilter);
                header = this.application.filterHeaders.get(complexFilter);

                content.setVisibility(RelativeLayout.INVISIBLE);
                header.setText(((String) header.getText()).replace("[-]", "[+]"));
                content.getLayoutParams().height = 0;
                content.invalidate(); // für Android 5 notwendig
                content.requestLayout(); // für Android 5 notwendig
            }
        }

    }

    public void refreshFilterbar() {

        final Spinner directionSpinner = (Spinner) this.activity.findViewById(R.id.movieDirectionSpinner);
        final Spinner categorySpinner = (Spinner) this.activity.findViewById(R.id.movieCategorySpinner);
        final EditText searchField = (EditText) activity.findViewById(R.id.movieSearchField);
        final EditText actorSearchField = (EditText) activity.findViewById(R.id.movieActorSearchField);

        // Reset Button
        Button resetButton = (Button) activity.findViewById(R.id.movieResetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFilter((MainActivity) getActivity());
            }
        });


        // Filter Funktionalität
        Button sortButton = (Button) activity.findViewById(R.id.movieFilterButton);
        sortButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                // verstecke Hintergrundhinweis
                hideBackgroundHint();

                // sonst würde vielleicht in Film in der Preview angezeigt werden, der gar nicht mehr existiert
                previewImdbID = null;

                // Schließe Tastatur nach Eingabe
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                // MovieQueryFactory für gesamte Filterung und Sortierung
                MovieQueryFactory movieFactory = new MovieQueryFactory(application.preferences);

                // Suchinhalt verarbeiten
                String searchExpression = "";
                try{searchExpression = URLEncoder.encode(searchField.getText().toString(), "utf-8");} catch (Exception e) {}
                if (searchExpression.length() > 0) {
                    movieFactory.addConditions(new String[]{"Suche=" + searchExpression.replace(" ", "%20")});
                }

                // Schauspielersuche verarbeiten
                String actorSearchExpression = "";
                try{actorSearchExpression = URLEncoder.encode(actorSearchField.getText().toString(), "utf-8");} catch (Exception e) {};
                if (actorSearchExpression.length() > 0) {
                    movieFactory.addConditions(new String[]{"Schauspieler=" + actorSearchExpression});
                }

                // Prüfe Sortierung
                String category = (String) categorySpinner.getSelectedItem();
                String direction = (String) directionSpinner.getSelectedItem();
                // ersetzte direction durch richtigen Begriff
                // ersetze Kategorie durch richtigen Spaltennamen und füge zur Factory hinzu
                movieFactory.setOrder(application.orderNames.get(category), directionNames.get(direction));

                // werte Spinner aus
                String content;
                for (String spinner : application.movieFilterSpinner) {

                    if (application.movieFilterShown.contains(spinner)) {

                        content = application.filterSpinner.get(spinner).getSelectedItem().toString();
                        String[] passthroughFilter = new String[]{"acodecger", "acodeceng", "vcodec", "resolution", "channelsger", "channelseng", "hdd"};

                        if (content.equals("3D") && spinner.equals("dimension")) {
                            movieFactory.addConditions(new String[]{"3d=1"});
                        } else if (content.equals("nicht 3D") && spinner.equals("dimension")) {
                            movieFactory.addConditions(new String[]{"3d=0"});
                        } else if (!content.equals("beliebig") && spinner.equals("fsk")) {
                            movieFactory.addConditions(new String[]{"FSK=<" + content});
                        } else if (!content.equals("beliebig") && spinner.equals("Genre")) {
                            movieFactory.addConditions(new String[]{"Genre=" + content});
                        } else if (content.equals("Englisch") && spinner.equals("language")) {
                            movieFactory.addConditions(new String[]{"Englisch=1"});
                        } else if (content.equals("nur Deutsch") && spinner.equals("language")) {
                            movieFactory.addConditions(new String[]{"Englisch=0"});
                            // passthrough Filter
                        } else if (!content.equals("beliebig") && Arrays.asList(passthroughFilter).contains(spinner)) {
                            movieFactory.addConditions(new String[]{spinner + "=" + content});
                        } else if (content.equals("fehlerfrei") && spinner.equals("checked")) {
                            movieFactory.addConditions(new String[]{spinner + "=1"});
                        } else if (content.equals("fehlerhaft") && spinner.equals("checked")) {
                            movieFactory.addConditions(new String[]{spinner + "=0"});
                        } else if (content.equals("nicht geprüft") && spinner.equals("checked")) {
                            movieFactory.addConditions(new String[]{spinner + "=NULL"});
                        } else if (content.equals("ja") && spinner.equals("views")) {
                            movieFactory.addConditions(new String[]{"Gesehenzaehler=>1"});
                        } else if (content.equals("nein") && spinner.equals("views")) {
                            movieFactory.addConditions(new String[]{"Gesehenzaehler=0"});
                        } else if (content.equals("vorhanden") && spinner.equals("youtube")) {
                            movieFactory.addConditions(new String[]{"Youtube=1"});
                        } else if (content.equals("deutsch") && spinner.equals("youtube")) {
                            movieFactory.addConditions(new String[]{"Youtube=DE"});
                        } else if (content.equals("englisch") && spinner.equals("youtube")) {
                            movieFactory.addConditions(new String[]{"Youtube=EN"});
                        } else if (content.equals("keiner") && spinner.equals("youtube")) {
                            movieFactory.addConditions(new String[]{"Youtube=0"});
                        } else {
                            Log.d("DEBUG", "MovieListFragment: ignoring value " + spinner + " with content " + content);
                        }
                    }
                }


                // Prüfe komplexe Filter

                String absoluteFilter;
                String lowerBoundFilter;
                String upperBoundFilter;

                for (String complexFilter : application.movieComplexFilter) {

                    if (application.movieFilterShown.contains(complexFilter)) {

                        absoluteFilter = application.absoluteFilters.get(complexFilter).getText().toString();
                        lowerBoundFilter = application.lowerBoundFilters.get(complexFilter).getText().toString();
                        upperBoundFilter = application.upperBoundFilters.get(complexFilter).getText().toString();

                        if (absoluteFilter.length() > 0) {
                            movieFactory.addConditions(new String[]{complexFilterNames.get(complexFilter) + "=" + normalizeValue(complexFilter, absoluteFilter)});
                        } else if (lowerBoundFilter.length() > 0 && upperBoundFilter.length() == 0) {
                            movieFactory.addConditions(new String[]{complexFilterNames.get(complexFilter) + "=>" + normalizeValue(complexFilter, lowerBoundFilter)});
                        } else if (lowerBoundFilter.length() == 0 && upperBoundFilter.length() > 0) {
                            movieFactory.addConditions(new String[]{complexFilterNames.get(complexFilter) + "=<" + normalizeValue(complexFilter, upperBoundFilter)});
                        } else if (lowerBoundFilter.length() > 0 && upperBoundFilter.length() > 0) {
                            movieFactory.addConditions(new String[]{complexFilterNames.get(complexFilter) + "=" + normalizeValue(complexFilter, lowerBoundFilter) + "," + normalizeValue(complexFilter, upperBoundFilter)});
                        }

                    }

                }

                // Aktualisiere ListView
                updateMovieListView(movieFactory);
                ((MainActivity) getActivity()).closeMovieFilter(); // schließe Filter Drawer

            }
        });

    }

    public void initializeFragment(ListView movieListView, Bundle savedInstanceState) {

        // setURL wurde aufgerufen (Schauspieler-Filme) oder onSaveInstance
        if (this.lastURL != null) {

            this.refreshFilterbar();
            this.updateMovieListView(null);

        // ListView wurde erstellt, ohne Angabe eines Links oder vorher gesichert worden zu sein
        } else if (savedInstanceState == null) {
            MovieQueryFactory factory = new MovieQueryFactory(this.application.preferences);
            factory.setOrder("name", "ASC");
            this.updateMovieListView(factory);
            this.refreshFilterbar();
            this.resetFilter(this.activity);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // speicher Filmliste, die momentan angezeigt wird
        outState.putString("lastURL", this.lastURL);
        this.application.saveFilterSettings();
        // speicher bei Drehung den Film, der in der Preview ist
        if (this.previewImdbID != null) {
            outState.putString("previewImdbID", this.previewImdbID);
            outState.putInt("previewDimension", this.previewDimension);
        } else if (this.adapter != null) {
            outState.putString("previewImdbID", this.adapter.previewImdbID);
            outState.putInt("previewDimension", this.adapter.previewDimension);
        }
        outState.putBoolean("hideFilter", this.hideFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // sonst springt listView jedes mal auf default zurück, nachdem ein Film ausgewählt wurde
        // verhindet, dass Filtereinstellungen im Drawer verloren gehen
        if (!initialized) {

            if (this.activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                this.fragment = inflater.inflate(R.layout.movie_list_fragment_landscape, container, false); //wendet layout an
                //MovieDetailedFragment movieDetailedFragment = new MovieDetailedFragment();
                this.landscape = true;
            } else {
                this.fragment = inflater.inflate(R.layout.list_fragment, container, false); //wendet layout an
            }
            this.fragment.setBackgroundColor(getResources().getColor(R.color.background));

            this.listView = (ListView) fragment.findViewById(android.R.id.list); //hole Referenz auf ListView
            this.initializeHashMaps();
            this.initializeFragment(this.listView, savedInstanceState);
            this.initialized = true;
        }

        return this.fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.lastURL = savedInstanceState.getString("lastURL");
            this.previewImdbID = savedInstanceState.getString("previewImdbID", null);
            this.previewDimension = savedInstanceState.getInt("previewDimension", 3);
            this.hideFilter = savedInstanceState.getBoolean("hideFilter");
        }
    }

    @Override
    public void onPause() {
        //this.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        if (!this.hideFilter) {
            this.activity.setMovieFilterInvisible(); // deaktiviere Filter Drawer
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        //this.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (!this.hideFilter) {
            this.activity.setMovieFilterVisible(); // aktiviere Filter Drawer
        }
        if (this.adapter != null) {
            this.activity.setTitle("Filme (" + this.adapter.getCount() + ")");
        } else {
            this.activity.setTitle("Filme");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (MainActivity) getActivity();
        this.application = (MyApplication) getActivity().getApplication();
    }
}
