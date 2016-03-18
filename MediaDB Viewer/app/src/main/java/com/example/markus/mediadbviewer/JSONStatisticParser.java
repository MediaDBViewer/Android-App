package com.example.markus.mediadbviewer;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

class JSONStatisticParser extends AsyncTask<String, String, String> {

    private String url;
    private StatisticFragment fragment;
    private String statistic;
    private ArrayList<LinkedHashMap<String, String>> statisticList;
    private MainActivity activity;

    public JSONStatisticParser(MainActivity activity) {

        this.activity = activity;

    }

    public ArrayList<LinkedHashMap<String, String>> getStatistic() {

        // jedes Element der Liste ist eine Zeile der Statistic
        return this.statisticList;

    }

    public void setFragment (StatisticFragment fragment) {

        this.fragment = fragment;

    }

    public void setStatistic (String statistic) {

        this.statistic = statistic;

    }

    @Override
    protected String doInBackground(String[] links) {

        try {

            this.url = links[0];
            URL url = new URL(links[0]);
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            InputStreamReader isReader = new InputStreamReader(stream);
            JsonReader jsonReader = new JsonReader(isReader);
            this.statisticList = readJsonStream(stream);
            if (this.fragment != null) {
                this.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            fragment.addStatistic(statisticList, statistic);
                        } catch (Exception e) {
                            Log.d("Exception", "JSONStatisticParser: " + e.getClass().getName());
                            // tritt auf, wenn Handy während laden der Daten gedreht wird
                        }
                    }
                });
            }

        } catch (Exception e) {

            Log.d("Exceptions", e.getClass().getName());

        }

        return null;
    }

    public ArrayList<LinkedHashMap<String, String>> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        ArrayList<LinkedHashMap<String, String>> statisticList = new ArrayList();
        try {
            reader.beginObject(); //startet das root Object, welches mit { beginnt
            for (int i=0; i<3; i++) { //überspringt die ersten 4 Attribute, da uninteressant
                reader.nextName(); // nextName muss aufgerufen werden !!!
                reader.skipValue();
            }
            if (reader.nextName().equals("Antwort")) {
                reader.beginObject();
                if (reader.nextName().equals("Spalten")) {
                    reader.skipValue();
                }
            }
            statisticList = readMessagesArray(reader);
        } catch (Exception e) {
            Log.d("JSONFehler", e.getMessage() + " : " + this.statistic);
        } finally {
            reader.close();
        }
        return statisticList;
    }

    public ArrayList<LinkedHashMap<String, String>> readMessagesArray(JsonReader reader) throws IOException {
        ArrayList<LinkedHashMap<String, String>> statistic = new ArrayList();
        if (reader.nextName().equals("Data")) {
        }
        reader.beginArray();
        while (reader.hasNext()) {
            statistic.add(readMessage(reader));
        }
        reader.endArray();
        reader.endObject();
        reader.endObject();
        return statistic;
    }

    public LinkedHashMap<String, String> readMessage(JsonReader reader) throws IOException {
        LinkedHashMap<String, String> statisticRow = new LinkedHashMap<>();

        reader.beginObject();
        while (reader.hasNext()) {

            String key = reader.nextName();
            String value;
            if (reader.peek() == JsonToken.NULL) {
                value = "";
                reader.skipValue();
            } else {
                value = reader.nextString();
            }
            statisticRow.put(key, value);

        }



        reader.endObject();
        return statisticRow;
    }



}
