package il.co.radio.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.util.List;

@Entity(tableName = "stations", primaryKeys = {"sourceUrl", "position"})
public class StationEntity {

    @NonNull
    @ColumnInfo(name = "sourceUrl")
    public String sourceUrl;

    @ColumnInfo(name = "position")
    public int position;

    public List<String> feed;
    public List<String> img;
    public List<String> name;
    public List<String> nowplaying;
    public List<String> maincolor;
}
