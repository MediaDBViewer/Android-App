package com.example.markus.mediadbviewer;


import android.os.AsyncTask;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class URLChecker extends AsyncTask<String, String, String> {

    private boolean exists = false;

    public URLChecker() {

    }

    public boolean getResult() {

        return this.exists;

    }

    public boolean checkURL(String url) {
        try {
            Log.d("INFO", "URLChecker: checking " + url);
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con =  (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("HEAD");
            con.setRequestProperty( "Accept-Encoding", "" ); //verhindert EOF-Exception
            this.exists = (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            Log.d("EXCEPTION", "URLChecker: " + e.getClass().getName());
            this.exists = false;
        }
        return this.exists;

    }

    protected String doInBackground(String[] URLName){
        checkURL(URLName[0]);
        return null;
    }

}
