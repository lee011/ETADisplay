package hk.kllstudio.eta.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import hk.kllstudio.eta.R;
import hk.kllstudio.eta.apiget.responses.ETA;
import hk.kllstudio.eta.apiget.responses.RouteStop;
import hk.kllstudio.eta.apiget.responses.Stop;

public class RouteEtaAdapter extends RecyclerView.Adapter<RouteEtaAdapter.ViewHolder> implements View.OnClickListener {
    private List<ETA> etas;
    private final List<RouteStop> routeStops;
    private List<Stop> stopList;
    private OnItemClickListener mOnItemClickListener;

    public RouteEtaAdapter(List<ETA> etas, List<RouteStop> routeStops, List<Stop> stopList) {
        this.etas = etas;
        this.routeStops = routeStops;
        this.stopList = stopList;
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
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

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, (int) v.getTag());
        }
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
            if (eta.getDir().equals(routeStop.getBound())
                    && eta.getRoute().equals(routeStop.getRoute())
                    && eta.getServiceType().equals(routeStop.getServiceType())
                    && eta.getSeq() == routeStop.getSeq()) {
                filteredETA.add(eta);
            }
        }
        holder.clearEtaText();
        for (int i = 0; i < filteredETA.size(); i++) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.TRADITIONAL_CHINESE);
            if (filteredETA.get(i).getEta() != null) {
                holder.etaTextView[i].setText(dateFormat.format(filteredETA.get(i).getEta()));
                holder.etaTextView[i].setVisibility(View.VISIBLE);
            } else {
                holder.etaTextView[i].setText(filteredETA.get(i).getRemarkTc());
                holder.etaTextView[i].setSelected(true);
                holder.etaTextView[i].setVisibility(View.VISIBLE);
                break;
            }
        }
        holder.stopNameTextView.setText(String.format("%d. %s", routeStop.getSeq(), stop == null ? "" : stop.getNameTc()));
        holder.itemView.setTag(position);
    }

    public List<ETA> getEtas(int position) {
        RouteStop routeStop = getRouteStop(position);
        List<ETA> filteredETA = new ArrayList<>();
        for (ETA eta : etas) {
            if (eta.getDir().equals(routeStop.getBound())
                    && eta.getRoute().equals(routeStop.getRoute())
                    && eta.getServiceType().equals(routeStop.getServiceType())
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

    public class ViewHolder extends ItemBaseViewHolder {
        TextView stopNameTextView;
        TextView[] etaTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            stopNameTextView = itemView.findViewById(R.id.stopNameTextView);
            etaTextView = new TextView[]{itemView.findViewById(R.id.eta1TextView), itemView.findViewById(R.id.eta2TextView), itemView.findViewById(R.id.eta3TextView)};
        }

        public void clearEtaText() {
            for (TextView textView : etaTextView) {
                textView.setText("");
                textView.setVisibility(View.GONE);
            }
        }
    }
}
