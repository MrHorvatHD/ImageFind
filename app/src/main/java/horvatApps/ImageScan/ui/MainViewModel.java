package horvatApps.ImageScan.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import horvatApps.ImageScan.db.ImageRepository;
import horvatApps.ImageScan.db.models.ImageEntityDB;

public class MainViewModel extends AndroidViewModel {

    private ImageRepository imageRepository;
    private LiveData<List<ImageEntityDB>> allImages;

    public MainViewModel(@NonNull Application application) {
        super(application);

        this.imageRepository = new ImageRepository(application);
        this.allImages = imageRepository.getAllImages();
    }

    public LiveData<List<ImageEntityDB>> getAllImages() {
        return allImages;
    }

    public void insertImage(ImageEntityDB imageEntityDB){
        imageRepository.insertImage(imageEntityDB);
    }
}
