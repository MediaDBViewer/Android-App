package com.example.markus.mediadbviewer;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.content.DialogInterface;
import android.graphics.Movie;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;

public class UpdateDialogFragment extends DialogFragment{

    private MediaObject media;
    private MyApplication application;

    public void setMediaObject(MediaObject media) {

        this.media = media;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        LinearLayout dialogLayout = (LinearLayout) inflater.inflate(R.layout.update_layout, null);

        final CheckBox viewedBox = (CheckBox) dialogLayout.findViewById(R.id.inkrementCheckbox);
        final RadioButton failedButton = (RadioButton) dialogLayout.findViewById(R.id.checkedRadioFailedButton);
        final RadioButton successButton = (RadioButton) dialogLayout.findViewById(R.id.checkedRadioSuccessButton);
        final EditText commentField = (EditText) dialogLayout.findViewById(R.id.commentField);

        // setze default values
        if (this.media.simpleValues.keySet().contains("checked")) {
            if (this.media.simpleValues.get("checked").equals("0")) {
                failedButton.setChecked(true);
            } else if (this.media.simpleValues.get("checked").equals("1")) {
                successButton.setChecked(true);
            }
        }
        if (this.media.simpleValues.keySet().contains("commentEpisode")) {
            commentField.setText(this.media.simpleValues.get("commentEpisode"));
        } else if (this.media.simpleValues.keySet().contains("comment")) {
            commentField.setText(this.media.simpleValues.get("comment"));
        }

        return builder.setView(dialogLayout)
                .setNegativeButton(getResources().getString(R.string.updateDialogAbortButton), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        dismiss();

                    }
                })
                .setPositiveButton(getResources().getString(R.string.updateDialogStartButton), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        // hole gesetzte Werte
                        String comment = "";
                        try {comment = URLEncoder.encode(commentField.getText().toString(), "utf-8");} catch (Exception e) {}
                        String checked = "";
                        String viewed = "";
                        if (successButton.isChecked()) {
                            checked = "1";
                        } else if (failedButton.isChecked()) {
                            checked = "0";
                        }
                        if (viewedBox.isChecked()) {
                            viewed = "1";
                        }

                        // führe Update der Tabelle aus
                        TableUpdater updater = new TableUpdater(application.preferences);
                        if (media.simpleValues.keySet().contains("imdbID")) {
                            updater.setTable("Filme");
                            updater.setConditions(new String[]{"imdbID=" + media.simpleValues.get("imdbID"), "3d=" + media.simpleValues.get("3d")});
                        } else if (media.simpleValues.keySet().contains("episodenumber")) {
                            updater.setTable("Episoden");
                            updater.setConditions(new String[]{"season_nr=" + media.simpleValues.get("season_nr"), "episodenumber=" + media.simpleValues.get("episodenumber")});
                        }

                        updater.setValues(viewed, checked, comment);
                        updater.execute();

                        FragmentManager fragmentManager = ((MainActivity) getActivity()).getSupportFragmentManager();


                        // updated alle parents
                        if (media.simpleValues.keySet().contains("imdbID")) {

                            // erstelle MovieDetailedFragment neu
                            fragmentManager.popBackStack();
                            MovieDetailedFragment movieDetailedFragment = new MovieDetailedFragment();
                            if (media.simpleValues.get("3d").length() == 0) {
                                movieDetailedFragment.setDimensions(0);
                            } else {
                                movieDetailedFragment.setDimensions(1);
                            }
                            movieDetailedFragment.setImdbID(media.simpleValues.get("imdbID"));
                            movieDetailedFragment.setForceReload();
                            fragmentManager.beginTransaction().replace(R.id.mainContent, movieDetailedFragment).addToBackStack("moviedDetailedFragment").commit();

                            // update movieListFragment
                            /*
                            MovieListFragment movieListFragment = (MovieListFragment) fragmentManager.findFragmentByTag("movieListFragment");
                            ArrayList<MediaObject> movieList = ((MovieArrayAdapter) movieListFragment.getListView().getAdapter()).getMovieList();
                            for (MediaObject movie : movieList) {
                                if (movie.simpleValues.get("imdbID").equals(media.simpleValues.get("imdbID"))) {
                                    movie.simpleValues.put("comment", comment);
                                    movie.simpleValues.put("checked", checked);
                                    movie.simpleValues.put("views", String.valueOf(Integer.valueOf(movie.simpleValues.get("views")) + 1));
                                    break;
                                }

                            }
                            */

                        } else if (media.simpleValues.keySet().contains("episodenumber")) {

                            // erstelle Episode detailed Fragment neu
                            fragmentManager.popBackStack();
                            EpisodeDetailedFragment episodeDetailedFragment = new EpisodeDetailedFragment();
                            episodeDetailedFragment.setSeason_nr(media.simpleValues.get("season_nr"));
                            episodeDetailedFragment.setEpisodenumber(Integer.valueOf(media.simpleValues.get("episodenumber")));
                            episodeDetailedFragment.setForceReload();
                            fragmentManager.beginTransaction().replace(R.id.mainContent, episodeDetailedFragment).addToBackStack("episodeDetailedFragment").commit();

                            // update aller ListFragmente
                            ((EpisodeListFragment) fragmentManager.findFragmentByTag("episodeListFragment")).setContentChanged();
                            ((SeasonListFragment) fragmentManager.findFragmentByTag("seasonListFragment")).setContentChanged();
                            ((SeriesListFragment) fragmentManager.findFragmentByTag("seriesListFragment")).setContentChanged();

                        }

                        dismiss();

                    }
                }).create();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.application = (MyApplication) getActivity().getApplication();
    }
}
