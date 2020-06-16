package horvatApps.ImageScan.logic;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;



import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.ArrayList;

import horvatApps.ImageScan.db.ImageRepository;
import horvatApps.ImageScan.db.models.ImageDetail;

import static horvatApps.ImageScan.db.models.Mapper.imageDetailToImageEntity;

public class MLService extends IntentService {

    private Context context;
    private ArrayList<String> allImageFolders;
    private ArrayList<ImageDetail> allImagesSelectedForML;
    ImageRepository imageRepository;

    public MLService(){
        super("MLService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        this.allImageFolders = intent.getStringArrayListExtra("allImageFolders");
        this.imageRepository = new ImageRepository(getApplication());

        run();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Konec");
    }

    private void run(){
        allImagesSelectedForML = getImageList();

        int i = 1;
        for(ImageDetail imageForML : allImagesSelectedForML){
            System.out.println("a:" + i);
            i++;
            runTextRecognitionOnImage(imageForML,i);
        }
    }


    private void runTextRecognitionOnImage(final ImageDetail imageDetailObject, final int i) {
        InputImage image = null;
        try {
            image = InputImage.fromFilePath(getApplicationContext(), imageDetailObject.getUri());

            TextRecognizer recognizer = TextRecognition.getClient();

            recognizer.process(image)
                    .addOnSuccessListener(
                            new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text texts) {
                                    imageDetailObject.setImageText(texts.getText().toLowerCase());
                                    storeToDB(imageDetailObject);
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

    private void storeToDB(ImageDetail imageDetail){
        imageRepository.insertImage(imageDetailToImageEntity(imageDetail));
    }

    private ArrayList<ImageDetail> getImageList() {
        ArrayList<ImageDetail> imageDetail = new ArrayList<ImageDetail>();

        String[] projection = {MediaStore.Images.ImageColumns._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        String selectionArgs = allImageFolders.toString();
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




}
