package horvatApps.ImageFind.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import horvatApps.ImageFind.Adapters.RecyclerViewAdapter;
import horvatApps.ImageFind.R;
import horvatApps.ImageFind.db.models.ImageDetail;
import horvatApps.ImageFind.db.models.ImageEntityDB;
import horvatApps.ImageFind.db.models.Mapper;
import horvatApps.ImageFind.logic.MLForegroundService;

public class MainActivity extends AppCompatActivity{

    private RecyclerViewAdapter recyclerViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<ImageDetail> imagesFromDB = new ArrayList<ImageDetail>();

    private MainViewModel mainViewModel;
    private boolean redirected = false;

    @Override
    //initialises the viewModel, UI elements and checks if any scans were already performed
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        darkModeHandle();

        handleSharedPref();

        initUiElements();
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

    }

    private void darkModeHandle() {
        //sets night mode to folow system settings on android pie and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //matches navigation bar with background
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.mainBackground));
    }

    //handle dark mode and observers
    @Override
    protected void onResume() {
        super.onResume();

        observerSetup();
        lastUpdateTime = 0;

        recyclerViewAdapter.notifyDataSetChanged();

        darkModeHandle();
    }

    /*
    HANDLING OPTIONS MENU CREATION
    ----------------------------------------------------------------------------------------------------------
     */

    private SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //create the toolbar menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);

        //create the search bar in the toolbar menu
        MenuItem searchItem = menu.findItem(R.id.toolbar_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(false);

        recyclerViewAdapter.notifyDataSetChanged();

        //handle searching in the search bar
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                mainViewModel.searchImage(text.toLowerCase());
                return false;
            }
        });
        return true;
    }

    private long lastRefreshTime = 0;
    //handles clicking on icons from the toolbar menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dropdown_menu_scan:
                Intent intentScan = new Intent(this, ScanActivity.class);
                startActivity(intentScan);
                return true;

            case R.id.dropdown_menu_about:
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                return true;

            case R.id.dropdown_menu_instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                return true;

            case R.id.dropdown_menu_refresh:
                //start refresh only if 20 sec from and ML service not already running
                if(!isServiceRunning(this,MLForegroundService.class) && System.currentTimeMillis() - lastRefreshTime >= 20000) {
                    checkForNewImages();
                    lastRefreshTime = System.currentTimeMillis();
                }
                else
                    Toast.makeText(this, getString(R.string.toastScanAlert), Toast.LENGTH_LONG).show();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    UI ELEMENTS INITIALISATION
    ----------------------------------------------------------------------------------------------------------
     */


    public void initUiElements() {
        //setup the toolbar
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        //init of swipe refresh
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if(!isServiceRunning(this,MLForegroundService.class) && System.currentTimeMillis() - lastRefreshTime >= 20000) {
                checkForNewImages();
                lastRefreshTime = System.currentTimeMillis();
            }
            else
                Toast.makeText(this, getString(R.string.toastScanAlert), Toast.LENGTH_LONG).show();

            swipeRefreshLayout.setRefreshing(false);
        });


        RecyclerView recyclerView = findViewById(R.id.recyclerImages);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerViewAdapter = new RecyclerViewAdapter(this, imagesFromDB);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    /*
    DB OBSERVERS INITIALISATION
    ----------------------------------------------------------------------------------------------------------
     */

    //initialises observers for live data
    long lastUpdateTime = 0;
    public void observerSetup() {
        //handle all data live updates in recycler view if not using the search bar
        mainViewModel.getAllImages().observe(this, new Observer<List<ImageEntityDB>>() {

            @Override
            public void onChanged(List<ImageEntityDB> imageEntityDBS) {
                int queryLength = 0;
                try {
                    queryLength = searchView.getQuery().length();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //updates Recycler view only every 1 sec
                if (lastUpdateTime == 0 || queryLength == 0 && System.currentTimeMillis() - lastUpdateTime >= 1000)
                    try {
                        imagesFromDB.clear();
                        for (ImageEntityDB imageEntityDB : imageEntityDBS) {
                            imagesFromDB.add(Mapper.imageEntityToImageDetail(imageEntityDB));
                        }

                        //clears deleted images from database
                        clearGoneImages();

                        recyclerViewAdapter.notifyDataSetChanged();
                        lastUpdateTime = System.currentTimeMillis();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });

        //handle search bar live updates in recycler view
        mainViewModel.getSearchedImages().observe(this, new Observer<List<ImageEntityDB>>() {
            @Override
            public void onChanged(List<ImageEntityDB> imageEntityDBS) {

                try {
                    imagesFromDB.clear();
                    for (ImageEntityDB imageEntityDB : imageEntityDBS) {
                        imagesFromDB.add(Mapper.imageEntityToImageDetail(imageEntityDB));
                    }

                    recyclerViewAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /*
    HANDLES REMOVED IMAGES FROM DEVICE AND REFFRESHING OF IMAGES IN FOLDERS
    ----------------------------------------------------------------------------------------------------------
     */

    //removes all images stored in the database that are not on device anymore
    private void clearGoneImages() {
        //check for files acces permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            ArrayList<String> allImageUris = getAllImageUrisOnDevice();
            ArrayList<String> imagesToDelete = new ArrayList<String>();

            for (ImageDetail imageDetail : imagesFromDB) {
                if (!allImageUris.contains(imageDetail.getUri().toString()))
                    imagesToDelete.add(imageDetail.getUri().toString());
            }

            if (imagesToDelete.size() > 0)
                mainViewModel.deleteImages(imagesToDelete);
        }
    }

    private void checkForNewImages(){
        //check for files acces permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ImageScanPref", 0);
            String scannedFolders = sharedPref.getString("ScannedFolders", "NoFoldersFoundPlsWork");

            if(scannedFolders.equals("NoFoldersFoundPlsWork"))
                return;

            String[] foldersToCheck = scannedFolders.split(",");
            ArrayList<String> allImageFolders = new ArrayList<>(Arrays.asList(foldersToCheck));

            if (allImageFolders.size() > 0) {
                //clear search view
                Toolbar toolbar = findViewById(R.id.toolbar);
                toolbar.collapseActionView();

                //toast that it's refreshing
                Toast.makeText(this, getString(R.string.toastRefreshStart), Toast.LENGTH_LONG).show();

                //start ML service
                Intent intent = new Intent(this, MLForegroundService.class);
                intent.putExtra("allImageFolders", allImageFolders);
                intent.putExtra("command", "Refresh");
                startService(intent);
            }
        }
    }


    //fetches uri of all images on device
    private ArrayList<String> getAllImageUrisOnDevice() {
        ArrayList<String> imageUris = new ArrayList<String>();

        //sql select
        String[] projection = {MediaStore.Images.ImageColumns._ID};

        try (Cursor cursor = this.getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null)) {

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

            while (cursor.moveToNext()) {
                // Get values of columns for a given image.
                long id = cursor.getLong(idColumn);

                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                imageUris.add(contentUri.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageUris;
    }


    /*
    HANDLE SHARED PREFFERENCES
    ----------------------------------------------------------------------------------------------------------
     */

    public void handleSharedPref() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ImageScanPref", 0);
        String lastScanTime = sharedPref.getString("LastScan", "never");
        boolean isFirst = sharedPref.getBoolean("isFirst", true);

        //if the instructions guide hasn't already been shown redirect to it
        if (isFirst) {
            //starts instructions activity on a new stack
            Intent intent = new Intent(this, InstructionsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        //checks shared prefferences for the date of last scan
        else if (lastScanTime.equals("never") && !redirected) {
            redirected = true;

            //if scan not preformed yet and redirect hasn't happened yet, redirect to scan activity
            Intent intent = new Intent(this, ScanActivity.class);
            startActivity(intent);
        }
    }

    /*
    CHECKS IF SERVICE IS RUNNING
    ----------------------------------------------------------------------------------------------------------
     */

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return service.foreground;
            }
        }
        return false;
    }

}

