package horvatApps.ImageScan.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

import horvatApps.ImageScan.db.models.ImageEntityDB;

@Dao
public interface ImageDAO {

    @Insert
    void insertImage(ImageEntityDB imageEntityDB);

    @Query("SELECT * FROM images")
    LiveData<List<ImageEntityDB>> getAllImages();
}
