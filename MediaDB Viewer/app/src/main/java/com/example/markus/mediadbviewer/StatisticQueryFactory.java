package com.example.markus.mediadbviewer;


import android.content.SharedPreferences;
import android.util.Log;

import java.util.Arrays;

public class StatisticQueryFactory {

    private String statistic = "";
    private String rootUrl = "";
    private String url = "";

    public StatisticQueryFactory(SharedPreferences preferences) {

        this.rootUrl += preferences.getString("server", "") + "api.php";
        this.rootUrl += "?key=";
        this.rootUrl += preferences.getString("apikey", "");
        this.rootUrl += "&action=GetStatistic";

    }

    public void setStatistic(String statistic) {

        this.statistic = statistic;

    }

    public String getUrl() {


        this.url = this.rootUrl + "&Statistik=" + this.statistic;
        Log.d("INFO", "StatisticQueryFactory: build URL " + this.url);
        return this.url;

    }

}
