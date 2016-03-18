package com.example.markus.mediadbviewer;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;

public class GeneralSettingsFragment extends Fragment {

    private View fragment = null;
    private boolean initialized = false;
    private MyApplication application;

    private EditText settingApiKeyField;
    private EditText settingServerDomainField;
    //private EditText settingServerCoverDomainField;
    private EditText settingScrollSpeedField;
    private EditText settingCacheValidityField;
    private TextView settingCacheSizeText;
    private Spinner settingCoverFlowOrderSpinner;
    private CheckBox settingHideSeenCheckBox;
    private CheckBox settingShowFinishedCheckBox;
    private EditText settingCoverFlowItemCount;
    private Spinner settingStorageSpinner;

    private void showWrongConectionAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog = builder.setMessage("Falscher API-Key oder Server angegeben!").setTitle(R.string.settingsMissingTitle).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

    }

    final private boolean savePreferences(boolean disableRestart) {

        SharedPreferences.Editor preferencesEditor = this.application.preferences.edit();

        boolean accessChanged = false;
        String apikey = settingApiKeyField.getText().toString();
        if (!apikey.equals(this.application.preferences.getString("apikey", null))) {
            preferencesEditor.putString("apikey", apikey);
            accessChanged = true;
        }

        String server = settingServerDomainField.getText().toString();
        if (server.length() > 0) {
            if (!server.substring(0, 7).equals("http://")) {
                server = "http://" + server;
            }
            if (!server.substring(server.length() - 1).equals("/")) {
                server += "/";
            }
        }
        if (!server.equals(this.application.preferences.getString("server", null))) {
            preferencesEditor.putString("server", server);
            accessChanged = true;
        }

        // prüfe ob Kombination aus Key und Server zulässig ist
        if (accessChanged) {
            MovieQueryFactory factory = new MovieQueryFactory(this.application.preferences);
            factory.setKey(settingApiKeyField.getText().toString());
            factory.setServer(server);
            JSONParser parser = new JSONParser(null, this.application);
            try {
                parser.execute(new String[]{factory.getRightsURL()}).get();
            } catch (Exception e) {
            }
            if ((parser.isKeyValid() == null) || (parser.isKeyValid() == false)) {
                return false;
            }
        }

        /*
        String coverServer = settingServerCoverDomainField.getText().toString();
        if (coverServer.length() > 0) {
            if (!coverServer.substring(0, 7).equals("http://")) {
                coverServer = "http://" + coverServer;
            }
            if (!coverServer.substring(coverServer.length() - 1).equals("/")) {
                coverServer += "/";
            }
        }

        preferencesEditor.putString("coverServer", coverServer);
        */

        String scrollSpeed = settingScrollSpeedField.getText().toString();
        if (scrollSpeed.length() > 0) {
            if (Integer.valueOf(scrollSpeed) < 1) {
                scrollSpeed = "1";
            } else if (Integer.valueOf(scrollSpeed) > 20) {
                scrollSpeed = "20";
            }
            preferencesEditor.putString("scrollSpeed", scrollSpeed);
        }

        String cacheValidity = settingCacheValidityField.getText().toString();
        if (cacheValidity.length() > 0) {
            preferencesEditor.putString("cacheValidity", cacheValidity);
        }

        if (this.settingCoverFlowOrderSpinner.getSelectedItem() != null) {
            preferencesEditor.putString("coverFlowOrder", (String) this.application.orderNames.get(this.settingCoverFlowOrderSpinner.getSelectedItem()));
        }

        if (!((String) this.settingStorageSpinner.getSelectedItem()).equals(this.application.preferences.getString("storageType", "Handy"))) {

            preferencesEditor.putString("storageType", (String) this.settingStorageSpinner.getSelectedItem());
            preferencesEditor.commit();
            this.application.setCoverRootFolder();
            CoverDownloader downloader = new CoverDownloader(null, (MainActivity) getActivity(), this.application);
            downloader.execute(new String[0]);

        }

        preferencesEditor.putBoolean("coverFlowHideSeen", this.settingHideSeenCheckBox.isChecked());

        preferencesEditor.putBoolean("showFinished", this.settingShowFinishedCheckBox.isChecked());

        String coverFlowItemCount = this.settingCoverFlowItemCount.getText().toString();
        if (coverFlowItemCount.length() > 0) {
            preferencesEditor.putString("coverFlowItemCount", coverFlowItemCount);
        }

        // muss als letztes ausgeführt werden
        preferencesEditor.commit();

        // lade Defaults, falls diese noch nicht gesetzt sind
        if (this.application.preferences.getString("valuesMovieShown", null) == null) {
            this.application.configureDefaults();
        }

        if (accessChanged && !disableRestart) {
            ((MainActivity) getActivity()).restartSettings();
        }

        return true;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            //schließe alle Fortschrittsfragment, falls diese noch offen sein sollten
            FragmentManager manager = this.getActivity().getSupportFragmentManager();
            if (manager.findFragmentByTag("coverDownloadProgressFragment") != null) {
                Log.d("DEBUG", "found Download Fragment");
                manager.beginTransaction().remove(manager.findFragmentByTag("coverDownloadProgressFragment")).commit();
            }
            if (manager.findFragmentByTag("coverMoveProgressFragment") != null) {
                manager.beginTransaction().remove(manager.findFragmentByTag("coverMoveProgressFragment")).commit();
            }
        }

        if (!this.initialized) {

            final MainActivity activity = (MainActivity) getActivity();

            final SharedPreferences preferences = this.application.preferences;
            this.fragment = inflater.inflate(R.layout.general_settings_fragment, container, false);

            // initialisiere EditText Felder mit gespeicherten Werten
            this.settingApiKeyField = (EditText) fragment.findViewById(R.id.settingsApiKeyField);
            this.settingServerDomainField = (EditText) fragment.findViewById(R.id.settingsServerDomainField);
            //this.settingServerCoverDomainField = (EditText) fragment.findViewById(R.id.settingsServerCoverDomainField);
            this.settingScrollSpeedField = (EditText) fragment.findViewById(R.id.settingsScrollSpeedField);
            this.settingCacheValidityField = (EditText) fragment.findViewById(R.id.settingsCacheValidityField);
            this.settingCacheSizeText = (TextView) fragment.findViewById(R.id.cacheSizeValue);
            this.settingCoverFlowOrderSpinner = (Spinner) fragment.findViewById(R.id.settingsCoverFlowOrderSpinner);
            this.settingHideSeenCheckBox = (CheckBox) fragment.findViewById(R.id.settingsCoverFlowHideSeenCheckBox);
            this.settingShowFinishedCheckBox = (CheckBox) fragment.findViewById(R.id.settingsShowFinishedCheckBox);
            this.settingCoverFlowItemCount = (EditText) fragment.findViewById(R.id.settingsCoverFlowItemCountField);
            this.settingStorageSpinner = (Spinner) fragment.findViewById(R.id.settingsCoverStorageSpinner);

            // setze aktuelle Werte für EditText Felder
            this.settingApiKeyField.setText(preferences.getString("apikey", ""));
            this.settingServerDomainField.setText(preferences.getString("server", ""));
            //this.settingServerCoverDomainField.setText(preferences.getString("coverServer", ""));
            this.settingScrollSpeedField.setText(preferences.getString("scrollSpeed", ""));
            this.settingCacheValidityField.setText(preferences.getString("cacheValidity", ""));
            this.settingCacheSizeText.setText(String.valueOf(Math.round(((new File(this.application.internalAppFolder + "shared_prefs/cache.xml")).length() + (new File(application.rootCoverFolder.replace("cover/", "shared_prefs/cacheTimestamps.xml"))).length())*100/Math.pow(1024,2))/100.).replace(".", ",") + " MB");
            this.settingHideSeenCheckBox.setChecked(this.application.preferences.getBoolean("coverFlowHideSeen", false));
            this.settingShowFinishedCheckBox.setChecked(this.application.preferences.getBoolean("showFinished", false));
            this.settingCoverFlowItemCount.setText(this.application.preferences.getString("coverFlowItemCount", ""));


            // befülle Spinner für Coverflow Kriterium
            ArrayList<String> availableCategories = new ArrayList<>();
            // prüfe auf welche Spalten der Zugriff erlaubt ist
            for (String category : getResources().getStringArray(R.array.categorySpinner)) {
                if (this.application.valuesMovieShown.contains(category)) {
                    availableCategories.add(this.application.orderNames.get(category));
                }
            }
            ArrayAdapter<CharSequence> coverFlowOrderAdapter = new ArrayAdapter<CharSequence>(activity, R.layout.spinner_layout, availableCategories.toArray(new String[0]));
            coverFlowOrderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.settingCoverFlowOrderSpinner.setAdapter(coverFlowOrderAdapter);
            this.settingCoverFlowOrderSpinner.setSelection(coverFlowOrderAdapter.getPosition(this.application.orderNames.get(this.application.preferences.getString("coverFlowOrder", "added"))));

            // befülle Spinner für Speicherort
            getContext().getExternalFilesDir(null).getAbsolutePath(); // erstelle den App-Ordner auf Speicherkarte
            ArrayAdapter<CharSequence> storageAdapter;
            // prüfe ob der Files-Ordner existiert oder Schreibrechte auf den Android/data/ Folder auf der SD-Karte existieren
            if ((this.application.externalAppFolder != null) && (new File(this.application.externalAppFolder).exists() || new File(this.application.externalAppFolder.replace("com.example.markus.mediadbviewer/files/", "")).canWrite())) {
                storageAdapter = ArrayAdapter.createFromResource(activity, R.array.storageType, R.layout.spinner_layout);
            } else {
                storageAdapter = new ArrayAdapter<CharSequence>(activity, R.layout.spinner_layout, new String[]{getResources().getStringArray(R.array.storageType)[0]});
            }
            storageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.settingStorageSpinner.setAdapter(storageAdapter);
            this.settingStorageSpinner.setSelection(storageAdapter.getPosition(this.application.preferences.getString("storageType", "Handy")));

            // setze OnClickListener zum Laden aller Cover
            Button downloadAllCover = (Button) fragment.findViewById(R.id.downloadAllCoverButton);
            downloadAllCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (savePreferences(true)) {
                        new CoverDownloader(true, ((MainActivity) getActivity()), application).execute();
                    } else {
                        showWrongConectionAlert();
                    }
                }
            });

            // setze OnClickListener zum Laden fehlender Cover
            Button downloadMissingCover = (Button) fragment.findViewById(R.id.downloadMissingCoverButton);
            downloadMissingCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (savePreferences(true)) {
                        new CoverDownloader(false, ((MainActivity) getActivity()), application).execute();
                    } else {
                        showWrongConectionAlert();
                    }
                }
            });

            // setze OnClickListener zum Leeren des Caches
            Button clearCache = (Button) fragment.findViewById(R.id.clearCacheButton);
            clearCache.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    application.cache.edit().clear().commit();
                    application.cacheTimestamps.edit().clear().commit();
                    ((TextView) ((ViewGroup) v.getParent()).findViewById(R.id.cacheSizeValue)).setText(String.valueOf(Math.round(((new File(application.rootCoverFolder.replace("cover/", "shared_prefs/cache.xml"))).length() + (new File(application.internalAppFolder + "shared_prefs/cacheTimestamps.xml")).length()) * 100 / Math.pow(1024, 2)) / 100.).replace(".", ",") + " MB");
                }
            });

            // setze onClickListener für Save Button
            Button saveButton = (Button) fragment.findViewById(R.id.settingsSaveButton);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!savePreferences(false)) {
                        showWrongConectionAlert();
                    }
                }
            });

        }

        return this.fragment;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.application = (MyApplication) getActivity().getApplication();
    }
}
