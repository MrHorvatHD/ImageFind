package horvatApps.ImageFind.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

import horvatApps.ImageFind.db.models.ImageEntityDB;

@Dao
public interface ImageDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertImage(ImageEntityDB imageEntityDB);

    @Query("SELECT * FROM images")
    LiveData<List<ImageEntityDB>> getAllImages();

    @Query("SELECT * FROM images")
    List<ImageEntityDB> getAllImagesFromDB();

    @Query("SELECT * FROM images WHERE imageText LIKE '%' || :param || '%'")
    List<ImageEntityDB> searchImages(String param);

    @Query("DELETE FROM images WHERE uri in (:uri)")
    void deleteImages(ArrayList<String> uri);

    @Query("DELETE FROM images")
    void deleteALLImages();
}
