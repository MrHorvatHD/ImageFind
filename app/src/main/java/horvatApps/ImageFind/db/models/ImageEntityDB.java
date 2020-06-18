package horvatApps.ImageFind.db.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "images", indices = @Index(value = {"uri"}, unique = true))
public class ImageEntityDB {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

    @ColumnInfo(name = "uri")
    private String uri;

    @ColumnInfo(name = "thumb")
    private String thumb;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "imageText")
    private String imageText;

    @Ignore
    public ImageEntityDB(){}

    public ImageEntityDB(String uri, String thumb, String name, String imageText){
        this.uri = uri;
        this.thumb = thumb;
        this.name = name;
        this.imageText = imageText;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageText() {
        return imageText;
    }

    public String getThumb() {
        return thumb;
    }

    public String getUri() {
        return uri;
    }

    public void setImageText(String imageText) {
        this.imageText = imageText;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
