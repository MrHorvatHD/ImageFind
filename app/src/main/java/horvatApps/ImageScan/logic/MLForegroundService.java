package horvatApps.ImageScan.logic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import horvatApps.ImageScan.R;
import horvatApps.ImageScan.db.ImageRepository;
import horvatApps.ImageScan.db.models.ImageDetail;

import static horvatApps.ImageScan.db.models.Mapper.imageDetailToImageEntity;

public class MLForegroundService extends Service {

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;

    private Context context;
    private ArrayList<String> allImageFolders;
    private ArrayList<ImageDetail> allImagesSelectedForML;
    ImageRepository imageRepository;


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getApplicationContext();

        this.allImageFolders = intent.getStringArrayListExtra("allImageFolders");
        this.imageRepository = new ImageRepository(getApplication());

        run();

        return START_REDELIVER_INTENT;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
    MAIN RECOGNITION LOGIC
    ----------------------------------------------------------------------------------------------------------
     */

    private void run(){
        allImagesSelectedForML = getImageList();

        builder = new NotificationCompat.Builder(this, "ProgressNotification");
        startForeground(69, createNotification());

        /*int i = 1;
        for(ImageDetail imageForML : allImagesSelectedForML){
            System.out.println("a:" + i);
            i++;
            runTextRecognitionOnImage(imageForML,i);
        }*/

        for (int i = 0; i < allImagesSelectedForML.size(); i++) {
            runTextRecognitionOnImage(allImagesSelectedForML.get(i),i+1);
        }
    }


    private void runTextRecognitionOnImage(final ImageDetail imageDetailObject, final int progress) {
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
                                    updateNotification(progress);
                                    storeToDB(imageDetailObject);
                                    stopServiceWhenDone(progress);
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

    private void stopServiceWhenDone(int progress){
        if(progress == allImagesSelectedForML.size())
            stopSelf();
    }

    /*
    GETTING ALL IMAGES TO PROCESS
    ----------------------------------------------------------------------------------------------------------
     */

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



    /*
    NOTIFICATION BUILDING
    ----------------------------------------------------------------------------------------------------------
     */

    //create notification channel with notificationManager to update notification in the future
    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT < 26) {
            return;
        } else {

            NotificationChannel channel = new NotificationChannel("ProgressNotification", getString(R.string.notificationChannelName), NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.notificationChannelDescription));

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setProgress(100,0,false)
                .setContentTitle(getString(R.string.notificationContentTitle));

        String content = String.format("%s: %d/%d",getString(R.string.notificationContentText),0,allImagesSelectedForML.size());
        builder.setContentText(content);

        return builder.build();
    }

    public void updateNotification(int progress){
        builder.setProgress(allImagesSelectedForML.size(),progress,false);
        String content = String.format("%s: %d/%d",getString(R.string.notificationContentText),progress,allImagesSelectedForML.size());
        builder.setContentText(content);
        notificationManager.notify(69,builder.build());
    }

}
