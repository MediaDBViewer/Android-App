package com.example.markus.mediadbviewer;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.app.ListFragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class OwnListFragment extends DetailedFragment {

    public View fragment;

    // setz im Hintergrund des Fragments einen Fehlertext
    public void setBackgroundHint(String hintText) {

        if (this.fragment != null) {

            TextView backgroundHint = (TextView) this.fragment.findViewById(R.id.listBackgroundHint);
            backgroundHint.setText(hintText);
            backgroundHint.setVisibility(TextView.VISIBLE);

        }

    }

    public void hideBackgroundHint() {

        TextView backgroundHint = (TextView) this.fragment.findViewById(R.id.listBackgroundHint);
        backgroundHint.setVisibility(TextView.GONE);

    }

}
