package horvatApps.ImageFind.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.IOException;

import horvatApps.ImageFind.R;
import horvatApps.ImageFind.db.models.ImageDetail;

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
        toolbar.setTitle(R.string.OcrTextToolbarTitle);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
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

    // sets the image from intent and starts the text recognition process
    public void handleIntent(){
        Intent intent = getIntent();

        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            ImageView image = findViewById(R.id.imageView);
            image.setImageURI(imageUri);

            runTextRecognitionOnImage(imageUri);
        }
    }

    //main Optical Character Recognition logic
    private void runTextRecognitionOnImage(Uri imageUri) {
        InputImage image = null;
        try {
            image = InputImage.fromFilePath(getApplicationContext(), imageUri);

            TextRecognizer recognizer = TextRecognition.getClient();

            recognizer.process(image)
                    .addOnSuccessListener(
                            new OnSuccessListener<Text>() {
                                //on succesful image processing update the the recognised text view and store the data to db
                                @Override
                                public void onSuccess(Text texts) {
                                    //imageDetailObject.setImageText(texts.getText().toLowerCase());
                                    //storeToDB(imageDetailObject);

                                    TextView recognisedText = findViewById(R.id.OCRtextContent);
                                    if(texts.getText().length() > 0)
                                        recognisedText.setText(texts.getText());
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                    e.printStackTrace();
                                }
                            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
