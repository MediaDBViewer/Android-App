package com.example.markus.mediadbviewer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.example.markus.mediadbviewer.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class SettingsFragment extends Fragment {

    private ViewGroup rootView = null;
    private MyApplication application;
    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (this.rootView == null) {
            this.rootView = (ViewGroup) inflater.inflate(R.layout.settings_fragment, container, false);

            this.rootView.setBackgroundColor(getResources().getColor(R.color.background));

            // Instantiate a ViewPager and a PagerAdapter.
            this.mPager = (ViewPager) this.rootView.findViewById(R.id.settingsPager);
            this.mPagerAdapter = new ScreenSlidePagerAdapter(getActivity().getSupportFragmentManager());
            this.mPagerAdapter.setContext(this.application, (MainActivity) getActivity());
            this.mPager.setAdapter(mPagerAdapter);
            this.mPager.setOffscreenPageLimit(4); // Anzahl der Pages die nach links und rechts im Speicher gehalten werden
        }
        return rootView;

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Einstellungen");
    }

    @Override
    public void onPause() {
        super.onPause();
        // Schließe Tastatur nach Eingabe
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.rootView.getWindowToken(), 0);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.application = (MyApplication) getActivity().getApplication();
    }

}