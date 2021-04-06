package hk.kllstudio.eta.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import hk.kllstudio.eta.R;
import hk.kllstudio.eta.apiget.responses.Route;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> implements View.OnClickListener, Filterable {
    private final List<Route> routes;
    private final List<Route> filtered;
    private OnItemClickListener mOnItemClickListener = null;

    @Override
    public Filter getFilter() {
        return null;
    }

    public interface Predicate {
        boolean apply(Route item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    //define interface
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public RouteAdapter() {
        routes = new ArrayList<>();
        filtered=new ArrayList<>();
    }

    public RouteAdapter(List<Route> routes) {
        this.routes = routes;
        filtered = new ArrayList<>();
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_item_layout, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteAdapter.ViewHolder holder, int position) {
        Route route = filtered.get(position);
        holder.destTextView.setText(route.getDestTc());
        holder.originTextView.setText(route.getOrigTc());
        holder.routeNumberTextView.setText(route.getRoute());
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    public void filter(Predicate predicate) {
        filtered.clear();
        for (Route route : routes) {
            if (predicate.apply(route)) {
                filtered.add(route);
                if (filtered.size() >= 100) break;
            }
        }
        notifyDataSetChanged();
    }

    public Route get(int position) {
        return filtered.get(position);
    }

    public class ViewHolder extends ItemBaseViewHolder {
        public TextView routeNumberTextView, originTextView, destTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            routeNumberTextView=itemView.findViewById(R.id.routeNumberTextView);
            originTextView=itemView.findViewById(R.id.originTextView);
            destTextView=itemView.findViewById(R.id.destTextView);
        }
    }
}
