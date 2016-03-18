package com.example.markus.mediadbviewer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class StatisticFragment extends Fragment {

    private View fragment = null;
    private boolean initialized = false;
    private LayoutInflater inflater;
    private MainActivity activity;
    private LinearLayout rootLayout;
    private ProgressDialog progress;
    private MyApplication application;

    // gibt true zurück, wenn eine Netzwerkverbindung besteht
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void addStatistic(ArrayList<LinkedHashMap<String, String>> statisticList, String statistic) {

        // erstelle Segment des Fragments
        LinearLayout statisticLayout = (LinearLayout) inflater.inflate(R.layout.statistic_layout, null);
        TextView header = (TextView) statisticLayout.findViewById(R.id.statisticHeader);
        if (this.application.statisticNames.keySet().contains(statistic)) {
            header.setText("[+] " + this.application.statisticNames.get(statistic));
        } else {
            header.setText("[+] " + statistic);
        }
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayout parent = (LinearLayout) v.getParent();
                LinearLayout content = (LinearLayout) parent.findViewById(R.id.statisticContent);
                TextView header = (TextView) parent.findViewById(R.id.statisticHeader);

                if (content.getVisibility() == LinearLayout.INVISIBLE) {
                    header.setText(((String) header.getText()).replace("[+]", "[-]"));
                    content.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    content.setVisibility(LinearLayout.VISIBLE);
                    content.invalidate(); // für Android 5 notwendig
                    content.requestLayout(); // für Android 5 notwendig
                } else {
                    content.setVisibility(LinearLayout.INVISIBLE);
                    header.setText(((String) header.getText()).replace("[-]", "[+]"));
                    content.getLayoutParams().height = 0;
                    content.invalidate(); // für Android 5 notwendig
                    content.requestLayout(); // für Android 5 notwendig
                }

            }
        });

        // erstelle Tabelle aus StatistikObjektListe
        TableLayout statisticHeaderTable = (TableLayout) statisticLayout.findViewById(R.id.statisticHeaderTable);
        TableLayout statisticContentTable = (TableLayout) statisticLayout.findViewById(R.id.statisticContentTable);
        TableRow headerRow = null;
        TableRow contentRow = null;
        // iteriere über alle Zeilen, also Attribute
        if (statisticList.size() > 0) { // verhindert Absturz, wenn View nicht existiert

            for (String key : statisticList.get(0).keySet()) {
                headerRow = (TableRow) inflater.inflate(R.layout.table_row_text, null);
                ((TextView) headerRow.findViewById(R.id.firstColumn)).setText(key);
                statisticHeaderTable.addView(headerRow);
                // erstelle alle Spalten einer bestimmten Zeile
                contentRow = new TableRow(getActivity());
                for (int i = statisticList.size() - 1; i >= 0; i--) {
                    TextView column = (TextView) inflater.inflate(R.layout.table_row_textview, null);
                    column.setText(statisticList.get(i).get(key));
                    contentRow.addView(column);
                }
                statisticContentTable.addView(contentRow);
            }

            // entferne Hintergrundinfo, sobald erste Statistik zum Fragment hinzugefügt wird
            ((TextView) this.fragment.findViewById(R.id.statisticFragmentInfoText)).setVisibility(TextView.GONE);

            rootLayout.addView(statisticLayout);
        } else {
            Log.d("DEBUG", "StatisticFragment: size of statistic is 0: " + statistic);
        }

        // verstecke progressDialog, sobald erste Statistic geladen wurde
        if (this.progress.isShowing()) {
            this.progress.dismiss();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (!this.initialized) {

            this.inflater = inflater;
            this.fragment = inflater.inflate(R.layout.statistic_fragment, container, false);
            this.fragment.setBackgroundColor(getResources().getColor(R.color.background));
            this.activity = ((MainActivity) getActivity());
            this.progress = ProgressDialog.show(this.activity, "", "Lade Daten ...", true, false);
            this.rootLayout = (LinearLayout) fragment.findViewById(R.id.statisticRootLayout);
            StatisticQueryFactory statisticQueryFactory = new StatisticQueryFactory(this.application.preferences);

            JSONStatisticParser parser;
            TextView backgroundHint = (TextView) fragment.findViewById(R.id.statisticFragmentInfoText);

            // es sind keine Statistiken freigegeben oder Server ist nicht erreichbar
            URLChecker checker = new URLChecker();
            try{checker.execute(new String[]{this.application.preferences.getString("server", "") + "app/Version.txt"}).get();} catch (Exception e) {}

            if ((this.application.availableStatistics.size() == 0) || (!checker.getResult())) {
                this.progress.dismiss();

            // es besteht keine Internetverbindung
            } else if (!this.isNetworkAvailable()) {
                this.progress.dismiss();
                // zeige Info im Hintergrund des Fragments an
                backgroundHint.setText(R.string.hintNoNetworkConnection);

            // Statistiken können normal geparst werden
            } else {
                for (String statistic : this.application.availableStatistics) {


                    // hole Statistik
                    statisticQueryFactory.setStatistic(statistic);
                    parser = new JSONStatisticParser(this.activity);
                    parser.setFragment(this);
                    parser.setStatistic(statistic);
                    parser.execute(statisticQueryFactory.getUrl());
                }
            }

            this.initialized = true;

        }

        return this.fragment;

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Statistik");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.application = (MyApplication) getActivity().getApplication();
    }
}
