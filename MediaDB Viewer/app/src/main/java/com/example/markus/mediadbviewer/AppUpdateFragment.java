package com.example.markus.mediadbviewer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AppUpdateFragment extends DialogFragment {

    private Bundle arguments;

    @Override
    public void setArguments(Bundle args) { //recentVersion, appLink, changeLog müssen übergeben werden
        this.arguments = args;
        super.setArguments(args);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("appLink", arguments.getString("appLink"));
        outState.putString("recentVersion", arguments.getString("recentVersion"));
        outState.putString("changeLog", arguments.getString("changeLog"));
        super.onSaveInstanceState(outState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            this.arguments = savedInstanceState;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        LinearLayout dialogLayout = (LinearLayout) inflater.inflate(R.layout.app_update_layout, null);

        TextView versionView = (TextView) dialogLayout.findViewById(R.id.appUpdateVersionText);
        TextView changelogView = (TextView) dialogLayout.findViewById(R.id.appUpdateChagelog);

        // setze Texte
        versionView.setText(getResources().getString(R.string.appUpdateVersionText).replace("CURRENT", getResources().getString(R.string.app_version)).replace("RECENT", this.arguments.getString("recentVersion")));
        changelogView.setText(this.arguments.getString("changeLog"));

        return builder.setView(dialogLayout)
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        dismiss();

                    }
                })
                .setPositiveButton("Herunterladen", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(arguments.getString("appLink")));
                        startActivity(browserIntent);
                        dismiss();

                    }
                }).create();

    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onStart();
        /*
        // scrollt Scroll View ans Ende um aktuellste Änderungen anzuzeigen
        final ScrollView scrollView = (ScrollView) this.getDialog().findViewById(R.id.appUpdateScrollView);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        */

    }
}