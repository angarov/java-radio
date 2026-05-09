package il.co.radio.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public abstract class StationDao {

    @Query("SELECT * FROM stations WHERE sourceUrl = :sourceUrl ORDER BY position ASC")
    public abstract List<StationEntity> getStationsBySource(String sourceUrl);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(List<StationEntity> stations);

    @Query("DELETE FROM stations WHERE sourceUrl = :sourceUrl")
    public abstract void deleteBySource(String sourceUrl);

    @Transaction
    public void replaceStations(String sourceUrl, List<StationEntity> stations) {
        deleteBySource(sourceUrl);
        insertAll(stations);
    }
}
