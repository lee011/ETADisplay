package hk.kllstudio.eta.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import hk.kllstudio.eta.EtaActivity;
import hk.kllstudio.eta.EtaReadout;
import hk.kllstudio.eta.MyApplication;
import hk.kllstudio.eta.R;
import hk.kllstudio.eta.adapters.BookmarkedEtaAdapter;
import hk.kllstudio.eta.apiget.kmb.Request;
import hk.kllstudio.eta.apiget.kmb.responses.ETA;
import hk.kllstudio.eta.apiget.kmb.responses.Route;
import hk.kllstudio.eta.apiget.kmb.responses.Stop;
import hk.kllstudio.eta.room.Bookmark;
import hk.kllstudio.eta.room.BookmarkMethods;
import hk.kllstudio.eta.room.Database;

public class HomeFragment extends Fragment implements Observer<List<Stop>>, SwipeRefreshLayout.OnRefreshListener, BookmarkedEtaAdapter.OnItemClickListener {
    BookmarkedEtaAdapter adapter;
    SwipeRefreshLayout refreshLayout;
    FusedLocationProviderClient locationProviderClient;
    ConstraintLayout constraintLayout;
    RecyclerView recyclerView;
    private Timer timer;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.textView2);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日, EEEE HH:mm:ss", Locale.TRADITIONAL_CHINESE);
                if (isAdded()) {
                    getActivity().runOnUiThread(() -> textView.setText(String.format("現在的時間是 %s。", format.format(new Date()))));
                }
            }
        }, 0, 500);
        refreshLayout = root.findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(this);
        constraintLayout = root.findViewById(R.id.mainLayout);
        recyclerView = root.findViewById(R.id.bookmarkedEtaListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        Executor executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());
        final MyApplication application = (MyApplication) getActivity().getApplication();
        application.fetchStopInfo();
        final List<Stop> stops = application.getStops();
        application.observeStops(getViewLifecycleOwner(), this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String timeDisplayStyle = preferences.getString("time_display_style", "full_time");
        final boolean useLocation = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                preferences.getBoolean("use_location", false);
        executor.execute(() -> {
            Map<String, String> dirMap = new HashMap<>();
            dirMap.put("I", "inbound");
            dirMap.put("O", "outbound");
            Database database = Room.databaseBuilder(getContext(), Database.class, "appdata").build();
            final BookmarkMethods bookmarkMethods = database.bookmark();
            final List<Bookmark> bookmarks = bookmarkMethods.getBookmarks();
            final List<ETA> etas = new ArrayList<>();
            final List<Route> routes = new ArrayList<>();
            for (Bookmark bookmark : bookmarks) {
                try {
                    etas.addAll(Request.getETA(bookmark.getStop(), bookmark.getRoute(), bookmark.getServiceType()).getData());
                    routes.add(Request.getRoute(bookmark.getRoute(), dirMap.get(bookmark.getDir()), bookmark.getServiceType()).getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            handler.post(() -> {
                adapter = new BookmarkedEtaAdapter(bookmarks, routes, etas, stops, timeDisplayStyle);
                adapter.setOnItemClickListener(HomeFragment.this);
                if (useLocation) {
                    locationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
                    locationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
                        if (location != null) {
                            adapter.setLocation(location);
                        }
                    });
                }
                recyclerView.setAdapter(adapter);
            });

        });
        return root;
    }

    @Override
    public void onChanged(List<Stop> stops) {
        if (adapter != null) {
            adapter.setStopList(stops);
        }
    }

    @Override
    public void onRefresh() {
        refreshLayout.setRefreshing(true);
        Executor executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final boolean useLocation = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                preferences.getBoolean("use_location", false);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> dirMap = new HashMap<>();
                dirMap.put("I", "inbound");
                dirMap.put("O", "outbound");
                Database database = Room.databaseBuilder(getContext(), Database.class, "appdata").build();
                final BookmarkMethods bookmarkMethods = database.bookmark();
                final List<Bookmark> bookmarks = bookmarkMethods.getBookmarks();
                final List<ETA> etas = new ArrayList<>();
                final List<Route> routes = new ArrayList<>();
                for (Bookmark bookmark : bookmarks) {
                    try {
                        etas.addAll(Request.getETA(bookmark.getStop(), bookmark.getRoute(), bookmark.getServiceType()).getData());
                        routes.add(Request.getRoute(bookmark.getRoute(), dirMap.get(bookmark.getDir()), bookmark.getServiceType()).getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                handler.post(() -> {
                    adapter.setEtas(etas);
                    adapter.setRoutes(routes);
                    if (useLocation) {
                        locationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
                        locationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationTokenSource().getToken()).addOnSuccessListener(getActivity(), location -> {
                            if (location != null) {
                                adapter.setLocation(location);
                            } else {
                                locationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), location1 -> {
                                    if (location1 != null) {
                                        adapter.setLocation(location1);
                                    }
                                });
                            }
                        });
                    } else {
                        adapter.setLocation(null);
                    }
                    refreshLayout.setRefreshing(false);
                });
            }
        });
    }

    @Override
    public void onItemClick(View view, final int position) {
        final Stop stop = adapter.getStop(position);
        final Route route = adapter.getRoute(position);
        final Bookmark bookmark = adapter.getBookmark(position);
        if (stop != null && bookmark != null && route != null) {
            final List<ETA> etas = adapter.getEtas(position);
            new MaterialAlertDialogBuilder(getContext())
                    .setTitle(String.format("%s 線 往 %s：%s", route.getRoute(), route.getDestTc(), stop.getNameTc()))
                    .setItems(new String[]{"讀出到站時間", "檢視路線到站時間", "檢視巴士站位置", "檢視巴士站街景", "移除收藏"}, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                EtaReadout.read(getContext(), stop.getNameTc(), etas);
                                break;
                            case 1:
                                Intent intent = new Intent(getContext(), EtaActivity.class);
                                intent.putExtra("routeNumber", bookmark.getRoute())
                                        .putExtra("bound", bookmark.getDir())
                                        .putExtra("serviceType", bookmark.getServiceType());
                                startActivity(intent);
                                break;
                            case 2:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://www.google.com/maps/search/?api=1&query=%f,%f", stop.getLatitude(), stop.getLongitude()))));
                                break;
                            case 3:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://www.google.com/maps/@?api=1&map_action=pano&viewpoint=%f,%f", stop.getLatitude(), stop.getLongitude()))));
                                break;
                            case 4:
                                Executor executor = Executors.newSingleThreadExecutor();
                                final Handler handler = new Handler(Looper.getMainLooper());
                                executor.execute(() -> {
                                    Database database = Room.databaseBuilder(getContext(), Database.class, "appdata").build();
                                    final BookmarkMethods bookmarkMethods = database.bookmark();
                                    bookmarkMethods.deleteBookmark(bookmark);
                                    handler.post(() -> {
                                        adapter.removeBookmark(position);
                                        Snackbar.make(constraintLayout, "已從收藏移除。", BaseTransientBottomBar.LENGTH_LONG).show();
                                    });
                                });
                                break;
                        }
                    })
                    .show();
        }
    }
}