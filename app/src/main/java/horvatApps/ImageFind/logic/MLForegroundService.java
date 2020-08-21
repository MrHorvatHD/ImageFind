package horvatApps.ImageFind.logic;

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
import android.os.Handler;
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
import java.util.HashSet;
import java.util.Locale;

import horvatApps.ImageFind.R;
import horvatApps.ImageFind.db.ImageRepository;
import horvatApps.ImageFind.db.models.ImageDetail;
import horvatApps.ImageFind.db.models.ImageEntityDB;
import horvatApps.ImageFind.ui.MainActivity;

import static horvatApps.ImageFind.db.models.Mapper.imageDetailToImageEntity;

public class MLForegroundService extends Service {

    //global instances of notifications
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;

    //global arrays for image processing
    private ArrayList<String> allImageFolders;
    private ArrayList<ImageDetail> allImagesSelectedForML;
    private String command;
    ImageRepository imageRepository;

    //creates an notification channel
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    //proceses the starting intent and runs the main logic
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.allImageFolders = intent.getStringArrayListExtra("allImageFolders");
        this.command = intent.getStringExtra("command");
        this.imageRepository = new ImageRepository(getApplication());

        //start a new thread for OCR
        new Thread(new Runnable() {
            public void run() {
                runMain();
            }
        }).start();

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

    /* Gets all images for processsing
    *  Starts the notification
    *  Starts image procesing one by one
    * */
    private void runMain(){
        allImagesSelectedForML = new ArrayList<>();
        builder = new NotificationCompat.Builder(this, "ProgressNotification");
        startForeground(69, createNotification());

        //either scans the whole folder or finds images from folders that have not been scanned yet
        if(this.command.equals("Scan")) {
            allImagesSelectedForML = getImageList();
            for (int i = 0; i < allImagesSelectedForML.size(); i++) {
                runTextRecognitionOnImage(allImagesSelectedForML.get(i), i + 1);
            }
        }
        else if(this.command.equals("Refresh")){
            //get all images in DB
            HashSet<String> uriInDB = new HashSet<>();
            for(ImageEntityDB imgDB : imageRepository.getAllImagesFromDB())
                uriInDB.add(imgDB.getUri());

            //find all images not yet scanned
            ArrayList<ImageDetail> imagesToCheck = getImageList();
            for(ImageDetail img : imagesToCheck){
                if(!uriInDB.contains(img.getUri().toString())) {
                    allImagesSelectedForML.add(img);
                    //System.out.println(img.getName());
                }
            }

            if(allImagesSelectedForML.size() == 0)
                this.stopSelf();


            //start the ML if at least 1 img found
            for (int i = 0; i < allImagesSelectedForML.size(); i++) {
                runTextRecognitionOnImage(allImagesSelectedForML.get(i), i + 1);
            }
        }
    }

    //main Optical Character Recognition logic
    private void runTextRecognitionOnImage(final ImageDetail imageDetailObject, final int progress) {
        InputImage image = null;
        try {
            image = InputImage.fromFilePath(getApplicationContext(), imageDetailObject.getUri());

            TextRecognizer recognizer = TextRecognition.getClient();

            recognizer.process(image)
                    .addOnSuccessListener(
                            new OnSuccessListener<Text>() {
                                //on succesful image processing update the notification and store the data in db
                                @Override
                                public void onSuccess(Text texts) {
                                    imageDetailObject.setImageText(texts.getText().toLowerCase());
                                    updateNotification(progress);
                                    storeToDB(imageDetailObject);

                                    //lauch function to stop service if processing done
                                    stopServiceWhenDone(progress);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                    e.printStackTrace();
                                    stopSelf();
                                }
                            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //stores the processed data to database
    private void storeToDB(ImageDetail imageDetail){
        imageRepository.insertImage(imageDetailToImageEntity(imageDetail));
    }

    //stops service if processing done
    private void stopServiceWhenDone(int progress){
        if(progress == allImagesSelectedForML.size())
            new Handler().postDelayed(this::stopSelf, 2000);
    }

    /*
    GETTING ALL IMAGES TO PROCESS
    ----------------------------------------------------------------------------------------------------------
     */

    //fetches all images to process
    private ArrayList<ImageDetail> getImageList() {
        ArrayList<ImageDetail> imageDetail = new ArrayList<ImageDetail>();

        //sql select
        String[] projection = {MediaStore.Images.ImageColumns._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        //sql where arguments
        String selectionArgs = allImageFolders.toString();
        selectionArgs = selectionArgs.replace("[","(").replace("]",")");

        //sql where
        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " IN " + selectionArgs;

        //select order by
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

            NotificationChannel channel = new NotificationChannel("ProgressNotification", getString(R.string.notificationChannelName), NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(getString(R.string.notificationChannelDescription));
            channel.setSound(null,null);

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //creates notification that displays the progress of the scan
    private Notification createNotification() {

        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setColor(getColor(R.color.colorPrimary))
                .setOngoing(true)
                .setProgress(100,0,false)
                .setContentTitle(getString(R.string.notificationContentTitle))
                .setOnlyAlertOnce(true);

        String content = String.format(Locale.getDefault(),"%s: %d / %d",getString(R.string.notificationContentText), 0, allImagesSelectedForML.size());
        builder.setContentText(content);

        return builder.build();
    }

    //updates the progress bar and text
    public void updateNotification(int progress){
        builder.setProgress(allImagesSelectedForML.size(),progress,false);
        String content = String.format("%s: %d / %d",getString(R.string.notificationContentText), progress, allImagesSelectedForML.size());
        builder.setContentText(content);
        notificationManager.notify(69, builder.build());
    }

}
