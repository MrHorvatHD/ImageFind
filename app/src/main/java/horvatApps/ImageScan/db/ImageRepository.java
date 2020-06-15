package horvatApps.ImageScan.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import horvatApps.ImageScan.db.models.ImageEntityDB;

public class ImageRepository {

    private ImageDAO imageDAO;

    private LiveData<List<ImageEntityDB>> allImages;

    public ImageRepository(Application application){
        Database db;
        db = Database.getDatabase(application);
        this.imageDAO = db.imageDAO();
        allImages = imageDAO.getAllImages();
    }

    // inserts images to database
    public void insertImage(final ImageEntityDB image) {
        // run query to insert a product on the executor
        Database.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                //System.out.println("tuki smo");
                imageDAO.insertImage(image);
            }
        });
    }

    public LiveData<List<ImageEntityDB>> getAllImages() {
        return allImages;
    }

}
