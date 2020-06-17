package horvatApps.ImageScan.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.HashSet;

import horvatApps.ImageScan.R;
import horvatApps.ImageScan.logic.MLForegroundService;
import horvatApps.ImageScan.logic.MLService;

public class ScanActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private Spinner spinner;
    private ArrayList<String> allImageFolders;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        initUiElements();
    }

    public void initUiElements() {
        appBarLayout = findViewById(R.id.app_bar_layout2);
        toolbar = findViewById(R.id.toolbar2);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

    }

    public void newScan(View v){
        permissionHandle();
    }

    /*
    FOLDER SELECTOR
    ----------------------------------------------------------------------------------------------------------
     */
    private AlertDialog dialogA;
    public void buildSelector() {

        allImageFolders = getImageFolders();
        final String[] foldersFound = allImageFolders.toArray(new String[allImageFolders.size()]);
        for (int i = 0; i < foldersFound.length; i++) {
            foldersFound[i] = foldersFound[i].replace("'","");
        }
        final boolean[] checkedItems = new boolean[foldersFound.length];


        AlertDialog.Builder folderSelectorBuilder = new AlertDialog.Builder(this);
        folderSelectorBuilder.setTitle(R.string.scanDialogTitle);
        folderSelectorBuilder.setMultiChoiceItems(foldersFound, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                dissablePositiveButton(checkedItems);
            }
        });
        folderSelectorBuilder.setPositiveButton(R.string.scanDialogConfirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //storeSharedPref(foldersFound,checkedItems);
                testService(foldersFound,checkedItems);
            }
        });
        folderSelectorBuilder.setNeutralButton(R.string.scanDialogCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialogA = folderSelectorBuilder.create();

        dialogA.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //if(condition)
                    ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });
        dialogA.show();


    }


    public void dissablePositiveButton(boolean[] checkedItems){

        if(allFalse(checkedItems))
            dialogA.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        else
            dialogA.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
    }

    public boolean allFalse(boolean[] checked){
        for(boolean bool : checked)
            if(bool) return false;

        return true;
    }


    public void testService(String[] foldersFound, boolean[] checked){
        ArrayList<String> selectedFolders = new ArrayList<String>();
        for (int i = 0; i < foldersFound.length; i++) {
            if(checked[i])
                selectedFolders.add("'" + foldersFound[i] + "'");
        }

        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

        Intent intent = new Intent(this, MLForegroundService.class);
        intent.putExtra("allImageFolders", selectedFolders);
        startService(intent);
    }


    /*
    SHARED PREFFERENCES
    ----------------------------------------------------------------------------------------------------------
     */

    public void storeSharedPref(String[] foldersFound, boolean[] checked) {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ImageScanPref", 0);


        HashSet<String> selectedFolders = new HashSet<String>();
        for (int i = 0; i < foldersFound.length; i++) {
            if(checked[i])
                selectedFolders.add("'" + foldersFound[i] + "'");
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("Folders", selectedFolders);
        editor.putBoolean("Scanned", true);
        editor.apply();
    }

    /*
    READING ALL FOLDERS
    ----------------------------------------------------------------------------------------------------------
     */

    private ArrayList<String> getImageFolders() {
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
            buildSelector();

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
                    buildSelector();
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
