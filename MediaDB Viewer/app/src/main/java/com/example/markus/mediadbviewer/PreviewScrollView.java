package com.example.markus.mediadbviewer;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

/**
 * Created by markus on 10.02.16.
 */
public class PreviewScrollView extends ScrollView{

    private int currentStartX = 0;
    private int startX = 0;
    private int currentDistance = 0;
    private int distance = 0;
    private MainActivity activity;
    private String imdbID;
    private int dimension;
    private boolean preview = false;
    private int width;
    private LinearLayout parent;
    private ListView list;

    public void setActivity(MainActivity activity) {

        this.activity = activity;
        Point size = new Point();
        this.activity.getWindowManager().getDefaultDisplay().getSize(size);
        this.width = size.x;

    }

    public void setPreview() {

        this.preview = true;
        this.parent = (LinearLayout) this.getParent().getParent();
        this.list = (ListView) ((ViewGroup) this.parent.getParent()).findViewById(android.R.id.list);

    }

    public void setImdbID(String imdbID) {

        this.imdbID = imdbID;

    }

    public void setDimension(int dimension) {

        this.dimension = dimension;

    }

    public PreviewScrollView(Context context) {
        super(context);
    }

    public PreviewScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PreviewScrollView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent (MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        Log.d("Dragged", "");
        return super.onDragEvent(event);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (this.preview) {
            int x = (int) event.getX();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = x;
                    currentStartX = x;
                    currentDistance = 0;
                    break;
                case MotionEvent.ACTION_MOVE:
                    currentDistance = currentStartX - x;
                    distance = startX -x;
                    if (currentDistance > 0 && distance > 75) {
                        this.currentStartX = x;
                        /*
                        if (this.parent.getLayoutParams().width == 0) {
                            this.parent.setLayoutParams(new LinearLayout.LayoutParams((this.width / 2), LinearLayout.LayoutParams.MATCH_PARENT));
                        }
                        */
                        //this.parent.getLayoutParams() += currentDistance;
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(this.width/2, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.setMargins(this.distance,0,0,0);
                        this.parent.setLayoutParams(params);
                        this.parent.requestLayout();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (this.distance > (0.45 * this.width)) {
                        MovieDetailedFragment fragment = new MovieDetailedFragment();
                        fragment.setImdbID(this.imdbID);
                        fragment.setDimensions(this.dimension);
                        this.activity.getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, fragment, "movieDetailedFragment").addToBackStack("movieDetailedFragment").commit();
                    }
                    if (parent.getLayoutParams().width != 0) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(this.width/2, LinearLayout.LayoutParams.MATCH_PARENT);
                        parent.setLayoutParams(params);
                        //parent.getLayoutParams().width = this.width/2;
                        parent.requestLayout();
                    }
                    break;
            }
        }

        return true;
    }


}
