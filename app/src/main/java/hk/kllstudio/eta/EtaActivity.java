package hk.kllstudio.eta;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import hk.kllstudio.eta.adapters.KmbRouteEtaAdapter;
import hk.kllstudio.eta.adapters.NwstbusRouteEtaAdapter;
import hk.kllstudio.eta.adapters.RouteEtaAdapter;
import hk.kllstudio.eta.apiget.BaseResponse;
import hk.kllstudio.eta.apiget.kmb.Request;
import hk.kllstudio.eta.apiget.kmb.responses.ETA;
import hk.kllstudio.eta.apiget.kmb.responses.Route;
import hk.kllstudio.eta.apiget.kmb.responses.RouteStop;
import hk.kllstudio.eta.apiget.kmb.responses.Stop;
import hk.kllstudio.eta.room.Bookmark;
import hk.kllstudio.eta.room.BookmarkMethods;
import hk.kllstudio.eta.room.Database;

public class EtaActivity extends AppCompatActivity implements Observer<List<Stop>>, SwipeRefreshLayout.OnRefreshListener, RouteEtaAdapter.OnItemClickListener {
    RouteEtaAdapter adapter;
    SwipeRefreshLayout refreshLayout;
    String routeNumber;
    String bound;
    String serviceType;
    String co;
    TextView lastUpdatedTextView;
    FusedLocationProviderClient locationProviderClient;
    ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eta);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Bundle bundle = getIntent().getExtras();
        co = bundle.getString("co", "KMB");
        routeNumber = bundle.getString("routeNumber");
        bound = bundle.getString("bound");
        serviceType = bundle.getString("serviceType");
        Executor executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());
        MyApplication application = (MyApplication) getApplication();
        final List<Stop> stops = application.getStops();
        application.observeStops(this, this);
        final RecyclerView recyclerView = findViewById(R.id.etaListRecyclerView);
        final TextView routeNumberTextView = findViewById(R.id.routeNumberTextView);
        final TextView originTextView = findViewById(R.id.originTextView);
        final TextView destTextView = findViewById(R.id.destTextView);
        final ImageView imageView = findViewById(R.id.imageView);
        lastUpdatedTextView = findViewById(R.id.lastUpdatedTextView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        refreshLayout = findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setRefreshing(true);
        constraintLayout = findViewById(R.id.mainLayout);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String timeDisplayStyle = preferences.getString("time_display_style", "full_time");
        final boolean useLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                preferences.getBoolean("use_location", false);
        GradientDrawable imageViewDrawable = (GradientDrawable) imageView.getDrawable();
        switch (co) {
            case "KMB":
                imageViewDrawable.setColor(0xFFFF0000);
                break;
            case "CTB":
                imageViewDrawable.setColor(0xFFF4E010);
                break;
            case "NWFB":
                imageViewDrawable.setColor(0xFFFF7F27);
                break;
        }
        if (co.equals("KMB")) {
            executor.execute(() -> {
                try {
                    Map<String, String> dirMap = new HashMap<>();
                    dirMap.put("I", "inbound");
                    dirMap.put("O", "outbound");
                    final Route route = Request.getRoute(routeNumber, dirMap.get(bound), serviceType).getData();
                    final List<RouteStop> routeStops = Request.getRouteStops(routeNumber, dirMap.get(bound), serviceType).getData();
                    final BaseResponse<List<ETA>> response = Request.getRouteETA(routeNumber, serviceType);
                    final List<ETA> etas = response.getData();
                    handler.post(() -> {
                        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.TRADITIONAL_CHINESE);
                        lastUpdatedTextView.setText(format.format(response.getGeneratedTimestamp()));
                        routeNumberTextView.setText(route.getRoute());
                        originTextView.setText(route.getOrigTc());
                        destTextView.setText(route.getDestTc());
                        adapter = new KmbRouteEtaAdapter(etas, routeStops, stops, timeDisplayStyle);
                        if (useLocation) {
                            locationProviderClient = LocationServices.getFusedLocationProviderClient(EtaActivity.this);
                            locationProviderClient.getLastLocation().addOnSuccessListener(EtaActivity.this, location -> {
                                if (location != null) {
                                    adapter.setLocation(location);
                                }
                            });
                        }
                        adapter.setOnItemClickListener(EtaActivity.this);
                        recyclerView.setAdapter(adapter);
                        refreshLayout.setRefreshing(false);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (co.equals("CTB") || co.equals("NWFB")) {
            AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.loading_data)
                    .setView(R.layout.dialog_loading)
                    .setCancelable(false)
                    .show();
            final TextView textView = dialog.findViewById(R.id.textView3);
            final LinearProgressIndicator progress = dialog.findViewById(R.id.progress);
            executor.execute(() -> {
                try {
                    Map<String, String> dirMap = new HashMap<>();
                    dirMap.put("I", "inbound");
                    dirMap.put("O", "outbound");
                    handler.post(() -> {
                        textView.setText(R.string.loading_route_data);
                        progress.setIndeterminate(true);
                    });
                    final hk.kllstudio.eta.apiget.nwstbus.responses.Route route = hk.kllstudio.eta.apiget.nwstbus.Request.getRoute(co, routeNumber).getData();
                    handler.post(() -> {
                        textView.setText(R.string.loading_route_stop_data);
                        progress.setIndeterminate(true);
                    });
                    final List<hk.kllstudio.eta.apiget.nwstbus.responses.RouteStop> routeStops = hk.kllstudio.eta.apiget.nwstbus.Request.getRouteStops(co, routeNumber, dirMap.get(bound)).getData();
                    final List<hk.kllstudio.eta.apiget.nwstbus.responses.ETA> etas = new ArrayList<>();
                    final List<hk.kllstudio.eta.apiget.nwstbus.responses.Stop> stops1 = new ArrayList<>();
                    handler.post(() -> {
                        textView.setText(R.string.loading_stop_eta_data);
                        progress.setIndeterminate(false);
                        progress.setMax(routeStops.size());
                        progress.setProgressCompat(0, true);
                    });
                    Date time = new Date();
                    for (int i = 0, routeStopsSize = routeStops.size(); i < routeStopsSize; i++) {
                        hk.kllstudio.eta.apiget.nwstbus.responses.RouteStop routeStop = routeStops.get(i);
                        int finalI = i;
                        handler.post(() -> {
                            textView.setText(String.format(Locale.TRADITIONAL_CHINESE, getString(R.string.loading_stop_eta_data_f), finalI + 1, routeStopsSize));
                            progress.setIndeterminate(false);
                            progress.setMax(routeStops.size());
                            progress.setProgressCompat(finalI, true);
                        });
                        final hk.kllstudio.eta.apiget.nwstbus.responses.Stop stop = hk.kllstudio.eta.apiget.nwstbus.Request.getStop(routeStop.getStop()).getData();
                        BaseResponse<List<hk.kllstudio.eta.apiget.nwstbus.responses.ETA>> response = hk.kllstudio.eta.apiget.nwstbus.Request.getETA(co, routeStop.getStop(), routeStop.getRoute());
                        final List<hk.kllstudio.eta.apiget.nwstbus.responses.ETA> eta = response.getData();
                        time = response.getGeneratedTimestamp();
                        etas.addAll(eta);
                        stops1.add(stop);
                    }
                    Date finalTime = time;
                    handler.post(() -> {
                        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.TRADITIONAL_CHINESE);
                        lastUpdatedTextView.setText(format.format(finalTime));
                        routeNumberTextView.setText(route.getRoute());
                        if (bound.equals("I")) {
                            originTextView.setText(route.getDestTc());
                            destTextView.setText(route.getOrigTc());
                        } else {
                            originTextView.setText(route.getOrigTc());
                            destTextView.setText(route.getDestTc());
                        }
                        adapter = new NwstbusRouteEtaAdapter(etas, routeStops, stops1, timeDisplayStyle, bound);
                        if (useLocation) {
                            locationProviderClient = LocationServices.getFusedLocationProviderClient(EtaActivity.this);
                            locationProviderClient.getLastLocation().addOnSuccessListener(EtaActivity.this, location -> {
                                if (location != null) {
                                    adapter.setLocation(location);
                                }
                            });
                        }
                        adapter.setOnItemClickListener(EtaActivity.this);
                        recyclerView.setAdapter(adapter);
                        dialog.dismiss();
                        refreshLayout.setRefreshing(false);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.eta_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.menu_refresh) {
            onRefresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onChanged(List<Stop> stops) {
        if (adapter != null && adapter instanceof KmbRouteEtaAdapter) {
            ((KmbRouteEtaAdapter) adapter).setStopList(stops);
        }
    }

    @Override
    public void onRefresh() {
        refreshLayout.setRefreshing(true);
        Executor executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean useLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                preferences.getBoolean("use_location", false);
        if (co.equals("KMB")) {
            executor.execute(() -> {
                try {
                    final BaseResponse<List<ETA>> response = Request.getRouteETA(routeNumber, serviceType);
                    final List<ETA> etas = response.getData();
                    handler.post(() -> {
                        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.TRADITIONAL_CHINESE);
                        lastUpdatedTextView.setText(format.format(response.getGeneratedTimestamp()));
                        ((KmbRouteEtaAdapter) adapter).setEtas(etas);
                        if (useLocation) {
                            locationProviderClient = LocationServices.getFusedLocationProviderClient(EtaActivity.this);
                            locationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationTokenSource().getToken()).addOnSuccessListener(EtaActivity.this, location -> {
                                if (location != null) {
                                    adapter.setLocation(location);
                                } else {
                                    locationProviderClient.getLastLocation().addOnSuccessListener(EtaActivity.this, location1 -> {
                                        if (location1 != null) {
                                            adapter.setLocation(location1);
                                        }
                                    });
                                }
                            });
                        }
                        refreshLayout.setRefreshing(false);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (co.equals("CTB") || co.equals("NWFB")) {
            AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.loading_data)
                    .setView(R.layout.dialog_loading)
                    .setCancelable(false)
                    .show();
            final TextView textView = dialog.findViewById(R.id.textView3);
            final LinearProgressIndicator progress = dialog.findViewById(R.id.progress);
            executor.execute(() -> {
                try {
                    Map<String, String> dirMap = new HashMap<>();
                    dirMap.put("I", "inbound");
                    dirMap.put("O", "outbound");
                    Date time = new Date();
                    final List<hk.kllstudio.eta.apiget.nwstbus.responses.RouteStop> routeStops = hk.kllstudio.eta.apiget.nwstbus.Request.getRouteStops(co, routeNumber, dirMap.get(bound)).getData();
                    final List<hk.kllstudio.eta.apiget.nwstbus.responses.ETA> etas = new ArrayList<>();
                    handler.post(() -> {
                        textView.setText(R.string.loading_eta_data);
                        progress.setIndeterminate(false);
                        progress.setMax(routeStops.size());
                        progress.setProgressCompat(0, true);
                    });
                    for (int i = 0, routeStopsSize = routeStops.size(); i < routeStopsSize; i++) {
                        int finalI = i;
                        hk.kllstudio.eta.apiget.nwstbus.responses.RouteStop routeStop = routeStops.get(i);
                        handler.post(() -> {
                            textView.setText(String.format(Locale.TRADITIONAL_CHINESE, getString(R.string.loading_eta_data_f), finalI + 1, routeStopsSize));
                            progress.setIndeterminate(false);
                            progress.setMax(routeStops.size());
                            progress.setProgressCompat(finalI, true);
                        });
                        BaseResponse<List<hk.kllstudio.eta.apiget.nwstbus.responses.ETA>> response = hk.kllstudio.eta.apiget.nwstbus.Request.getETA(co, routeStop.getStop(), routeStop.getRoute());
                        final List<hk.kllstudio.eta.apiget.nwstbus.responses.ETA> eta = response.getData();
                        time = response.getGeneratedTimestamp();
                        etas.addAll(eta);
                    }
                    Date finalTime = time;
                    handler.post(() -> {
                        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.TRADITIONAL_CHINESE);
                        lastUpdatedTextView.setText(format.format(finalTime));
                        ((NwstbusRouteEtaAdapter) adapter).setEtas(etas);
                        if (useLocation) {
                            locationProviderClient = LocationServices.getFusedLocationProviderClient(EtaActivity.this);
                            locationProviderClient.getLastLocation().addOnSuccessListener(EtaActivity.this, location -> {
                                if (location != null) {
                                    adapter.setLocation(location);
                                }
                            });
                        }
                        dialog.dismiss();
                        refreshLayout.setRefreshing(false);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if (co.equals("KMB")) {
            final Stop stop = ((KmbRouteEtaAdapter) adapter).getStop(position);
            final RouteStop routeStop = ((KmbRouteEtaAdapter) adapter).getRouteStop(position);
            if (stop != null) {
                final List<ETA> etas = ((KmbRouteEtaAdapter) adapter).getEtas(position);
                new MaterialAlertDialogBuilder(this)
                        .setTitle(stop.getNameTc())
                        .setItems(R.array.route_eta_options, (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    EtaReadout.read(EtaActivity.this, stop.getNameTc(), etas);
                                    break;
                                case 1:
                                    Executor executor = Executors.newSingleThreadExecutor();
                                    final Handler handler = new Handler(Looper.getMainLooper());
                                    executor.execute(() -> {
                                        Database database = Room.databaseBuilder(EtaActivity.this, Database.class, "appdata").build();
                                        final BookmarkMethods bookmarkMethods = database.bookmark();
                                        bookmarkMethods.addBookmark(new Bookmark(routeNumber, stop.getStop(), routeStop.getSeq(), routeStop.getServiceType(), routeStop.getBound()));
                                        handler.post(() -> Snackbar.make(constraintLayout, "已加入至收藏。", BaseTransientBottomBar.LENGTH_LONG).show());
                                    });
                                    break;
                                case 2:
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://www.google.com/maps/search/?api=1&query=%f,%f", stop.getLatitude(), stop.getLongitude()))));
                                    break;
                                case 3:
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://www.google.com/maps/@?api=1&map_action=pano&viewpoint=%f,%f", stop.getLatitude(), stop.getLongitude()))));
                                    break;
                            }
                        })
                        .show();
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.error)
                        .setMessage(R.string.stop_not_loaded)
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                        }).show();
            }
        } else if (co.equals("CTB") || co.equals("NWFB")) {
            final hk.kllstudio.eta.apiget.nwstbus.responses.Stop stop = ((NwstbusRouteEtaAdapter) adapter).getStop(position);
            final hk.kllstudio.eta.apiget.nwstbus.responses.RouteStop routeStop = ((NwstbusRouteEtaAdapter) adapter).getRouteStop(position);
            if (stop != null) {
                final List<hk.kllstudio.eta.apiget.nwstbus.responses.ETA> etas = ((NwstbusRouteEtaAdapter) adapter).getEtas(position);
                new MaterialAlertDialogBuilder(this)
                        .setTitle(stop.getNameTc())
                        .setItems(R.array.route_eta_options, (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    EtaReadout.read(EtaActivity.this, stop.getNameTc(), etas);
                                    break;
                                case 1:
                                    /*Executor executor = Executors.newSingleThreadExecutor();
                                    final Handler handler = new Handler(Looper.getMainLooper());
                                    executor.execute(() -> {
                                        Database database = Room.databaseBuilder(EtaActivity.this, Database.class, "appdata").build();
                                        final BookmarkMethods bookmarkMethods = database.bookmark();
                                        bookmarkMethods.addBookmark(new Bookmark(routeNumber, stop.getStop(), routeStop.getSeq(), routeStop.getServiceType(), routeStop.getBound()));
                                        handler.post(() -> Snackbar.make(constraintLayout, "已加入至收藏。", BaseTransientBottomBar.LENGTH_LONG).show());
                                    });*/
                                    break;
                                case 2:
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://www.google.com/maps/search/?api=1&query=%f,%f", stop.getLatitude(), stop.getLongitude()))));
                                    break;
                                case 3:
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://www.google.com/maps/@?api=1&map_action=pano&viewpoint=%f,%f", stop.getLatitude(), stop.getLongitude()))));
                                    break;
                            }
                        })
                        .show();
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.error)
                        .setMessage(R.string.stop_not_loaded)
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                        }).show();
            }
        }
    }
}