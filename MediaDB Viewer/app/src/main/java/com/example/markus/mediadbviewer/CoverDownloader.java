package com.example.markus.mediadbviewer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Movie;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

class CoverDownloader extends AsyncTask<String, String, String> {

    private boolean allCover;
    private boolean moveCover;
    private ArrayList<MediaObject> movieList;
    private ArrayList<MediaObject> seriesList;
    private ArrayList<MediaObject> seasonList;
    private MainActivity activity;
    private String rootDomain;
    private int fileCount;
    private MyApplication application;
    private ProgressDialogFragment progressDialogFragment;

    public CoverDownloader(Boolean allCover, MainActivity activity, MyApplication application) {

        this.activity = activity;
        this.application = application;
        if (this.application.preferences.getString("coverServer", "").length() < 8) {
            this.rootDomain = this.application.preferences.getString("server", "") + "cover/";
        } else {
            this.rootDomain = this.application.preferences.getString("coverServer", "") + "cover/";
        }
        if (allCover != null) {
            this.allCover = allCover;
        } else {
            this.moveCover = true;
        }

    }

    // zählt alle Dateien in einem Verzeichnis
    private void getFileNumber(String dirPath) {
        File f = new File(dirPath);
        File[] files = f.listFiles();

        if (files != null)
            for (int i = 0; i < files.length; i++) {
                this.fileCount++;
                File file = files[i];

                if (file.isDirectory()) {
                    getFileNumber(file.getAbsolutePath());
                }
            }
    }

    private void deleteRecursivly(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles())
                deleteRecursivly(child);
            file.delete();
        } else {
            file.delete();
        }
    }

    private void moveCover() {

        String sourceFolder = "";
        if (this.application.preferences.getString("storageType", this.application.getResources().getStringArray(R.array.storageType)[0]).equals(this.application.getResources().getStringArray(R.array.storageType)[0])) {
            sourceFolder = this.application.externalAppFolder + "cover/";
        } else {
            sourceFolder = this.application.internalAppFolder + "cover/";
        }
        this.copyFolder(new File(sourceFolder), new File(this.application.rootCoverFolder));
        this.deleteRecursivly(new File(sourceFolder));

    }

    public void copyFolder(File sourceLocation, File targetLocation) {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {
                copyFolder(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {

            this.progressDialogFragment.incrementProgress();
            try {

                InputStream in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();

            } catch (Exception e) {
                // muss abgefangen werden
                Log.d("EXCEPTION", "CoverDownloader: " + e.getClass().getName());
            }
        }

    }

    // lädt alle Cover eines Types mmit bestimmter Qualität herunter
    private void downloadCoverCollection(String coverType, String quality, ArrayList<String> keys) {

        String downloadFolder = "";
        String downloadUrl = "";

        // erstelle URL und Downloadpfad
        if (coverType.equals("Filme") && keys.get(0).length() == 7) {
            if (quality.equals("low")) {
                downloadFolder = this.application.rootCoverFolder + coverType + "/" + quality + "/";
                downloadUrl = rootDomain + coverType + "/" + quality + "/";
            } else if (quality.equals("high")) {
                downloadFolder = this.application.rootCoverFolder + coverType + "/" + quality + "/";
                downloadUrl = rootDomain + coverType + "/" + quality + "/";
            } else {
                Log.d("DEBUG", "CoverDownloader: invalid quality " + quality + " for cover type " + coverType);
            }
        } else if (coverType.equals("Serien")) {
            if (quality.equals("low")) {
                downloadFolder = this.application.rootCoverFolder + coverType + "/" + quality + "/";
                downloadUrl = rootDomain + coverType + "/" + quality + "/";
            } else {
                Log.d("DEBUG", "CoverDownloader: invalid quality " + quality + " for cover type " + coverType);
            }
        } else if (coverType.equals("Staffeln")) {
            if (quality.equals("low")) {
                downloadFolder = this.application.rootCoverFolder + coverType + "/" + quality + "/";
                downloadUrl = rootDomain + coverType + "/" + quality + "/";
            } else if (quality.equals("high")) {
                downloadFolder = this.application.rootCoverFolder + coverType + "/" + quality + "/";
                downloadUrl = rootDomain + coverType + "/" + quality + "/";
            } else {
                Log.d("DEBUG", "CoverDownloader: invalid quality " + quality + " for cover type " + coverType);
            }
        } else {
            Log.d("DEBUG", "CoverDownloader: could not download for " + quality + " with key " + keys.get(0));
        }

        File downloadFolderFile = new File(downloadFolder);
        // erstelle downloadFolder
        if (!downloadFolderFile.exists()) {
            downloadFolderFile.mkdirs();
        }

        // lösche alle Dateien im Ordner
        if (this.allCover) {
            File[] files = downloadFolderFile.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }

        for (String key: keys) {

            this.downloadCover(downloadUrl + key + ".jpg", downloadFolder + key + ".jpg");

        }

    }


    // lädt ein bestimmtes Cover herunter
    private void downloadCover(String downloadUrl, String targetFilePath) {

        File targetFile = new File(targetFilePath);

        // downloade alle cover
        if (!targetFile.exists()) {
            int count;
            try {
                URL url = new URL(downloadUrl);
                URLConnection connection = url.openConnection();
                connection.connect();

                // getting file length
                int lenghtOfFile = connection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                OutputStream output = new FileOutputStream(targetFile);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {

            }

            // inkrementiere Progress
            this.progressDialogFragment.incrementProgress();
            //this.progressDialogFragment.progressDialog.setProgress(this.progressDialogFragment.progressDialog.getProgress() + 1);
        }

    }


    @Override
    protected void onPreExecute(){

        this.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        if (!this.moveCover) {

            // berechne Anzahl der Files
            this.getFileNumber(this.application.rootCoverFolder);

            // erstelle Query Factories
            MovieQueryFactory movieQueryFactory = new MovieQueryFactory(this.application.preferences);
            movieQueryFactory.addColumns(new String[]{"imdbID"});
            SeriesQueryFactory seriesQueryFactory = new SeriesQueryFactory("Serien", this.application.preferences);
            seriesQueryFactory.addColumns(new String[]{"series_nr"});
            SeriesQueryFactory seasonQueryFactory = new SeriesQueryFactory("Staffeln", this.application.preferences);
            seasonQueryFactory.addColumns(new String[]{"season_nr"});

            // hole ArrayLists
            JSONParser movieParser = new JSONParser(this.activity, this.application);
            try {
                movieParser.execute(movieQueryFactory.getUrl()).get();
            } catch (Exception e) {
            }
            this.movieList = movieParser.getMediaList();
            JSONParser seriesParser = new JSONParser(this.activity, this.application);
            try {
                seriesParser.execute(seriesQueryFactory.getUrl()).get();
            } catch (Exception e) {
            }
            this.seriesList = seriesParser.getMediaList();
            JSONParser seasonParser = new JSONParser(this.activity, this.application);
            try {
                seasonParser.execute(seasonQueryFactory.getUrl()).get();
            } catch (Exception e) {
            }
            this.seasonList = seasonParser.getMediaList();

            // initialisiere Progressbar
            Bundle arguments = new Bundle();
            if (this.allCover) {
                arguments.putInt("progressMax", this.movieList.size() * 2 + this.seriesList.size() + (this.seasonList.size() * 2));
            } else {
                arguments.putInt("progressMax", (this.movieList.size() * 2) + this.seriesList.size() + (this.seasonList.size() * 2) - this.fileCount);
            }
            arguments.putString("message", this.activity.getResources().getString(R.string.updateDialogMessage));

            this.progressDialogFragment = new ProgressDialogFragment();
            this.progressDialogFragment.setArguments(arguments);
            this.progressDialogFragment.setCancelable(false);
            this.progressDialogFragment.show(this.activity.getSupportFragmentManager(), "coverDownloadProgressFragment");

        } else {

            String sourceFolder = "";
            if (this.application.preferences.getString("storageType", this.application.getResources().getStringArray(R.array.storageType)[0]).equals(this.application.getResources().getStringArray(R.array.storageType)[0])) {
                sourceFolder = this.application.externalAppFolder + "cover/";
            } else {
                sourceFolder = this.application.internalAppFolder + "cover/";
            }

            Bundle arguments = new Bundle();
            this.getFileNumber(sourceFolder);
            arguments.putInt("progressMax", this.fileCount);
            arguments.putString("message", this.activity.getResources().getString(R.string.moveCoverMessage));
            this.progressDialogFragment = new ProgressDialogFragment();
            this.progressDialogFragment.setArguments(arguments);
            this.progressDialogFragment.setCancelable(false);
            this.progressDialogFragment.show(this.activity.getSupportFragmentManager(), "coverMoveProgressFragment");

        }

        super.onPreExecute();
    }


    @Override
    protected String doInBackground(String[] keys) {

        if (this.moveCover) {
            this.moveCover();

        } else {

            ArrayList<String> imdbIDs = new ArrayList<String>();
            ArrayList<String> series_nrs = new ArrayList<String>();
            ArrayList<String> season_nrs = new ArrayList<String>();
            imdbIDs.add("0000000");

            // befülle ArrayLists
            for (MediaObject movie : this.movieList) {
                imdbIDs.add(movie.simpleValues.get("imdbID"));
            }
            for (MediaObject series : this.seriesList) {
                series_nrs.add(String.valueOf(series.simpleValues.get("series_nr")));
            }
            for (MediaObject season : this.seasonList) {
                season_nrs.add(String.valueOf(season.simpleValues.get("season_nr")));
            }

            // lade Cover
            this.downloadCoverCollection("Filme", "low", imdbIDs);
            this.downloadCoverCollection("Serien", "low", series_nrs);
            this.downloadCoverCollection("Staffeln", "low", season_nrs);
            this.downloadCoverCollection("Filme", "high", imdbIDs);
            this.downloadCoverCollection("Staffeln", "high", season_nrs);

        }

        return null;

    }

    @Override
    protected void onPostExecute(String link_url) {

        try {
            this.progressDialogFragment.dismiss();
        } catch (Exception e) {
            // tritt auf, wenn Cover bei gesperrten Bildschirm geladen werden und das Fragment geschlossen wird
            Log.d("EXCEPTION", "CoverDownloader: " + e.getClass().getName());
        }

        this.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

}
