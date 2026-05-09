package il.co.radio.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {StationEntity.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class RadioDatabase extends RoomDatabase {

    private static volatile RadioDatabase INSTANCE;

    public abstract StationDao stationDao();

    public static RadioDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RadioDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            RadioDatabase.class,
                            "radio_database"
                    ).fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
