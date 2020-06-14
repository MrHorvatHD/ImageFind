package horvatApps.ImageScan.db;

import android.net.Uri;

public class ImageDetail {
    private Uri uri;
    private String thumb;
    private String name;


    public ImageDetail(Uri uri, String thumb ,String name){
        this.uri = uri;
        this.thumb = thumb;
        this.name = name;
    }

    public Uri getUri(){
        return this.uri;
    }

    public String getThumb(){
        return this.thumb;
    }

    public String getName(){
        return this.name;
    }
}
