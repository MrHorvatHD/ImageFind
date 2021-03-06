package horvatApps.ImageFind.db;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import horvatApps.ImageFind.db.models.ImageEntityDB;

@androidx.room.Database(entities = {ImageEntityDB.class}, version = 2, exportSchema = false)
public abstract class Database extends RoomDatabase {


    // add a DAO reference
    public abstract ImageDAO imageDAO();

    //initialisation of room database
    private static volatile Database INSTANCE;

    public static Database getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (Database.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            Database.class, "ScanDB")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    //initialisation of database executors for async queryies
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
}