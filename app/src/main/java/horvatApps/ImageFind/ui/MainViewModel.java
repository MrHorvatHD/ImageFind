package horvatApps.ImageFind.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import horvatApps.ImageFind.db.ImageRepository;
import horvatApps.ImageFind.db.models.ImageEntityDB;

public class MainViewModel extends AndroidViewModel {

    private ImageRepository imageRepository;
    private LiveData<List<ImageEntityDB>> allImages;
    private MutableLiveData<List<ImageEntityDB>> searchedImages;

    public MainViewModel(@NonNull Application application) {
        super(application);

        this.imageRepository = new ImageRepository(application);
        this.allImages = imageRepository.getAllImages();
        this.searchedImages = imageRepository.getSearchedImages();
    }

    public LiveData<List<ImageEntityDB>> getAllImages() {
        return allImages;
    }
    public MutableLiveData<List<ImageEntityDB>> getSearchedImages() {
        return searchedImages;
    }

    public void insertImage(ImageEntityDB imageEntityDB){
        imageRepository.insertImage(imageEntityDB);
    }

    public void searchImage(String param){
        imageRepository.searchImage(param);
    }
    public void deleteImages(ArrayList<String> params){
        imageRepository.deleteImages(params);
    }
}
