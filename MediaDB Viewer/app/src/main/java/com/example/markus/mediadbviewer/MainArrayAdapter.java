package com.example.markus.mediadbviewer;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainArrayAdapter extends ArrayAdapter<MediaObject>{

    ArrayList<MediaObject> mediaList;
    Context context;

    public MainArrayAdapter (Context context, List mediaList) {

        super(context, -1, mediaList);
        this.mediaList = (ArrayList<MediaObject>) mediaList;
        this.context = context;

    }

    public void changeData(ArrayList<MediaObject> mediaList) {

        this.mediaList = mediaList;
        this.clear();

        if (mediaList != null){
            for (MediaObject media : mediaList) {
                this.insert(media, this.getCount());
            }
        }
        this.notifyDataSetChanged();

    }

}
