package com.example.markus.mediadbviewer;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

public class DetailedFragment extends Fragment {

    public View fragment;

    public void updateData(){}

    // setz im Hintergrund des Fragments einen Fehlertext
    public void setBackgroundHint(String hintText) {

        if (this.fragment != null) {

            TextView backgroundHint = (TextView) this.fragment.findViewById(R.id.detailedBackgroundHint);
            backgroundHint.setText(hintText);
            backgroundHint.setVisibility(TextView.VISIBLE);

        }

    }

}
