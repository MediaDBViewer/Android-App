package com.example.markus.mediadbviewer;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class DetailedSettingsFragment extends Fragment {

    private View fragment = null;
    private LinearLayout checkBoxLayout;
    private LinkedHashMap<String, CheckBox> checkBoxes = new LinkedHashMap<String, CheckBox>();
    private ArrayList<String> allValues;
    private ArrayList<String> shownValues;
    private String resourceName;
    private MyApplication application;


    public void setValues(ArrayList<String> allValues, ArrayList<String> shownValues, String resourceName) {

        this.allValues = allValues;
        this.shownValues = shownValues;
        this.resourceName = resourceName;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("allValues", this.allValues);
        outState.putStringArrayList("shownValues", this.shownValues);
        outState.putString("resourceName", this.resourceName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            this.allValues = savedInstanceState.getStringArrayList("allValues");
            this.shownValues = savedInstanceState.getStringArrayList("shownValues");
            this.resourceName = savedInstanceState.getString("resourceName");
        }

        if (this.fragment == null) {

            this.fragment = inflater.inflate(R.layout.detailed_settings_fragment, container, false);
            this.checkBoxLayout = (LinearLayout) this.fragment.findViewById(R.id.detailedSettingsCheckboxLayout);
            if (this.allValues != null) {
                for (String value : this.allValues) {
                    if (!value.equals("season_nr") && !value.equals("series_nr")) {
                        CheckBox checkBox = (CheckBox) inflater.inflate(R.layout.simple_checkbox, null);
                        // wenn Spalte unbekannt, wird name aus der Datenbank genommen
                        if (this.application.detailedListNames.keySet().contains(value)) {
                            checkBox.setText(this.application.detailedListNames.get(value));
                        } else {
                            checkBox.setText(value);
                        }
                        if (this.shownValues.contains(value)) {
                            checkBox.setChecked(true);
                        }
                        checkBox.setId(checkBoxes.size()); // muss gesetzt werden, damit beim wiederherstellen des Fragments nicht alle Checkboxes gleich sind
                        this.checkBoxes.put(value, checkBox);
                        this.checkBoxLayout.addView(checkBox);
                    }
                }
                Button saveButton = (Button) fragment.findViewById(R.id.detailedSettingsSaveButton);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<String> values = new ArrayList<String>();
                        for (String key : checkBoxes.keySet()) {
                            if (checkBoxes.get(key).isChecked()) {
                                values.add(key);
                            }
                        }
                        application.saveArrayList(resourceName, values);
                        if (resourceName.equals("movieFilterShown")) {
                            application.initializeFilterBar(getActivity());
                        }
                    }
                });
            }
        }

        return this.fragment;

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.application = (MyApplication) getActivity().getApplication();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ((ViewGroup) this.fragment).removeAllViews();
    }
}
