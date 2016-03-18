package com.example.markus.mediadbviewer;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

// wird nur erstellt, wenn Verbindung zum Server fehlgeschlagen ist
public class DummySettingsFragment extends Fragment {

    private View fragment;

    // gibt true zurück, wenn eine Netzwerkverbindung besteht
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (this.fragment == null) {
            this.fragment = inflater.inflate(R.layout.dummy_settings_fragment, container, false);
        }

        // setze den Hintergrundtext

        TextView backgroundText = (TextView) this.fragment.findViewById(R.id.settingsFragmentInfoText);

        // kein Netzwerk verfügbar
        if (!this.isNetworkAvailable()) {
            backgroundText.setText(R.string.hintNoNetworkConnection);

        // Server trotz Netzwerkverbinung nicht erreichbar
        } else {
            backgroundText.setText(R.string.hintServerNotAvailable);
        }

        return this.fragment;

    }
}
