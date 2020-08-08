package horvatApps.ImageFind.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.AppBarLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import horvatApps.ImageFind.R;
import horvatApps.ImageFind.logic.MLForegroundService;

public class ScanActivity extends AppCompatActivity {

    TextView lastScanTime;

    //builds the view for the activity and initialises UI element
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        darkModeHandle();

        initUiElements();
    }

    @Override
    protected void onResume() {
        super.onResume();

        darkModeHandle();
    }

     /*
    UI
    ----------------------------------------------------------------------------------------------------------
     */

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

    //initialises UI elements
    public void initUiElements() {
        //setup the toolbar
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout2);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        toolbar.setTitle(R.string.toolbarScan);
        setSupportActionBar(toolbar);

        lastScanTime = findViewById(R.id.lastScanText);
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ImageScanPref", 0);
        lastScanTime.setText(String.format("%s %s", getString(R.string.lastScan), sharedPref.getString("LastScan", "never")));

        //only displays back arrow if scan was already performed
        if (getSupportActionBar() != null && !sharedPref.getString("LastScan", "never").equals("never")) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    //handle click on new scan button
    public void newScan(View v) {
        permissionHandle();
    }

    /*
    HANDLING OPTIONS MENU CREATION
    ----------------------------------------------------------------------------------------------------------
    */

    //only displays the menu if scan was already performed
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu2, menu);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ImageScanPref", 0);
        String lastScanTime = sharedPref.getString("LastScan", "never");

        return !lastScanTime.equals("never");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dropdown_menu_clear:
                deleteALLImagesAlert();
                break;
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //starts the delete process and updates shared preferences
    public void handleDelete() {
        MainViewModel mainViewModel = new MainViewModel(getApplication());
        mainViewModel.deleteALLImages();

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ImageScanPref", 0);

        //updates shared preferences
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("LastScan", "never");
        editor.apply();

        //updates UI
        lastScanTime.setText(getString(R.string.lastScanNever));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        invalidateOptionsMenu();
    }

    //dialog to comfirm clearing of all images from database
    public void deleteALLImagesAlert() {
        AlertDialog.Builder deleteALLImagesDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        deleteALLImagesDialog.setTitle(R.string.clearCacheTitle);
        deleteALLImagesDialog.setMessage(getString(R.string.clearCacheText));

        deleteALLImagesDialog.setPositiveButton(R.string.clearCacheConfirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleDelete();
            }
        });
        deleteALLImagesDialog.setNegativeButton(R.string.clearCacheDeny, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        deleteALLImagesDialog.show();
    }

    /*
    FOLDER SELECTOR
    ----------------------------------------------------------------------------------------------------------
    */

    //builds the folder selector
    private AlertDialog dialogA;

    public void buildFolderSelector() {

        //gets all image folder on device an converts
        ArrayList<String> allImageFolders = getImageFolders();
        final String[] foldersFound = allImageFolders.toArray(new String[allImageFolders.size()]);

        //text replacement for better displaying
        for (int i = 0; i < foldersFound.length; i++) {
            foldersFound[i] = foldersFound[i].replace("'", "");
        }
        final boolean[] checkedItems = new boolean[foldersFound.length];

        //builds the selector
        AlertDialog.Builder folderSelectorBuilder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        folderSelectorBuilder.setTitle(R.string.scanDialogTitle);
        folderSelectorBuilder.setMultiChoiceItems(foldersFound, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                //disables scan button if no elements selected
                dissablePositiveButton(checkedItems);
            }
        });
        folderSelectorBuilder.setPositiveButton(R.string.scanDialogConfirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //stores last scan date in shared prefernces
                storeSharedPref();

                //starts scanning service
                startService(foldersFound, checkedItems);
            }
        });
        folderSelectorBuilder.setNeutralButton(R.string.scanDialogCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialogA = folderSelectorBuilder.create();

        //disables scan button on start
        dialogA.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });
        dialogA.show();


    }

    //dissables scan button if no folders selected
    public void dissablePositiveButton(boolean[] checkedItems) {

        if (allFalse(checkedItems))
            dialogA.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        else
            dialogA.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
    }

    //checks if no folders are selected
    public boolean allFalse(boolean[] checked) {
        for (boolean bool : checked)
            if (bool) return false;

        return true;
    }

    /*
    OCR SERVICE INITIALISATION
    ----------------------------------------------------------------------------------------------------------
     */

    //starts the OCR service
    public void startService(String[] foldersFound, boolean[] checked) {

        //builds the list of all folders to scan
        ArrayList<String> selectedFolders = new ArrayList<String>();
        for (int i = 0; i < foldersFound.length; i++) {
            if (checked[i])
                selectedFolders.add("'" + foldersFound[i] + "'");
        }

        //starts the foreground service
        Intent intent = new Intent(this, MLForegroundService.class);
        intent.putExtra("allImageFolders", selectedFolders);
        startService(intent);
    }


    /*
    SHARED PREFFERENCES
    ----------------------------------------------------------------------------------------------------------
     */

    //stores last scan time in shared preferences
    public void storeSharedPref() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ImageScanPref", 0);

        //formats last scan time
        SimpleDateFormat s = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String lastScanTimeText = s.format(new Date());

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("LastScan", lastScanTimeText);
        editor.apply();

        //update UI
        lastScanTime.setText(String.format("%s %s", getString(R.string.lastScan), lastScanTimeText));

        //handler to update UI after 2000 ms
        new Handler().postDelayed(() -> {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            invalidateOptionsMenu();
        }, 2000);


    }

    /*
    READING ALL FOLDERS
    ----------------------------------------------------------------------------------------------------------
     */

    //gets all available image folders on device
    private ArrayList<String> getImageFolders() {
        ArrayList<String> allFolders = new ArrayList<String>();

        String[] projection = new String[]{MediaStore.MediaColumns.BUCKET_DISPLAY_NAME};

        String sortOrder = MediaStore.MediaColumns.BUCKET_DISPLAY_NAME + " ASC";


        try (Cursor cursor = this.getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, sortOrder)) {

            //int bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME);
            int bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME);

            //required sorting of folders because DISTINCT keyword doesn't work anymore
            HashSet<String> allFoldersHS = new HashSet();
            while (cursor.moveToNext()) {

                String bucket = cursor.getString(bucketColumn);

                allFoldersHS.add(bucket);
            }

            for (String folder : allFoldersHS)
                allFolders.add("'" + folder + "'");
            Collections.sort(allFolders);

        } catch (Exception e) {
            e.printStackTrace();

        }
        return allFolders;
    }
    /*private ArrayList<String> getImageFolders() {
        ArrayList<String> allFolders = new ArrayList<String>();

        String[] projection = {"DISTINCT " + MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        String sortOrder = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC";

        try (Cursor cursor = this.getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, sortOrder)) {

            int bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            while (cursor.moveToNext()) {

                String bucket = cursor.getString(bucketColumn);

                allFolders.add("'" + bucket + "'");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return allFolders;
    }*/


    /*
    RATIONALE DIALOG
    ----------------------------------------------------------------------------------------------------------
     */

    //displays rationale if storage permission not granted
    public void buildRationale() {
        AlertDialog.Builder rationaleDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        rationaleDialog.setTitle(R.string.permissionRationaleTitle);
        rationaleDialog.setMessage(getString(R.string.permissionRationale));

        rationaleDialog.setPositiveButton(R.string.permissionAllow, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String[] premissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(premissions, 420);
            }
        });
        rationaleDialog.setNegativeButton(R.string.permissionDeny, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        rationaleDialog.show();
    }

    //displays alert if storage permission not granted and set to "don't ask again"
    public void buildDeniedDialog() {
        AlertDialog.Builder deniedDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        deniedDialog.setTitle(R.string.permissionDeniedTitle);
        deniedDialog.setMessage(getString(R.string.permissionDeniedExplanation));

        deniedDialog.setPositiveButton(R.string.permissionDeniedAcknowledge, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        deniedDialog.show();
    }



    /*
    PERMISSIONS
    ----------------------------------------------------------------------------------------------------------
     */

    public void permissionHandle() {
        //if permission granted proceed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {

            //builds folder selector
            buildFolderSelector();

        }
        //if permission not granted check if app should show rationale dialog
        else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            buildRationale();

        }
        //request permission for reading external storage
        else {
            String[] premissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(premissions, 420);
        }
    }

    //handles result of permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 420:
                // If request is cancelled, the result arrays are empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // If permission is granted build the folder selector
                    buildFolderSelector();
                }
                // If permission is not granted build the dialog that explains why its necessary
                else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    buildDeniedDialog();
                }
        }
    }


}
