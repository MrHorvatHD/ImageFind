package horvatApps.ImageFind.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.android.material.appbar.AppBarLayout;

import horvatApps.ImageFind.R;

public class ShareIntentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_intent);

        initUI();
        handleIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();

        darkModeHandle();
    }

    //setup the toolbar
    public void initUI(){
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
    }

    private void darkModeHandle(){
        //sets night mode to folow system settings on android pie and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //matches navigation bar with background
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.mainBackground));
    }

    public void handleIntent(){
        Intent intent = getIntent();

        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            ImageView image = findViewById(R.id.imageView);
            image.setImageURI(imageUri);
        }
    }
}
