package hk.kllstudio.eta.adapters;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import hk.kllstudio.eta.DistanceCalculation;
import hk.kllstudio.eta.R;
import hk.kllstudio.eta.apiget.nwstbus.responses.ETA;
import hk.kllstudio.eta.apiget.nwstbus.responses.RouteStop;
import hk.kllstudio.eta.apiget.nwstbus.responses.Stop;

public class NwstbusRouteEtaAdapter extends RouteEtaAdapter {
    private List<ETA> etas;
    private final List<RouteStop> routeStops;
    private List<Stop> stopList;
    private final String timeDisplayStyle;
    private Location location;
    private String bound;

    public NwstbusRouteEtaAdapter(List<ETA> etas, List<RouteStop> routeStops, List<Stop> stopList, String timeDisplayStyle, String bound) {
        this.etas = etas;
        this.routeStops = routeStops;
        this.stopList = stopList;
        this.timeDisplayStyle = timeDisplayStyle;
        this.bound = bound;
    }

    public void setLocation(Location location) {
        this.location = location;
        notifyDataSetChanged();
    }

    public void setEtas(List<ETA> etas) {
        this.etas = etas;
        notifyDataSetChanged();
    }

    public void setStopList(List<Stop> stops) {
        stopList = stops;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_eta_item_layout, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteEtaAdapter.ViewHolder holder, int position) {
        RouteStop routeStop = getRouteStop(position);
        List<ETA> filteredETA = new ArrayList<>();
        Stop stop = findStop(routeStop.getStop());
        for (ETA eta : etas) {
            if (eta.getRoute().equals(routeStop.getRoute())
                    && eta.getDir().equals(bound)
                    && eta.getSeq() == routeStop.getSeq()) {
                filteredETA.add(eta);
            }
        }
        holder.clearEtaText();
        for (int i = 0; i < Math.min(filteredETA.size(), 3); i++) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.TRADITIONAL_CHINESE);
            if (filteredETA.get(i).getEta() != null) {
                StringBuilder stringBuilder = new StringBuilder();
                if (timeDisplayStyle.equals("full_time")) {
                    stringBuilder.append(dateFormat.format(filteredETA.get(i).getEta()));
                } else if (timeDisplayStyle.equals("minutes")) {
                    int minutes = getMinutes(filteredETA.get(i).getEta());
                    if (minutes < 0) {
                        stringBuilder.append("已到達/已開出");
                    } else if (minutes < 1) {
                        stringBuilder.append("即將到達");
                    } else {
                        stringBuilder.append(String.format("%d 分鐘", minutes));
                    }
                }
                if (!filteredETA.get(i).getRemarkTc().isEmpty()) {
                    stringBuilder.append(String.format(" %s", filteredETA.get(i).getRemarkTc()));
                }
                holder.etaTextView[i].setText(stringBuilder.toString());
                holder.etaTextView[i].setVisibility(View.VISIBLE);
            } else {
                holder.etaTextView[i].setText(filteredETA.get(i).getRemarkTc());
                holder.etaTextView[i].setSelected(true);
                holder.etaTextView[i].setVisibility(View.VISIBLE);
                break;
            }
        }
        if (location != null && stop != null) {
            holder.distanceTextView.setVisibility(View.VISIBLE);
            holder.distanceTextView.setText(DistanceCalculation.getHumanReadableDistance(location.getLatitude(), location.getLongitude(), stop.getLatitude(), stop.getLongitude()));
        } else {
            holder.distanceTextView.setVisibility(View.GONE);
        }
        holder.stopNameTextView.setText(String.format("%d. %s", routeStop.getSeq(), stop == null ? "" : stop.getNameTc()));
        holder.itemView.setTag(position);
    }

    private int getMinutes(Date date) {
        long startMs = new Date().getTime();
        long endMs = date.getTime();
        long totalMillis = endMs - startMs;
        return ((int) Math.ceil((totalMillis / 1000.0) / 60)) % 60;
    }

    public List<ETA> getEtas(int position) {
        RouteStop routeStop = getRouteStop(position);
        List<ETA> filteredETA = new ArrayList<>();
        for (ETA eta : etas) {
            if (eta.getRoute().equals(routeStop.getRoute())
                    && eta.getDir().equals(bound)
                    && eta.getSeq() == routeStop.getSeq()) {
                filteredETA.add(eta);
            }
        }
        return filteredETA;
    }

    private Stop findStop(String stopID) {
        for (Stop s : stopList) {
            if (s.getStop().equals(stopID)) {
                return s;
            }
        }
        return null;
    }

    public Stop getStop(int position) {
        return findStop(routeStops.get(position).getStop());
    }

    @Override
    public int getItemCount() {
        return routeStops.size();
    }

    public RouteStop getRouteStop(int position) {
        return routeStops.get(position);
    }
}
