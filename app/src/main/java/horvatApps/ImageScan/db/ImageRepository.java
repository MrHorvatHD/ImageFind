package horvatApps.ImageScan.db;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import horvatApps.ImageScan.db.models.ImageEntityDB;

public class ImageRepository {

    private ImageDAO imageDAO;

    private LiveData<List<ImageEntityDB>> allImages;
    private MutableLiveData<List<ImageEntityDB>> searchedImages = new MutableLiveData<>();

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

    // retrieves recipe from database
    public void searchImage(final String param) {

        // run query on the executor, postValue to searchResults
        Database.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                searchedImages.postValue(imageDAO.searchImages(param));
            }
        });
    }

    public void deleteImages(final ArrayList<String> params){
        Database.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                imageDAO.deleteImages(params);
            }
        });
    }

    public LiveData<List<ImageEntityDB>> getAllImages() {
        return allImages;
    }
    public MutableLiveData<List<ImageEntityDB>> getSearchedImages(){
        return searchedImages;
    }
}
