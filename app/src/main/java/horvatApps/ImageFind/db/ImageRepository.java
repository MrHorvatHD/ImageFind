package horvatApps.ImageFind.db;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import horvatApps.ImageFind.db.models.ImageEntityDB;

public class ImageRepository {

    private ImageDAO imageDAO;

    private LiveData<List<ImageEntityDB>> allImages;
    private MutableLiveData<List<ImageEntityDB>> searchedImages = new MutableLiveData<>();

    //initialisation of the repository
    public ImageRepository(Application application){
        Database db;
        db = Database.getDatabase(application);
        this.imageDAO = db.imageDAO();
        allImages = imageDAO.getAllImages();
    }

    // inserts images to database
    public void insertImage(final ImageEntityDB image) {
        // run query to insert an image on the executor
        Database.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                imageDAO.insertImage(image);
            }
        });
    }

    // retrieves images from database if their scaned text contains param
    public void searchImage(final String param) {

        // run query on the executor, postValue to searchResults
        Database.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                searchedImages.postValue(imageDAO.searchImages(param));
            }
        });
    }

    //deletes all images from database if their uri is in arraylist params
    public void deleteImages(final ArrayList<String> params){
        // run query to delete images on the executor
        Database.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                imageDAO.deleteImages(params);
            }
        });
    }

    //deletes all images from database
    public void deleteALLImages(){
        // run query to delete images on the executor
        Database.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                imageDAO.deleteALLImages();
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
