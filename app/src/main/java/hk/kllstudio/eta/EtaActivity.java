package hk.kllstudio.eta;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import hk.kllstudio.eta.adapters.RouteEtaAdapter;
import hk.kllstudio.eta.apiget.Request;
import hk.kllstudio.eta.apiget.responses.ETA;
import hk.kllstudio.eta.apiget.responses.Route;
import hk.kllstudio.eta.apiget.responses.RouteStop;
import hk.kllstudio.eta.apiget.responses.Stop;

public class EtaActivity extends AppCompatActivity implements Observer<List<Stop>>, SwipeRefreshLayout.OnRefreshListener, RouteEtaAdapter.OnItemClickListener {
    RouteEtaAdapter adapter;
    SwipeRefreshLayout refreshLayout;
    String routeNumber;
    String bound;
    String serviceType;

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
        routeNumber = bundle.getString("routeNumber");
        bound = bundle.getString("bound");
        serviceType = bundle.getString("serviceType");
        Executor executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());
        MyApplication application = (MyApplication) getApplication();
        final List<Stop> stops = application.getStops();
        application.observeStops(this, this);
        final RecyclerView recyclerView =findViewById(R.id.etaListRecyclerView);
        final TextView routeNumberTextView = findViewById(R.id.routeNumberTextView);
        final TextView originTextView = findViewById(R.id.originTextView);
        final TextView destTextView = findViewById(R.id.destTextView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        refreshLayout = findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(this);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> dirMap = new HashMap<>();
                    dirMap.put("I", "inbound");
                    dirMap.put("O", "outbound");
                    final Route route = Request.getRoute(routeNumber, dirMap.get(bound), serviceType).getData();
                    final List<RouteStop> routeStops = Request.getRouteStops(routeNumber, dirMap.get(bound), serviceType).getData();
                    final List<ETA> etas = Request.getRouteETA(routeNumber, serviceType).getData();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            routeNumberTextView.setText(route.getRoute());
                            originTextView.setText(route.getOrigTc());
                            destTextView.setText(route.getDestTc());
                            adapter = new RouteEtaAdapter(etas, routeStops, stops);
                            adapter.setOnItemClickListener(EtaActivity.this);
                            recyclerView.setAdapter(adapter);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<ETA> etas = Request.getRouteETA(routeNumber, serviceType).getData();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setEtas(etas);
                            refreshLayout.setRefreshing(false);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        final Stop stop = adapter.getStop(position);
        final RouteStop routeStop = adapter.getRouteStop(position);
        final List<ETA> etas = adapter.getEtas(position);
        new MaterialAlertDialogBuilder(this)
                .setTitle(stop.getNameTc())
                .setItems(new String[]{"讀出到站時間", "把此站新增至收藏", "檢視巴士站位置", "檢視巴士站街景"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                EtaReadout.read(EtaActivity.this, stop.getNameTc(), etas);
                                break;
                            case 2:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://www.google.com/maps/search/?api=1&query=%f,%f", stop.getLatitude(), stop.getLongitude()))));
                                break;
                            case 3:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://www.google.com/maps/@?api=1&map_action=pano&viewpoint=%f,%f", stop.getLatitude(), stop.getLongitude()))));
                                break;
                        }
                    }
                })
                .show();
    }
}