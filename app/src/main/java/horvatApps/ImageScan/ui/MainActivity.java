package horvatApps.ImageScan.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import horvatApps.ImageScan.Adapters.RecyclerViewAdapter;
import horvatApps.ImageScan.R;
import horvatApps.ImageScan.db.models.ImageDetail;
import horvatApps.ImageScan.db.models.ImageEntityDB;
import horvatApps.ImageScan.db.models.Mapper;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;

    private ArrayList<ImageDetail> imageList;
    private ArrayList<ImageDetail> searchedImages;
    private ArrayList<String> imageFolders;
    private ArrayList<ImageDetail> imagesFromDB = new ArrayList<ImageDetail>();

    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUiElements();
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        observerSetup();

    }

    @Override
    protected void onResume() {
        super.onResume();
        permissionHandle();
    }

    /*
    HANDLING OPTIONS MENU CREATION
    ----------------------------------------------------------------------------------------------------------
     */

    private SearchView searchView;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.toolbar_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(false);

        recyclerViewAdapter.notifyDataSetChanged();

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.dropdown_menu_scan:
                Intent intent = new Intent(this, ScanActivity.class);
                startActivity(intent);
                return true;

            case R.id.dropdown_menu_about:
                Toast.makeText(this, "About", Toast.LENGTH_LONG).show();
                return true;

            case R.id.dropdown_menu_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    UI ELEMENTS INITIALISATION
    ----------------------------------------------------------------------------------------------------------
     */


    public void initUiElements() {
        appBarLayout = findViewById(R.id.app_bar_layout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerImages);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerViewAdapter = new RecyclerViewAdapter(this, imagesFromDB);
        recyclerView.setAdapter(recyclerViewAdapter);
    }


    /*
    DB OBSERVERS INITIALISATION
    ----------------------------------------------------------------------------------------------------------
     */

    public void observerSetup() {
        mainViewModel.getAllImages().observe(this, new Observer<List<ImageEntityDB>>() {

            @Override
            public void onChanged(List<ImageEntityDB> imageEntityDBS) {
                int queryLength = 0;
                try {
                    queryLength = searchView.getQuery().length();
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                if( queryLength == 0)
                    try {
                        imagesFromDB.clear();
                        for (ImageEntityDB imageEntityDB : imageEntityDBS) {
                            imagesFromDB.add(Mapper.imageEntityToImageDetail(imageEntityDB));
                        }

                        clearGoneImages();
                        recyclerViewAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });

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
    GET ALL SCANNED IMAGES
    ----------------------------------------------------------------------------------------------------------
     */

    private void clearGoneImages() {
        ArrayList<String> allImageUris = getAllImageUrisOnDevice();
        ArrayList<String> imagesToDelete = new ArrayList<String>();

        for(ImageDetail imageDetail : imagesFromDB){
            if(!allImageUris.contains(imageDetail.getUri().toString()))
                imagesToDelete.add(imageDetail.getUri().toString());
        }

        if(imagesToDelete.size() > 0)
            mainViewModel.deleteImages(imagesToDelete);
    }

    private ArrayList<String> getAllImageUrisOnDevice() {
        ArrayList<String> imageUris = new ArrayList<String>();

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
        boolean scanned = sharedPref.getBoolean("Scanned", false);
        if (!scanned) {
            /*Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);*/
        } else {

            HashSet<String> selectedFolders = new HashSet<>();

            imageFolders = new ArrayList<String>(sharedPref.getStringSet("Folders", new HashSet<String>()));

            //readImages();
        }
    }


    /*
    PERMISSIONS
    ----------------------------------------------------------------------------------------------------------
     */

    public void permissionHandle() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show();
            handleSharedPref();

        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            Toast.makeText(this, "rationale", Toast.LENGTH_LONG).show();

        } else {
            String[] premissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(premissions, 420);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 420:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    Toast.makeText(this, "111", Toast.LENGTH_LONG).show();
                    handleSharedPref();
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Toast.makeText(this, "222", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

}

