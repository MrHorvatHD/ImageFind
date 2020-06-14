package horvatApps.ImageScan.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.HashSet;

import horvatApps.ImageScan.Adapters.RecyclerViewAdapter;
import horvatApps.ImageScan.R;
import horvatApps.ImageScan.db.ImageDetail;
import horvatApps.ImageScan.db.ImageFolder;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private EditText searchField;

    private ArrayList<ImageDetail> imageList;
    private ArrayList<ImageDetail> searchedImages;
    private ArrayList<String> imageFolders;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUiElements();

    }

    @Override
    protected void onResume() {
        super.onResume();
        permissionHandle();
    }

    public void initUiElements() {
        appBarLayout = findViewById(R.id.app_bar_layout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        searchField = findViewById(R.id.editText);
        searchField.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                if(s.toString().length()>0)
                    updateRecycler(s.toString());
                else{
                    searchedImages.clear();
                    searchedImages.addAll(imageList);
                    recyclerViewAdapter.notifyDataSetChanged();
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        recyclerView = findViewById(R.id.recyclerImages);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewAdapter = new RecyclerViewAdapter(this, new ArrayList<ImageDetail>());
        recyclerView.setAdapter(recyclerViewAdapter);
    }


    public void updateRecycler(String searchText){
        searchedImages.clear();

        System.out.println(searchText);

        for(ImageDetail img : imageList){
            if(img.getName().contains(searchText))
                searchedImages.add(img);
        }

        System.out.println(searchedImages.toString());
        recyclerViewAdapter.notifyDataSetChanged();

    }

    /*
    GET ALL SCANNED IMAGES
    ----------------------------------------------------------------------------------------------------------
     */

    private void readImages() {
        imageList = getImageList();
        searchedImages = new ArrayList<ImageDetail>(imageList);
        recyclerViewAdapter = new RecyclerViewAdapter(this, searchedImages);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private ArrayList<ImageDetail> getImageList() {
        ArrayList<ImageDetail> imageDetail = new ArrayList<ImageDetail>();

        String[] projection = {MediaStore.Images.ImageColumns._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        String selectionArgs = imageFolders.toString();
        selectionArgs = selectionArgs.replace("[","(").replace("]",")");

        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " IN " + selectionArgs;

        String sortOrder = MediaStore.Images.Media.DISPLAY_NAME + " ASC";


        try (Cursor cursor = this.getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, selection, null, sortOrder)) {

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            while (cursor.moveToNext()) {
                // Get values of columns for a given image.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                String thumb = cursor.getString(dataColumn);
                String bucket = cursor.getString(bucketColumn);

                System.out.println(bucket);

                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);


                // Stores column values and the contentUri in a local object
                // that represents the media file.
                imageDetail.add(new ImageDetail(contentUri, thumb , name));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageDetail;
    }



    public void onClickSettings(View v){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

    }

    /*
    HANDLE SHARED PREFFERENCES
    ----------------------------------------------------------------------------------------------------------
     */

    public void handleSharedPref(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ImageScanPref", 0);
        boolean scanned = sharedPref.getBoolean("Scanned", false);
        if(!scanned){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else{

            HashSet<String> selectedFolders = new HashSet<>();

            imageFolders = new ArrayList<String>(sharedPref.getStringSet("Folders", new HashSet<String>()));

            readImages();
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

