package com.example.markus.mediadbviewer;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.lang.Math;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


public class EpisodeDetailedFragment extends DetailedFragment {

    private int episodenumber;
    private String season_nr;
    private String title;
    private MainActivity activity;
    private JSONParser episodeParser;
    private LayoutInflater inflater;
    private MyApplication application;
    private boolean forceReload = false;

    // beschreiben aus welcher Tabelle welche Werte geholt werden müssen
    // episodenumber und season_nr sind notwendig, um bei Update die Episode identifizieren zu können
    private String[] valuesEpisode = new String[]{"episodenumber", "season_nr", "name", "size", "duration", "width", "height", "vcodec", "totalbitrate",
            "acodecger", "abitrateger", "channelsger", "acodeceng", "abitrateeng", "channelseng", "hdd", "checked", "comment", "added", "md5", "views", "lastView"};
    private String[] valuesSeason = new String[]{"comment", "source"};
    // diese Werte werden in dieser Reihenfolge in die Tabelle eingetragen (müssen alle in den vorherigen Arrays existieren!!!)
    // wenn comment bei valuesShown angegeben wird, muss es sowohl in valuesEpisode, als auch in valuesSeason angegeben werden!

    public void setEpisodenumber (int episodenumber) {

        this.episodenumber = episodenumber;

    }

    public void setSeason_nr (String season_nr) {

        this.season_nr = season_nr;

    }

    // JSONParser wird dann so aufgerufen, dass kein Cache genutzt wird
    public void setForceReload() {

        this.forceReload = true;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("season_nr", this.season_nr);
        outState.putInt("episodenumber", this.episodenumber);
    }

    @Override
    public void updateData() {

        final MediaObject episode = this.episodeParser.getMediaList().get(0);

        // setze globale Titel-Variable
        this.title = episode.simpleValues.get("name");
        this.activity.setTitle(this.title);

        // entferne Cover vom Layout
        ImageView cover = (ImageView) this.fragment.findViewById(R.id.detailedCover);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        if (new File(this.application.coverFolderSeasonHigh + this.season_nr + ".jpg").exists()) {
            BitmapFactory.decodeFile(this.application.coverFolderSeasonHigh + this.season_nr + ".jpg", options);
            cover.setImageDrawable(Drawable.createFromPath(this.application.coverFolderSeasonHigh + this.season_nr + ".jpg"));
        } else {
            BitmapFactory.decodeResource(getResources(), R.drawable.default_cover, options);
            cover.setImageResource(R.drawable.default_cover);
        }
        cover.getLayoutParams().width = (int) (cover.getHeight() / (options.outHeight / (double) options.outWidth));

        // erstelle Tabelle / Titel
        TableLayout table = (TableLayout) this.fragment.findViewById(R.id.movieTable); // hole Tabellenreferenz
        TableRow row;
        TextView firstColumn;
        TextView secondColumn;

        Iterator<String> iterator = this.application.valuesEpisodeShown.iterator();
        String value;
        boolean rightsChanged = false;
        while (iterator.hasNext()) {
            value = iterator.next();
            if (!episode.simpleValues.keySet().contains(value)) {
                rightsChanged = true;
                iterator.remove();
            } else {
                row = (TableRow) LayoutInflater.from(this.fragment.getContext()).inflate(R.layout.table_row_text, null); // erstelle neue Zeile
                // wenn Spalte unbekannt, wird Name aus der Datenbank genommen
                if (this.application.detailedListNames.keySet().contains(value)) {
                    ((TextView) row.findViewById(R.id.firstColumn)).setText(this.application.detailedListNames.get(value));
                } else {
                    ((TextView) row.findViewById(R.id.firstColumn)).setText(value);
                }
                secondColumn = (TextView) this.inflater.inflate(R.layout.table_row_textview, null);
                row.addView(secondColumn);
                if (!value.equals("name")) {
                    table.addView(row);
                }

                if (value.equals("name")) {
                    TextView titel = (TextView) this.fragment.findViewById(R.id.movieTitleDetailed);
                    titel.setText(episode.simpleValues.get("name"));
                } else if (value.equals("size")) {
                    secondColumn.setText(String.valueOf(Math.round(Long.valueOf(episode.simpleValues.get(value)) * 100 / Math.pow(1024, 3)) / 100.) + " GB");
                } else if (value.equals("duration")) {
                    secondColumn.setText(String.valueOf(Integer.valueOf(episode.simpleValues.get(value)) / 60) + " Minuten");
                } else if (value.equals("width") || value.equals("height")) {
                    secondColumn.setText(episode.simpleValues.get(value) + " Pixel");
                } else if (value.contains("bitrate")) {
                    // verhindert, dass Einheit angezeigt wird, wenn keine Tonspur existiert
                    if (episode.simpleValues.get(value).length() > 0) {
                        secondColumn.setText(episode.simpleValues.get(value) + " kBit/s");
                    } else {
                        secondColumn.setText("");
                    }
                } else if (value.equals("checked")) {
                    if (episode.simpleValues.get(value).length() == 0) {
                        secondColumn.setText("nicht geprüft");
                    } else if (episode.simpleValues.get(value).equals("0")) {
                        secondColumn.setText("fehlerhaft");
                        //secondColumn.setTextColor(ContextCompat.getColor(getActivity(), R.color.red));
                        secondColumn.setTextColor(getResources().getColor(R.color.red));
                    } else if (episode.simpleValues.get(value).equals("1")) {
                        secondColumn.setText("fehlerfrei");
                        //secondColumn.setTextColor(ContextCompat.getColor(getActivity(), R.color.green));
                        secondColumn.setTextColor(getResources().getColor(R.color.green));
                    }
                } else if (value.equals("comment")) {
                    // erstelle Kommentar für Episode
                    secondColumn.setText(episode.simpleValues.get(value + "Episode"));
                    // erstelle Kommentar für Staffel
                    TableRow row2 = (TableRow) LayoutInflater.from(this.fragment.getContext()).inflate(R.layout.table_row_text, null); // erstelle neue Zeile
                    firstColumn = (TextView) row2.findViewById(R.id.firstColumn);
                    secondColumn = (TextView) this.inflater.inflate(R.layout.table_row_textview, null);
                    row2.addView(secondColumn);
                    firstColumn.setText("Kommentar Staffel");
                    secondColumn.setText(episode.simpleValues.get(value));
                    table.addView(row2);
                } else {
                    secondColumn.setText(episode.simpleValues.get(value));
                }
            }
        }

        if (rightsChanged) {
            // speichere neue verfügbare Spalten
            this.application.saveArrayList("valuesEpisodeShown", new ArrayList<String>(episode.simpleValues.keySet()));
        }


        // setze Button-Funktion
        Button openUpdateButton = (Button) this.fragment.findViewById(R.id.detailedFragmentUpdateButton);

        if (this.application.preferences.getBoolean("updateRight", false)) {
            openUpdateButton.setVisibility(Button.VISIBLE);
            openUpdateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    UpdateDialogFragment updateFragment = new UpdateDialogFragment();
                    updateFragment.setMediaObject(episode);
                    updateFragment.show(getFragmentManager(), "updateDialogFragment");

                }

            });
        } else {
            ((ViewGroup) openUpdateButton.getParent()).removeView(openUpdateButton);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            this.episodenumber = savedInstanceState.getInt("episodenumber");
            this.season_nr = savedInstanceState.getString("season_nr");
        }

        if (this.fragment == null) {
            this.inflater = inflater;
            this.activity = (MainActivity) getActivity();

            // lade entsprechendes Layout
            if (this.activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                this.fragment = inflater.inflate(R.layout.detailed_fragment_landscape, container, false);
            } else {
                this.fragment = inflater.inflate(R.layout.detailed_fragment, container, false); //wendet layout an
            }

            this.fragment.setBackgroundColor(getResources().getColor(R.color.background));

            // hole Informationen aus Staffel-Tabelle
            SeriesQueryFactory seasonQueryFactory = new SeriesQueryFactory("Staffeln", (this.application.preferences));
            seasonQueryFactory.addColumns(valuesSeason);
            seasonQueryFactory.addConditions(new String[]{"season_nr=" + this.season_nr});
            SeriesQueryFactory episodeQueryFactory = new SeriesQueryFactory("Episoden", (this.application.preferences));
            episodeQueryFactory.addColumns(valuesEpisode);
            episodeQueryFactory.addConditions(new String[]{"season_nr=" + this.season_nr, "episodenumber=" + this.episodenumber});
            this.episodeParser = new JSONParser(this.activity, this.application);
            this.episodeParser.setFragment(this);
            if (this.forceReload) {
                this.episodeParser.setForcReload();
            }
            this.episodeParser.execute(new String[]{episodeQueryFactory.getUrl(), seasonQueryFactory.getUrl()});

        }

        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(this.title);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.application = (MyApplication) getActivity().getApplication();
    }
}
