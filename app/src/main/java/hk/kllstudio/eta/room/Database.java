package hk.kllstudio.eta.room;

import androidx.room.RoomDatabase;

@androidx.room.Database(entities = {Bookmark.class}, version = 1)
public abstract class Database extends RoomDatabase {
    public abstract BookmarkMethods bookmark();
}
