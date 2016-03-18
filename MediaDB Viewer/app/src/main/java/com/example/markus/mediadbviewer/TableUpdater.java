package com.example.markus.mediadbviewer;


import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

public class TableUpdater extends AsyncTask<String, String, String> {

    private String rootURL;
    private String table = "";
    private String conditions = "";
    private StringBuffer response = new StringBuffer();
    private String checked = null;
    private String viewed = null;
    private String comment = null;

    private String[] validTables = new String[]{"Filme", "Episoden", "Staffeln"};

    public TableUpdater(SharedPreferences preferences) {

        this.rootURL = preferences.getString("server", "") + "api.php?key=" + preferences.getString("apikey", "") + "&action=SetData";

    }

    public String getResponse() {

        return this.response.toString();

    }

    public void setTable(String table) {

        if (Arrays.asList(this.validTables).contains(table)) {

            this.table = "&Tabelle=" + table;

        }
    }

    public void setConditions(String[] conditions) {

        for (String condition : conditions) {
            if (this.conditions.length() == 0) {
                this.conditions += condition;
            } else {
                this.conditions += "&" + condition;
            }
        }

    }

    public void setValues(String viewed, String checked, String comment) {

        this.comment = comment;
        this.viewed = viewed;
        this.checked = checked;

    }

    @Override
    protected String doInBackground(String[] links) {

        if (this.conditions.length() > 0 && this.table.length() > 0) {

            try {
                URL url = new URL(this.rootURL + this.table);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                //connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                Log.d("INFO", this.rootURL + this.table);

                // bastel Bedingungen zusammen
                if (this.viewed.equals("1")) {
                    this.conditions += "&Gesehen=1";
                }
                if (this.checked.equals("0") || this.checked.equals("1")) {
                    this.conditions += "&checked=" + this.checked;
                }
                if (this.comment.length() > 0) {
                    this.conditions += "&comment=" + this.comment.replace(" ", "%20");
                }

                Log.d("INFO", this.conditions);

                // Send post request
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(this.conditions);
                out.flush();
                out.close();

                //int responseCode = connection.getResponseCode();

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

            } catch (Exception e) {
                Log.d("Exception", e.getMessage());
            }

        }

        Log.d("INFO", this.response.toString());
        return this.response.toString();

    }
}
