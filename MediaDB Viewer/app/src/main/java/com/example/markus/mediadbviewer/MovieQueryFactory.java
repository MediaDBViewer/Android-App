package com.example.markus.mediadbviewer;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.Arrays;

public class MovieQueryFactory {

    private String url=""; // finale URL

    //private String[] validColumns = new String[]{"imdbID", "name", "year", "size", "md5", "3d", "vcodec", "duration", "totalbitrate", "resolution",
    //        "width", "height", "acodecger", "abitrateger", "channelsger", "acodeceng", "abitrateeng", "channelseng", "hdd", "rating",
    //        "checked", "views", "comment", "added", "lastView", "Genre", "Schauspieler", "summary", "youtube", "fsk"};
    //private String[] validConditions = new String[]{"Anzahl", "imdbID", "Suche", "3d", "Genre", "Schauspieler"};
    //private String[] validGenres = new String[]{"Action", "Adventure", "Animation", "Biography", "Comedy", "Crime", "Documentary", "Drama", "Family",
    //        "Fantasy", "History", "Horror", "Music", "Musical", "Mystery", "Romance", "Sci-Fi", "Short", "Sport", "Thriller", "War", "Western"};
    private String[] validDirections = new String[]{"ASC", "DESC", "asc", "desc", "Asc", "Desc"};
    private String key = null;
    private String server = null;
    private String columns = "";
    private String conditions = "";
    private String order = "";
    private SharedPreferences preferences;
    private boolean build = false;

    public MovieQueryFactory(SharedPreferences preferences) {

        this.preferences = preferences;
        this.url += preferences.getString("server", "") + "api.php";
        this.url += "?key=";
        this.url += preferences.getString("apikey", "");
        this.url += "&action=GetDataList";
        this.url += "&Tabelle=Filme";

    }

    public void setKey (String key) {

        this.key = key;

    }

    public void setServer (String server) {

        this.server = server;

    }

    // prüft Spalten und füge sie hinzu
    public void addColumns(String[] columns) {

        for (String column : columns) {

            // auf Gültigkeit der Spalten kann nicht mehr geprüft werden, da sonst keine unbekannten Spalten möglich wären

            //if (Arrays.asList(this.validColumns).contains(column)) {
            if (this.columns.length() == 0) {
                this.columns += column;
            } else {
                this.columns += ",";
                this.columns += column;
            }
            //} else {
            //    Log.d("DEBUG", "MovieQueryFactory: unknown column " + column);
            //}
        }

    }

    // füge Bedingungen zu Abfrage hinzu
    // werden wie folgt angegeben: imdbID=1234567
    public void addConditions(String[] conditions) {


        for (String condition : conditions) {
            /*
            if (Arrays.asList(this.validConditions).contains(condition.split("=")[0])) {
                if (condition.split("=").length == 2) {
                    if (condition.split("=")[0]=="3d") {
                        if ((Integer.valueOf(condition.split("=")[1]) != 1) && (Integer.valueOf(condition.split("=")[1]) != 1)) {
                            Log.d("DEBUG", "MovieQueryFactory: wrong value " + condition.split("=")[1] + "for 3d filter");
                        } else {
                            this.conditions += "&" + condition;
                        }
                    } else {
                        this.conditions += "&" + condition;
                    }
                } else {
                    Log.d("DEBUG", "MovieQueryFactory: no value assigned to condition " + condition);
                }
            } else {
                Log.d("DEBUG", "MovieQueryFactory: unknown condition " + condition);
            }
            */
            this.conditions += "&" + condition;
        }

    }

    // füge Sortierung zur Abfrage hinzu
    public void setOrder(String column, String direction) {

        if (this.order.length() == 0) {
            //if (Arrays.asList(this.validColumns).contains(column)) {
                if (Arrays.asList(this.validDirections).contains(direction)) {
                    this.order += "&Sortierung=" + column + "%20" + direction;
                } else {
                    Log.d("DEBUG", "MovieQueryFactory: invalid direction '" + direction + "'for ordering ");
                }
            //} else {
            //    Log.d("DEBUG", "MovieQueryFactory: invalid column " + column + " for ordering");
            //}
        } else {
            Log.d("DEBUG", "MovieQueryFactory: order clause already set - ignoring " + column + " " + direction);
        }
    }

    // erstelle URL
    public String getUrl () {

        if (!this.build) {

            // füge Spalten zur URL hinzu
            if (this.columns.length() != 0) {
                this.url += "&Spalten=";
                this.url += this.columns;
            }

            // füge Bedingungen zur URL hinzu
            if (this.conditions.length() != 0) {
                this.url += this.conditions;
            }

            // füge Sortierung zur URL hinzu
            if (this.order.length() != 0) {
                this.url += this.order;
            }

            Log.d("INFO", "MovieQueryFactory: build URL " + url);
            this.build = true;

        }

        return this.url;

    }

    public String getRightsURL() {

        String url = "";
        if (this.server != null) {
            url += this.server;
        } else {
            url +=this.preferences.getString("server", "");
        }
        url += "api.php?key=";
        if (this.key != null) {
            url += this.key;
        } else {
            url += preferences.getString("apikey", "");
        }
        url += "&action=GetKeyRights";



        Log.d("INFO", "MovieQueryFactory: build URL: " + url);
        return  url;

    }

}
