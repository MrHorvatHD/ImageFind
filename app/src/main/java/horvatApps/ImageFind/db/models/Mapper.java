package horvatApps.ImageFind.db.models;

import android.net.Uri;

public class Mapper {

    //maps objects from ImageEntitiyDB to imageDetail and vice versa

    public static ImageDetail imageEntityToImageDetail(ImageEntityDB imageEntityDB){
        ImageDetail imageDetail = new ImageDetail(Uri.parse(imageEntityDB.getUri()), imageEntityDB.getThumb(), imageEntityDB.getName());
        imageDetail.setImageText(imageEntityDB.getImageText());
        return imageDetail;
    }

    public static ImageEntityDB imageDetailToImageEntity(ImageDetail imageDetail){
        return new ImageEntityDB(imageDetail.getUri().toString(),imageDetail.getThumb(),imageDetail.getName(),imageDetail.getImageText());
    }

}
