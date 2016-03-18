package com.example.markus.mediadbviewer;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.lang.Math;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;


public class MovieDetailedFragment extends DetailedFragment {

    private String imdbID;
    private String title;
    private MediaObject movie = null;
    private int dimensions;
    private MainActivity activity;
    private JSONParser parser;
    private MyApplication application;
    private LayoutInflater inflater;
    private boolean preview = false;
    private boolean forceReload = false;


    public void setImdbID(String imdbID) {

        this.imdbID = imdbID;

    }

    public void setForceReload() {

        this.forceReload = true;

    }

    public void setDimensions(int dimensions) {

        this.dimensions = dimensions;

    }

    public void setPreview() {

        this.preview = true;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("imdbID", this.imdbID);
        outState.putInt("dimensions", this.dimensions);
        outState.putBoolean("preview", this.preview);
    }

    @Override
    public void updateData() {

        if (this.preview) {
            PreviewScrollView previewScrollView = (PreviewScrollView) getView().findViewById(R.id.movieDetailedScrollView);
            previewScrollView.setPreview();
            previewScrollView.setImdbID(this.imdbID);
            previewScrollView.setDimension(this.dimensions);
            previewScrollView.setActivity(this.activity);
        }

        this.movie = this.parser.getMediaList().get(0);
        this.title = this.movie.simpleValues.get("name");
        this.activity.setTitle(this.title);

        // tausche Default Cover aus

        ImageView cover = (ImageView) fragment.findViewById(R.id.detailedCover);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        if (new File(this.application.coverFolderMoviesHigh + this.imdbID + ".jpg").exists()) {
            BitmapFactory.decodeFile(this.application.coverFolderMoviesHigh + this.imdbID + ".jpg", options);
            cover.setImageDrawable(Drawable.createFromPath(this.application.coverFolderMoviesHigh + this.imdbID + ".jpg"));
        } else if (new File(this.application.coverFolderMoviesHigh + "0000000.jpg").exists()) {
            BitmapFactory.decodeFile(this.application.coverFolderMoviesHigh + "0000000.jpg", options);
            cover.setImageDrawable(Drawable.createFromPath(this.application.coverFolderMoviesHigh + "0000000.jpg"));
        } else {
            BitmapFactory.decodeResource(getResources(), R.drawable.default_cover, options);
            cover.setImageResource(R.drawable.default_cover);
        }
        cover.getLayoutParams().width = (int) (cover.getHeight() / (options.outHeight / (double) options.outWidth));

        // erstelle Tabelle / Titel
        TableLayout table = (TableLayout) this.fragment.findViewById(R.id.movieTable); // hole Tabellenreferenz
        TableRow row;
        TextView secondColumn;

        Iterator<String> iterator = this.application.valuesMovieShown.iterator();
        String value;
        boolean rightsChanged = false;
        while (iterator.hasNext()) {
            value = iterator.next();
            if (!this.movie.simpleValues.keySet().contains(value) && !value.equals("Genre") && !value.equals("Schauspieler")) {
                rightsChanged = true;
                iterator.remove();
            } else {
                row = (TableRow) LayoutInflater.from(this.fragment.getContext()).inflate(R.layout.table_row_text, null); // erstelle neue Zeile
                // wenn Spalte unbekannt, wird Name aus der Datenbank genommen
                if (this.application.detailedListNames.keySet().contains(value)) {
                    ((TextView) row.findViewById(R.id.firstColumn)).setText(this.application.detailedListNames.get(value));
                } else {
                    ((TextView) row.findViewById(R.id.firstColumn)).setText(value);
                }
                secondColumn = (TextView) this.inflater.inflate(R.layout.table_row_textview, null);
                row.addView(secondColumn);
                // füge nicht bei allen Werten eine Zeile in die Tabelle ein
                if (!value.equals("name") && !value.equals("summary") && !value.equals("Schauspieler")) {
                    table.addView(row);
                }
                if (value.equals("name")) {
                    TextView titel = (TextView) this.fragment.findViewById(R.id.movieTitleDetailed);
                    titel.setText(movie.simpleValues.get("name"));
                } else if (value.equals("summary")) {
                    // setze Header Text und onClick Methode
                    LinearLayout content = (LinearLayout) this.fragment.findViewById(R.id.summaryContent);
                    TextView header = (TextView) this.fragment.findViewById(R.id.summaryHeader);
                    TextView summaryText = (TextView) content.findViewById(R.id.summaryText);
                    summaryText.setText(this.movie.simpleValues.get(value));
                    header.setVisibility(TextView.VISIBLE);
                    header.setText("[+] " + this.application.detailedListNames.get(value));
                    header.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            LinearLayout parent = (LinearLayout) v.getParent();
                            LinearLayout content = (LinearLayout) parent.findViewById(R.id.summaryContent);
                            TextView header = (TextView) parent.findViewById(R.id.summaryHeader);

                            if (content.getVisibility() == RelativeLayout.GONE) {
                                header.setText(((String) header.getText()).replace("[+]", "[-]"));
                                content.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                                content.setVisibility(RelativeLayout.VISIBLE);
                                content.invalidate(); // für Android 5 notwendig
                                content.requestLayout(); // für Android 5 notwendig
                            } else {
                                content.setVisibility(RelativeLayout.GONE);
                                header.setText(((String) header.getText()).replace("[-]", "[+]"));
                                content.getLayoutParams().height = 0;
                                content.invalidate(); // für Android 5 notwendig
                                content.requestLayout(); // für Android 5 notwendig
                            }

                        }
                    });
                } else if (value.equals("imdbID")) {
                    secondColumn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            List<Intent> targetBrowserIntents = new ArrayList<Intent>();
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com/title/tt" + movie.simpleValues.get("imdbID")));
                            // entferne diese App als möglichen Viewer für den Intent
                            for (ResolveInfo info : activity.getPackageManager().queryIntentActivities(browserIntent, 0)) {
                                if (!info.activityInfo.packageName.equals(activity.getPackageName())) {
                                    Intent targetBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com/title/tt" + movie.simpleValues.get("imdbID")));
                                    targetBrowserIntent.setPackage(info.activityInfo.packageName);
                                    targetBrowserIntents.add(targetBrowserIntent);
                                }
                            }
                            // erstelle Chooser mit den vorher gefilterten activities für den Intent
                            Intent chooser = Intent.createChooser(targetBrowserIntents.remove(0), getResources().getString(R.string.openIMDBTitle));
                            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetBrowserIntents.toArray(new Parcelable[]{}));
                            startActivity(chooser);
                        }
                    });
                    SpannableString content = new SpannableString(this.movie.simpleValues.get("imdbID"));
                    content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                    secondColumn.setText(content);
                    secondColumn.setTextColor(getResources().getColor(R.color.linkColor));
                } else if (value.equals("youtube")) {
                    if (movie.simpleValues.get("youtube").length() > 0) {
                        SpannableString content = new SpannableString("");

                        if (movie.simpleValues.get("youtube").substring(0, 3).equals("DE:")) {
                            content = new SpannableString("Youtube (deutsch)");

                        } else if (movie.simpleValues.get("youtube").substring(0, 3).equals("EN:")) {
                            content = new SpannableString("Youtube (englisch)");
                        }
                        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + movie.simpleValues.get("youtube").split(":")[1]));
                        secondColumn.setText(content);
                        secondColumn.setTextColor(getResources().getColor(R.color.linkColor));
                        secondColumn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(browserIntent);
                            }
                        });
                    } else {
                        secondColumn.setText("nicht vorhanden");
                    }
                } else if (value.equals("size")) {
                    secondColumn.setText(String.valueOf(Math.round(Long.valueOf(movie.simpleValues.get("size")) * 100 / Math.pow(1024, 3)) / 100.) + " GB");
                } else if (value.equals("duration")) {
                    secondColumn.setText(String.valueOf(Integer.valueOf(movie.simpleValues.get("duration")) / 60) + " Minuten");
                } else if (value.equals("width") || value.equals("height")) {
                    secondColumn.setText(movie.simpleValues.get(value) + " Pixel");
                } else if (value.contains("bitrate")) {
                    // verhindert, dass Einheit angezeigt wird, wenn keine Tonspur existiert
                    if (movie.simpleValues.get(value).length() > 0) {
                        secondColumn.setText(movie.simpleValues.get(value) + " kBit/s");
                    } else {
                        secondColumn.setText("");
                    }
                } else if (value.equals("rating")) {
                    secondColumn.setText(movie.simpleValues.get("rating") + " / 10");
                } else if (value.equals("checked")) {
                    if (movie.simpleValues.get("checked").length() == 0) {
                        secondColumn.setText("nicht geprüft");
                    } else if (movie.simpleValues.get("checked").equals("0")) {
                        secondColumn.setText("fehlerhaft");
                        //secondColumn.setTextColor(ContextCompat.getColor(getActivity(), R.color.red));
                        secondColumn.setTextColor(getResources().getColor(R.color.red));
                    } else if (movie.simpleValues.get("checked").equals("1")) {
                        secondColumn.setText("fehlerfrei");
                        //secondColumn.setTextColor(ContextCompat.getColor(getActivity(), R.color.green));
                        secondColumn.setTextColor(getResources().getColor(R.color.green));
                    }
                } else if (value.equals("Genre")) {
                    String text = "";
                    boolean firstLoop = true;
                    for (String genre : movie.Genre) {
                        if (!firstLoop) {
                            text += "\r\n";
                        }
                        text += genre;
                        firstLoop = false;
                    }
                    secondColumn.setText(text);
                } else if (value.equals("Schauspieler")) {
                    // setze Header Text und onClick Methode
                    LinearLayout content = (LinearLayout) this.fragment.findViewById(R.id.actorContent);
                    TextView header = (TextView) this.fragment.findViewById(R.id.actorHeader);
                    TableLayout actorTable = (TableLayout) this.fragment.findViewById(R.id.actorTable);
                    header.setVisibility(TextView.VISIBLE);
                    header.setText("[+] " + this.application.detailedListNames.get(value));
                    header.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            LinearLayout parent = (LinearLayout) v.getParent();
                            LinearLayout content = (LinearLayout) parent.findViewById(R.id.actorContent);
                            TextView header = (TextView) parent.findViewById(R.id.actorHeader);

                            if (content.getVisibility() == RelativeLayout.GONE) {
                                header.setText(((String) header.getText()).replace("[+]", "[-]"));
                                content.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                                content.setVisibility(RelativeLayout.VISIBLE);
                                content.invalidate(); // für Android 5 notwendig
                                content.requestLayout(); // für Android 5 notwendig
                            } else {
                                content.setVisibility(RelativeLayout.GONE);
                                header.setText(((String) header.getText()).replace("[-]", "[+]"));
                                content.getLayoutParams().height = 0;
                                content.invalidate(); // für Android 5 notwendig
                                content.requestLayout(); // für Android 5 notwendig
                            }

                        }
                    });
                    boolean oddRow = true;
                    for (final String schauspieler : movie.schauspieler.keySet()) {

                        // erstelle Zeile für den aktuellen Schauspieler
                        TableRow actorRow = (TableRow) LayoutInflater.from(this.fragment.getContext()).inflate(R.layout.table_row_text, null); // erstelle neue Zeile
                        TextView actorFirstColumn = (TextView) actorRow.findViewById(R.id.firstColumn);
                        TextView actorSecondColumn = (TextView) LayoutInflater.from(this.fragment.getContext()).inflate(R.layout.table_row_textview, null); // erstelle zweite Spalte
                        actorRow.addView(actorSecondColumn);

                        // setze Hintergrundfarbe der aktuellen Zeile
                        if (oddRow) {
                            actorRow.setBackgroundColor(getResources().getColor(R.color.tableRowBackgroundDark));
                        } else {
                            actorRow.setBackgroundColor(getResources().getColor(R.color.tableRowBackgroundLight));
                        }
                        oddRow = !oddRow; // invertiere Farbe für nächsten Durchlauf

                        // setze Daten für den Schauspieler

                        SpannableString actorName = new SpannableString(schauspieler);
                        actorName.setSpan(new UnderlineSpan(), 0, actorName.length(), 0);
                        actorFirstColumn.setText(actorName);
                        actorFirstColumn.setTextColor(getResources().getColor(R.color.linkColor));
                        // öffnet Liste mit Filmen des Schauspielers
                        actorFirstColumn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                // erstelle Link mir Factory
                                MovieQueryFactory factory = new MovieQueryFactory(application.preferences);
                                String condition = "";
                                // Escape Leerzeichen und Sonderzeichen
                                try{ condition = URLEncoder.encode(schauspieler, "utf-8");} catch (Exception e) {}
                                factory.addConditions(new String[]{"Schauspieler=" + condition});
                                factory.addColumns(new String[]{"name", "year", "rating", "imdbID", "3d", "checked", "views", "duration"});
                                factory.setOrder("name", "ASC");

                                // erstelle Fragment
                                MovieListFragment fragment = new MovieListFragment();
                                fragment.hideFilter();
                                fragment.setURL(factory.getUrl());

                                // aktiviere Fragment
                                FragmentManager fragmentManager = ((MainActivity) getContext()).getSupportFragmentManager();
                                fragmentManager.beginTransaction().replace(R.id.mainContent, fragment, "movieListFragmentActor").addToBackStack("movieListFragmentActor").commit();

                            }
                        });

                        actorSecondColumn.setText(movie.schauspieler.get(schauspieler)); // Rollenname

                        // füge Zeile zur Schauspieler-Tabelle hinzu
                        actorTable.addView(actorRow);
                    }

                } else {
                    secondColumn.setText(movie.simpleValues.get(value));
                }

            }

        }

        if (rightsChanged) {
            // speichere neue verfügbare Spalten
            this.application.saveArrayList("valuesMovieShown", this.application.valuesMovieShown);
        }

        // setze Button-Funktion
        Button openUpdateButton = (Button) this.fragment.findViewById(R.id.detailedFragmentUpdateButton);

        if (this.application.preferences.getBoolean("updateRight", false)) {
            openUpdateButton.setVisibility(Button.VISIBLE);
            openUpdateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    UpdateDialogFragment updateFragment = new UpdateDialogFragment();
                    updateFragment.setMediaObject(movie);
                    updateFragment.show(getFragmentManager(), "updateDialogFragment");

                }

            });
        } else {
            ((ViewGroup) openUpdateButton.getParent()).removeView(openUpdateButton);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState != null) {

            this.imdbID = savedInstanceState.getString("imdbID");
            this.dimensions = savedInstanceState.getInt("dimensions");
            this.preview = savedInstanceState.getBoolean("preview");

        }


        if (this.fragment == null) {

            this.inflater = inflater;
            this.activity = (MainActivity) getActivity();

            // lade entsprechendes Layout
            if ((this.activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) && !this.preview) {
                this.fragment = inflater.inflate(R.layout.detailed_fragment_landscape, container, false);
            } else {
                this.fragment = inflater.inflate(R.layout.detailed_fragment, container, false); //wendet layout an
            }

            this.fragment.setBackgroundColor(getResources().getColor(R.color.background));

            // wird nicht aufgeführt, wenn setMovieObject ausgeführt wurde
            if (this.movie == null) {
                // erstelle Link mit MovieQuerryFactory
                MovieQueryFactory movieQueryFactory = new MovieQueryFactory(this.application.preferences);
                movieQueryFactory.addColumns(this.application.valuesMovieShown.toArray(new String[0]));
                movieQueryFactory.addConditions(new String[]{"imdbID=" + this.imdbID, "3d=" + String.valueOf(this.dimensions)});
                this.parser = new JSONParser(this.activity, this.application);
                this.parser.setFragment(this);
                if (this.forceReload) {
                    this.parser.setForcReload();
                }
                this.parser.execute(new String[]{movieQueryFactory.getUrl()});
            }

        }

        return fragment;
    }

    @Override
    public void onPause() {
        super.onStop();
        ((MainActivity) getActivity()).setShareIconVisibility(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(this.title);

        ((MainActivity) getActivity()).setShareIconVisibility(true);
        // setze den Intent für den ShareButton
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "http://imdb.com/title/tt" + this.imdbID);
        sendIntent.setType("text/plain");
        ((MainActivity) getActivity()).setShareIntent(sendIntent);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.application = (MyApplication) getActivity().getApplication();
    }
}

