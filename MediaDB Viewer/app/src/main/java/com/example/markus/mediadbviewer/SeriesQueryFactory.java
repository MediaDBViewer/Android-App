package com.example.markus.mediadbviewer;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.Arrays;

public class SeriesQueryFactory {

    private String url=""; // finale URL
    private String table = "";
    private String columns = "";
    private String conditions = "";
    private String order = "";
    private String[] validTables = new String[]{"Serien", "Staffeln", "Episoden"};
    //private String[] validColumnsSerien = new String[]{"name", "finished", "series_nr", "Statistik"};
    //private String[] validColumnsStaffeln = new String[]{"season", "resolution", "sound", "source", "comment", "series_nr", "season_nr", "Statistik"};
    //private String[] validColumnsEpisoden = new String[]{"episodenumber", "name", "size", "duration", "md5", "vcodec", "width", "height", "totalbitrate", "acodecger",
    //        "abitrateger", "channelsger", "acodeceng", "abitrateeng", "channelseng", "hdd", "comment", "checked", "added", "season_nr", "series_nr", "views", "lastView"};
    private String[] validConditionsStaffeln = new String[]{"series_nr", "season_nr"};
    private String[] validConditionsEpisoden = new String[]{"season_nr", "episodenumber"};

    // erstelle Objekt und setze Tabelle, falls gültig
    public SeriesQueryFactory(String table, SharedPreferences preferences) {

        if (Arrays.asList(this.validTables).contains(table)) {
            this.table = table;
        } else {
            Log.d("DEBUG", "SeriesQueryFactory: invalid table " + table);
        }

        this.url += preferences.getString("server", "") + "api.php";
        this.url += "?key=";
        this.url += preferences.getString("apikey", "");
        this.url += "&action=GetDataList";

    }

    // füge Spalten zur Abfrage hinzu
    public void addColumns (String[] columns) {

        for (String column : columns) {
            if (this.table.length() != 0) {
                if (this.columns.length() == 0) {
                    this.columns += column;
                } else {
                    this.columns += ",";
                    this.columns += column;
                }
                // auf Gültigkeit der Spalten kann nicht mehr geprüft werden, da sonst keine unbekannten Spalten möglich wären

                /*
                if ((this.table.equals("Serien") && Arrays.asList(this.validColumnsSerien).contains(column)) || (this.table.equals("Staffeln") && Arrays.asList(this.validColumnsStaffeln).contains(column)) || (this.table.equals("Episoden") && Arrays.asList(this.validColumnsEpisoden).contains(column))) {
                    if (this.columns.length() == 0) {
                        this.columns += column;
                    } else {
                        this.columns += ",";
                        this.columns += column;
                    }
                } else {
                    Log.d("DEBUG", "SeriesQueryFactory: invalid column " + column + " for table " + this.table);
                }
                */
            } else {
                Log.d("DEBUG", "SeriesQueryFactory: cant set column " + column + " - no table selected");
            }
        }

    }

    // füge Bedingungen zur Abfrage hinzu
    public void addConditions (String[] conditions) {

        for (String condition : conditions) {
            if (this.table.length() != 0) {
                if ((this.table.equals("Staffeln") && Arrays.asList(validConditionsStaffeln).contains(condition.split("=")[0])) || (this.table.equals("Episoden") && Arrays.asList(validConditionsEpisoden).contains(condition.split("=")[0]))) {
                    this.conditions += "&" + condition;
                } else {
                    Log.d("DEBUG", "SeriesQueryFactory: invalid condition " + condition + " for table " + this.table);
                }
            } else {
                Log.d("DEBUG", "SeriesQueryFactory: cant set condition " + condition + " - no table selected");
            }
        }
    }

    // setze Sortierreihenfolge ... noch zu implementieren !!!
    public void setOrder (String order, String direction) {

        if (this.table.length() != 0) {

            this.order = "&Sortierung=" + order + "%20" + direction;

        }

    }

    // erstelle URL
    public String getUrl () {

        if (this.table.length() != 0) {

            // füge Tabelle zur URL hinzu
            this.url += "&Tabelle=" + this.table;

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

            Log.d("INFO", "SeriesQueryFactory: build URL " + url);

            return this.url;

        } else {

            Log.d("DEBUG", "SeriesQueryFactory: error building url, no table set");
            return "";

        }

    }

}
