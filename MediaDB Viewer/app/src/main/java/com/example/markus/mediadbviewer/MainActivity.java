package com.example.markus.mediadbviewer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MyApplication application;
    public PopupWindow popup;
    private ShareActionProvider mShareActionProvider;

    public void closeMovieFilter() {

        this.application.mDrawerLayout.closeDrawer(this.application.filterLayout);

    }

    public void openMovieFilter() {

        this.application.mDrawerLayout.openDrawer(this.application.filterLayout);

    }

    public void setShareIconVisibility(boolean visible) {

        if (this.application.menu != null) {
            ((MenuItem) this.application.menu.findItem(R.id.menu_item_share)).setVisible(visible);
        }

    }

    public void setMovieFilterVisible() {

        this.application.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, this.application.filterLayout);
        if (this.application.menu != null) {
            ((MenuItem) this.application.menu.findItem(R.id.action_search)).setVisible(true);
        }

    }

    public void setMovieFilterInvisible() {

        this.application.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, this.application.filterLayout);
        // beim ersten Aufruf existiert Menu noch nicht
        if (this.application.menu != null) {
            ((MenuItem) this.application.menu.findItem(R.id.action_search)).setVisible(false);
        }
    }

    public boolean settingsMissing() {

        // prüfe ob notwendige Einstellungen gesetzt sind
        boolean missing = false;
        int message = 0;

        if (this.application.preferences.getString("server", "").length() < 8 && this.application.preferences.getString("apikey", "").length() != 10) {
            missing = true;
            message = R.string.serverAndApiKeyMissingText;
        } else if (this.application.preferences.getString("server", "").length() < 8) {
            missing = true;
            message = R.string.serverMissingText;
        } else if (this.application.preferences.getString("apikey", "").equals("removed")) {
            missing = true;
            message = R.string.apiKeyInvalidatedText;
            this.application.preferences.edit().putString("apikey", "").commit();
        } else if (this.application.preferences.getString("apikey", "").length() != 10) {
            missing = true;
            message = R.string.apiKeyMissingText;
        }

        if (missing) {
            SettingsFragment fragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, fragment, "settingsFragment").addToBackStack("settingsFragment").commit();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AlertDialog dialog = builder.setMessage(message).setTitle(R.string.settingsMissingTitle).create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        }

        return missing;

    }

    public void restartSettings() {

        getSupportFragmentManager().popBackStack("settingsFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        SettingsFragment fragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, fragment, "settingsFragment").addToBackStack("settingsFragment").commit();

    }

    // Funktion bei Auswahl eines Sidebar-Eintrags
    private void selectItemFromDrawer(int position) {

        FragmentManager fragmentManager = getSupportFragmentManager(); //Manager zum Verwalten der Fragmente
        // schließe alle offenen Fragmente
        for (int i=fragmentManager.getBackStackEntryCount(); i>0; i--) {
            fragmentManager.popBackStack();
        }
        this.application.mDrawerLayout.closeDrawer(this.application.navigationRootLayout); // Schließen der Sidebar

        // zerstöre Preview Fagment, falls es existiert
        Fragment movieDetailedFragmentPreview = getSupportFragmentManager().findFragmentByTag("movieDetailedFragmentPreview");
        if (movieDetailedFragmentPreview != null) {
            getSupportFragmentManager().beginTransaction().remove(movieDetailedFragmentPreview).commit();
        }

        // Element aus Navigation Drawer wurde ausgewählt
        if (!settingsMissing()) {
            if (this.application.navigationIcons.get(position).mTitle.equals("Filme")) {
                MovieListFragment fragment = new MovieListFragment(); // eigenes Fragment initialisieren
                fragmentManager.beginTransaction().replace(R.id.mainContent, fragment, "movieListFragment").addToBackStack("movieListFragment").commit(); //Fragment aktivieren
            } else if (this.application.navigationIcons.get(position).mTitle.equals("Serien")) {
                SeriesListFragment fragment = new SeriesListFragment(); // eigenes Fragment initialisieren
                fragmentManager.beginTransaction().replace(R.id.mainContent, fragment, "seriesListFragment").addToBackStack("seriesListFragment").commit(); //Fragment aktivieren
            } else if (this.application.navigationIcons.get(position).mTitle.equals("Einstellungen")) {
                SettingsFragment fragment = new SettingsFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, fragment, "settingsFragment").addToBackStack("settingsFragment").commit();
            } else if (this.application.navigationIcons.get(position).mTitle.equals("Statistik")) {
                StatisticFragment fragment = new StatisticFragment();
                fragmentManager.beginTransaction().replace(R.id.mainContent, fragment, "statisticFragment").addToBackStack("statisticFragment").commit();
            } else if (this.application.navigationIcons.get(position).mTitle.equals("Home")) {
                HomeFragment fragment = new HomeFragment();
                fragmentManager.beginTransaction().replace(R.id.mainContent, fragment, "homeFragment").addToBackStack("homeFragment").commit();
            }
        }

        this.application.navigationListView.setItemChecked(position, true); //Sidebar Eintrag als ausgewählt markieren
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.application = (MyApplication) getApplication();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hintergrundfarbe der Status Leiste setzen
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        // aktivierer Menü-Knopf oben links in der Ecke, um in die Sidebar zu kommen
        this.application.drawerRootLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        this.application.toggle = new ActionBarDrawerToggle(this, this.application.drawerRootLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        this.application.drawerRootLayout.setDrawerListener(this.application.toggle);
        this.application.toggle.syncState();

        // verhindert den Fehler, dass doppelte Einträge im Menü erscheinen
        if (this.application.navigationIcons.size() == 0) {

            this.application.navigationIcons.add(new NavigationbarIcon("Home", "Startbildschirm", R.drawable.ic_home));
            this.application.navigationIcons.add(new NavigationbarIcon("Filme", "Übersicht aller Filme", R.drawable.ic_movies));
            this.application.navigationIcons.add(new NavigationbarIcon("Serien", "Übersicht aller Serien", R.drawable.ic_series));
            this.application.navigationIcons.add(new NavigationbarIcon("Statistik", "Media Statistiken", R.drawable.ic_statistics));
            this.application.navigationIcons.add(new NavigationbarIcon("Einstellungen", "Konfiguration", R.drawable.ic_settings));

        }

        // linkes Slidermenu
        // DrawerLayout
        this.application.mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        // Populate the Navigtion Drawer with options
        this.application.navigationRootLayout = (NavigationView) findViewById(R.id.drawerPane);
        this.application.navigationLayout = (LinearLayout) findViewById(R.id.navigationLayout);
        this.application.navigationListView = (ListView) findViewById(R.id.navList);
        DrawerListAdapter adapter = new DrawerListAdapter(this, this.application.navigationIcons);
        this.application.navigationListView.setAdapter(adapter);

        // Drawer Item click listeners
        this.application.navigationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemFromDrawer(position); //führt Funktion zur Auswahl eines Sidebar-Eintrags aus
            }
        });

        // rechter Slider
        this.application.filterLayout = (LinearLayout) findViewById(R.id.filterLayout);
        this.application.initializeFilterBar(this);

        // verstecke Filtermenü
        this.application.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, this.application.filterLayout);
        // beim ersten Aufruf existiert Menu noch nicht
        if (this.application.menu != null) {
            ((MenuItem) this.application.menu.findItem(R.id.action_search)).setVisible(false);
        }

        // erstelle Home Fragment oder movieDetailedFragment, wenn ein Link aufgerufen wurde
        if (savedInstanceState == null) {
            boolean movieFound = false;
            if (!settingsMissing()) {
                // hole Intent, falls app über Link geöffnet wurde
                Uri intentContent = getIntent().getData();
                if (intentContent != null) {
                    List<String> params = intentContent.getPathSegments();
                    for (String segment : params) {
                        if (segment.substring(0, 2).equals("tt")) {
                            // suche imdbID in der URL
                            String imdbID = segment.substring(2, 9);
                            Log.d("DEBUG", "MainActivity: found this imdbID: " + imdbID);

                            //suche Film in 2D
                            // erstelle Link mit MovieQuerryFactory
                            int dimension = 0;
                            MovieQueryFactory movieQueryFactory = new MovieQueryFactory(this.application.preferences);
                            movieQueryFactory.addColumns(this.application.valuesMovieShown.toArray(new String[0]));
                            movieQueryFactory.addConditions(new String[]{"imdbID=" + imdbID, "3d=0"});
                            JSONParser parser = new JSONParser(this, this.application);
                            try {
                                parser.execute(new String[]{movieQueryFactory.getUrl()}).get();
                            } catch (Exception e) {
                            }

                            // versuche 3D Film zu holen, falls 2D nicht existiert
                            if (parser.getMediaList().size() == 0) {
                                dimension = 1;
                                movieQueryFactory = new MovieQueryFactory(this.application.preferences);
                                movieQueryFactory.addColumns(this.application.valuesMovieShown.toArray(new String[0]));
                                movieQueryFactory.addConditions(new String[]{"imdbID=" + imdbID, "3d=1"});
                                parser = new JSONParser(this, this.application);
                                try {
                                    parser.execute(new String[]{movieQueryFactory.getUrl()}).get();
                                } catch (Exception e) {
                                }
                            }

                            // erstelle MovieDetailedFragment, wenn Film in 2D oder 3D gefunden wurde
                            if (parser.getMediaList().size() != 0) {
                                movieFound = true;
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                MovieDetailedFragment movieDetailedFragment = new MovieDetailedFragment();
                                movieDetailedFragment.setImdbID(imdbID);
                                movieDetailedFragment.setDimensions(dimension);
                                fragmentManager.beginTransaction().replace(R.id.mainContent, movieDetailedFragment, "movieDetailedFragment").addToBackStack("movieDetailedFragment").commit();
                            } else {
                                // gibt Fehlerdialog aus, wenn Film nicht vorhanden
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                AlertDialog dialog = builder.setMessage(R.string.movieNotFoundMessage).setTitle(R.string.movieNotFoundTitle).create();
                                dialog.setCanceledOnTouchOutside(true);
                                dialog.show();
                            }
                        }
                    }

                    if (!movieFound) {
                        HomeFragment homeFragment = new HomeFragment();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.mainContent, homeFragment, "homeFragment").addToBackStack("homeFragment").commit();
                    }

                } else { // App wurde ohne Intent geöffnet

                    HomeFragment homeFragment = new HomeFragment();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.mainContent, homeFragment, "homeFragment").addToBackStack("homeFragment").commit();

                    // prüfe im Hintergrund auf Updates
                    UpdateChecker checker = new UpdateChecker();
                    checker.execute(new MainActivity[]{this});

                }

            }

        }

        // prüfe Schreibrechte auf Speicherkarte für Android6
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        this.application.restoreFilterSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.application.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName().equals("movieListFragment")) {
            ((MenuItem) this.application.menu.findItem(R.id.action_search)).setVisible(true);
        }

        // initialisiere den Share Button
        MenuItem shareIcon = (MenuItem) menu.findItem(R.id.menu_item_share);
        this.mShareActionProvider = new ShareActionProvider(this);
        MenuItemCompat.setActionProvider(shareIcon, this.mShareActionProvider);
        if (getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName().equals("movieDetailedFragment")) {
            this.setShareIconVisibility(true);
        }
        return true;
    }

    // setzt den Content der geteilt werden soll
    public void setShareIntent(Intent shareIntent) {
        if (this.mShareActionProvider != null) {
            this.mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            this.openMovieFilter();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        String itemToClose = "";
        try {
            itemToClose = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        } catch (Exception e) {
            //java.lang.ArrayIndexOutOfBoundsException trifft auf, wenn zu schnell hintereinandern gedrückt
            Log.d("EXCEPTION", "MainActivity: " + e.getClass().getName());
        }

        if (this.application.mDrawerLayout.isDrawerOpen(this.application.navigationRootLayout)) {
            this.application.mDrawerLayout.closeDrawer(this.application.navigationRootLayout);
        } else if (this.popup != null) {
            this.popup.dismiss();
            this.popup = null;
        }else {
            if (itemToClose.equals("homeFragment")) {
                finish();
                System.exit(0);
            } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                getSupportFragmentManager().popBackStack();
                // zerstöre Preview Fagment, falls es existiert
                Fragment movieDetailedFragmentPreview = getSupportFragmentManager().findFragmentByTag("movieDetailedFragmentPreview");
                if (movieDetailedFragmentPreview != null) {
                    getSupportFragmentManager().beginTransaction().remove(movieDetailedFragmentPreview).commit();
                }
                if (!settingsMissing()) {
                    Fragment homeFragment = new HomeFragment();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.mainContent, homeFragment, "homeFragment").addToBackStack("homeFragment").commit();
                }
            } else if (!itemToClose.equals("")) {
                getSupportFragmentManager().popBackStack();
            }

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
