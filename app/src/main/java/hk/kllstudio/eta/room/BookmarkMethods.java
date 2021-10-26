package hk.kllstudio.eta.room;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface BookmarkMethods {
    @Insert
    ListenableFuture<Long> addBookmarkAsync(Bookmark bookmark);

    @Insert
    long addBookmark(Bookmark bookmark);

    @Delete
    ListenableFuture<Integer> deleteBookmarkAsync(Bookmark bookmark);

    @Delete
    int deleteBookmark(Bookmark bookmark);

    @Query("select * from bookmark")
    ListenableFuture<List<Bookmark>> getBookmarksAsync();

    @Query("select * from bookmark")
    List<Bookmark> getBookmarks();

    @Query("select * from bookmark")
    LiveData<List<Bookmark>> observeBookmarks();

    @Query("select * from bookmark where id = :id")
    ListenableFuture<Bookmark> getBookmarkAsync(int id);

    @Query("select * from bookmark where id = :id")
    Bookmark getBookmark(int id);

    @Query("select * from bookmark where route = :route")
    ListenableFuture<List<Bookmark>> getBookmarksByRouteAsync(String route);

    @Query("select * from bookmark where stop = :stop")
    ListenableFuture<List<Bookmark>> getBookmarksByStopAsync(String stop);

    @Query("select * from bookmark where route = :route")
    List<Bookmark> getBookmarksByRoute(String route);

    @Query("select * from bookmark where stop = :stop")
    List<Bookmark> getBookmarksByStop(String stop);
}
