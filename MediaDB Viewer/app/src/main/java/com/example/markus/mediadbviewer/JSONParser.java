package com.example.markus.mediadbviewer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

class JSONParser extends AsyncTask<String, String, String> {

    private MainActivity activity;
    private MyApplication application;
    private boolean fromCache = false;
    private MediaObject initObject = null;
    private String url;
    private ArrayAdapter<MediaObject> adapter = null;
    private Fragment fragment = null;
    private ArrayList<MediaObject> mediaList = new ArrayList<MediaObject>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private ProgressDialog progress;
    private HashMap<String, ArrayList<String>> rightsMap = new HashMap<>();
    private boolean updateRight;
    private Boolean rightKey = null;
    private boolean forceReload = false;

    public void setArrayAdapter(ArrayAdapter<MediaObject> adapter) {

        this.adapter = adapter;
    }

    public void setFragment(Fragment fragment) {

        this.fragment = fragment;

    }

    public void setForcReload() {

        this.forceReload = true;

    }

    private void saveObject(String key, ArrayList<MediaObject> object) {

        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(object);
            so.flush();
            this.application.cache.edit().putString(key, new String(Base64.encodeToString(bo.toByteArray(), 0))).commit();
        } catch (Exception e) {
        }

    }

    // gibt NULL zurück, wenn Server Antwort nicht eindeutig geparst werden konnte
    // sonst ist es true oder false
    public Boolean isKeyValid() {

        return this.rightKey;

    }

    private ArrayList<MediaObject> loadObject(String key) {

        try {
            byte b[] = Base64.decode(this.application.cache.getString(key, "").getBytes(),0);
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            return (ArrayList<MediaObject>) si.readObject();

        } catch (Exception e) {}

        return new ArrayList<MediaObject>();

    }

    public JSONParser(MainActivity activity, MyApplication application) {

        this.activity = activity;
        this.application = application;

    }

    public ArrayList<MediaObject> getMediaList() {

        return this.mediaList;

    }

    public HashMap<String, ArrayList<String>> getRightsMap() {

        return this.rightsMap;

    }

    public boolean getUpdateRight() {

        return this.updateRight;

    }


    @Override
    protected String doInBackground(String[] links) {

        boolean firstLoop = true;
        for (String link : links) {

            this.url = link;

            // falls Informationen für Staffeln und Episode abgerufen werden
            if (!firstLoop && this.mediaList != null && this.mediaList.size() > 0) {
                this.initObject = this.mediaList.get(0);
            }

            firstLoop = false;
            try {
                if (this.activity != null) {
                    ConnectivityManager connManager = (ConnectivityManager) this.activity.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    Date lastCacheDate = this.dateFormat.parse(this.application.cacheTimestamps.getString(this.url, "2015-01-01 00:00:00"));

                    // kein WLAN verfügbar, Cache wird genutzt, wenn Gültigkeit nicht überschritten
                    if (!mWifi.isConnected() && this.application.cache.contains(this.url) && ((new Date()).getTime() - lastCacheDate.getTime()) < (1000 * 3600 * 24 * Integer.valueOf(this.application.preferences.getString("cacheValidity", "7"))) && !this.forceReload) {

                        this.mediaList = this.loadObject(links[0]);
                        this.fromCache = true;

                    } else {
                        URL url = new URL(this.url);
                        URLConnection connection = url.openConnection();
                        connection.connect();
                        InputStream stream = connection.getInputStream();
                        this.mediaList = readJsonStream(stream);
                    }

                } else {
                    URL url = new URL(this.url);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    InputStream stream = connection.getInputStream();
                    this.mediaList = readJsonStream(stream);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("EXCEPTION", "JSONParser: " + e.getClass().getName());

                // trotz Internetverbingung konnte keine Verbindung zum Host aufgebaut werden
                if (e.getClass().getName().equals("java.net.ConnectException") && this.application.cache.contains(this.url)) {

                    // Lade Daten aus Cache
                    this.mediaList = this.loadObject(links[0]);
                    this.fromCache = true;

                // Objekt konnte nicht aus Cache geladen werden, tritt auf, wenn keine Internetverbindung vorhanden
                } else if (this.fragment != null) {
                    // setze Hinweis im Hintergrund
                    // Detailed und List-Fragmente werden unterschieden, da sie von verschiedenen Klassen erben
                    if (this.fragment.getClass().getName().contains("List")) {
                        this.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((OwnListFragment) fragment).setBackgroundHint(activity.getResources().getString(R.string.hintNoCacheEntryAvailable));
                            }
                        });
                    } else if (this.fragment.getClass().getName().contains("Detailed")) {
                        this.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((DetailedFragment) fragment).setBackgroundHint(activity.getResources().getString(R.string.hintNoCacheEntryAvailable));
                            }
                        });
                    }
                }

            }
        }

        if (this.adapter != null) {
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.clear();
                    for (MediaObject media : mediaList) {
                        adapter.add(media);
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        }

        return null;
    }

    public ArrayList<MediaObject> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        ArrayList<MediaObject> list = new ArrayList<>();
        try {
            reader.beginObject(); //startet das root Object, welches mit { beginnt

            reader.nextName(); // nextName muss aufgerufen werden !!!
            reader.skipValue();

            if (reader.nextName().equals("API_KEY")) {
                if (reader.peek() == JsonToken.NULL) {
                    this.rightKey = false;
                } else {
                    this.rightKey = true;
                }
                reader.skipValue();
            }

            reader.nextName(); // nextName muss aufgerufen werden !!!
            reader.skipValue();

            if (reader.nextName().equals("Antwort")) {
                reader.beginObject();
                if (this.url.contains("GetKeyRights")) {
                    readRightsObject(reader);
                } else {
                    // skippe Spalten
                    reader.nextName();
                    reader.skipValue();
                    if (reader.nextName().equals("Data")) {
                        list = readMessagesArray(reader);
                    }
                }
                reader.endObject();
            }
            reader.endObject();
        } catch (Exception e) {
            Log.d("JSONFehler", e.getMessage());
        } finally {
            reader.close();
        }
        return list;
    }

    public ArrayList<MediaObject> readMessagesArray(JsonReader reader) throws IOException {
        ArrayList<MediaObject> media = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            media.add(readMessage(reader));
        }
        reader.endArray();

        if (!this.fromCache) {
            this.saveObject(this.url, media);
            this.application.cacheTimestamps.edit().putString(this.url, this.dateFormat.format(new Date())).commit();
        }
        return media;
    }

    public MediaObject readMessage(JsonReader reader) throws IOException {
        final MediaObject mediaObject;
        if (this.initObject == null) {
            mediaObject = new MediaObject();
        } else {
            mediaObject = this.initObject;
            mediaObject.simpleValues.put("commentEpisode", mediaObject.simpleValues.get("comment"));
        }

        reader.beginObject();
        while (reader.hasNext()) {

        String name = reader.nextName();
            //Log.d("JSON", name);
            if (name.equals("Schauspieler")) {
                reader.beginObject();
                String actor;
                String role;
                while (reader.hasNext()) {
                    actor = reader.nextName();
                    // prüfe, ob die Null ist oder nicht
                    if (reader.peek() != JsonToken.NULL) {
                        role = reader.nextString();
                    } else {
                        reader.skipValue();
                        role = "";
                    }
                    mediaObject.schauspieler.put(actor, role);
                }
                reader.endObject();
            } else if (name.equals("Genre") && reader.peek() != JsonToken.NULL) {
                reader.beginArray();
                while (reader.hasNext()) {
                    mediaObject.Genre.add(reader.nextString());
                }
                reader.endArray();
            } else {
                try {
                    String value = reader.nextString();
                    mediaObject.simpleValues.put(name, value);
                } catch (Exception e) {
                    mediaObject.simpleValues.put(name, "");
                }
            }
        }
        reader.endObject();

        return mediaObject;

    }

    public void readRightsObject(JsonReader reader) throws IOException {

        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("Update")) {
                this.updateRight = reader.nextBoolean();
            } else if (!name.equals("SpaltenFilme") && !name.equals("SpaltenEpisoden") && !name.equals("StatistikViews")) {
                reader.skipValue();
            } else {
                ArrayList<String> rightsList = new ArrayList<>();
                reader.beginArray();
                while (reader.hasNext()) {
                    rightsList.add(reader.nextString());
                }
                reader.endArray();
                this.rightsMap.put(name, rightsList);
            }
        }

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (this.activity != null) {
            try {
                this.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress = ProgressDialog.show(activity, "", "Lade Daten ...", true, false);
                    }
                });
            } catch (Exception e) {

            }
         }
    }

    @Override
    protected void onPostExecute(String s) {
        try {this.progress.dismiss();} catch (Exception e){}
        if (this.fragment != null) {
            try {
                ((DetailedFragment) this.fragment).updateData();
            } catch (Exception e) {
                // falls Handy während dem Laden der Daten gedreht wird
            }
        }
    }
}