package com.example.markus.mediadbviewer;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker extends AsyncTask<MainActivity, Void, Void> {

    protected String getContentFromRemoteFile(String contentUrl) {

        String content = "";

        try {
            URL url = new URL(contentUrl);
            URLConnection connection = url.openConnection();
            connection.connect();

            // getting file length
            int lenghtOfFile = connection.getContentLength();

            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            BufferedReader r = new BufferedReader(new InputStreamReader(input));

            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line + "\n");
            }
            r.close();
            content = total.toString();

        } catch (Exception e) {

            Log.d("EXCEPTION", "UpdateChecker: " + e.getClass().getName());

        }

        return content;

    }

    @Override
    protected Void doInBackground(MainActivity... params) {

        MainActivity activity = params[0];

        ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        // wird nur ausgeführt, wenn mit WLAN verbunden

        if (mWifi.isConnected()) {

            MyApplication application = (MyApplication) activity.getApplication();
            String server = application.preferences.getString("server", null);
            URLChecker checker = new URLChecker();
            Bundle arguments = new Bundle(); // zur Übergabe an den Dialog

            // bricht Ausführung ab, falls noch kein Server gesetzt ist oder die apk bzw. die Versionsnummer nicht verfügbar sind
            if ((server == null) || (!checker.checkURL(server + "app/Version.txt")) || (!checker.checkURL(server + "app/MediaDBViewer.apk"))) {
                return null;
            }

            arguments.putString("appLink", server + "app/MediaDBViewer.apk");
            arguments.putString("recentVersion", this.getContentFromRemoteFile(server + "app/Version.txt"));

            // verhindert Null-Poitner-Exception beim Vergleich des ersten Zeichens
            if (arguments.getString("recentVersion").length() > 0) {

                // durch Längenbegrenzung und Vergleich des ersten Buchstaben wird falscher Content abgefangen, falls z.B. eine Anmeldung im WLAN notwendig ist
                if (arguments.getString("recentVersion").substring(0, 1).equals("v") && arguments.getString("recentVersion").length() < 15 && !arguments.getString("recentVersion").replaceAll(" ", "").replaceAll("\t", "").replaceAll("\n", "").equals(activity.getResources().getString(R.string.app_version))) {

                    // setze Changelog
                    if (checker.checkURL(server + "app/Changelog.txt")) {
                        arguments.putString("changeLog", this.getContentFromRemoteFile(server + "app/Changelog.txt"));
                    } else {
                        arguments.putString("changeLog", "Changelog.txt ist auf dem Server nicht vorhanden");
                    }


                    // zeige Dialog
                    try {
                        AppUpdateFragment dialog = new AppUpdateFragment();
                        dialog.setArguments(arguments);
                        dialog.show(activity.getSupportFragmentManager(), "appUpdateDialogFragment");
                    } catch (Exception e) {
                        Log.d("EXCEPTION", "UpdateChecker: " + e.getClass().getName());
                        // gedreht während Abfrage lief
                    }

                }

            }

        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
