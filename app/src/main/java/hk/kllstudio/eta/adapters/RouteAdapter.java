package hk.kllstudio.eta.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import hk.kllstudio.eta.R;
import hk.kllstudio.eta.apiget.IRoute;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> implements View.OnClickListener, Filterable {
    private final List<IRoute> routes;
    private final List<IRoute> filtered;
    private OnItemClickListener mOnItemClickListener = null;

    @Override
    public Filter getFilter() {
        return null;
    }

    public interface Predicate {
        boolean apply(IRoute item);
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
        filtered = new ArrayList<>();
    }

    public RouteAdapter(List<IRoute> routes) {
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
        IRoute route = filtered.get(position);
        holder.destTextView.setText(route.getDestTc());
        holder.originTextView.setText(route.getOrigTc());
        holder.routeNumberTextView.setText(route.getRoute());
        GradientDrawable imageViewDrawable = (GradientDrawable) holder.imageView.getDrawable();
        switch (route.getCo()) {
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
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    public void filter(Predicate predicate) {
        filtered.clear();
        for (IRoute route : routes) {
            if (predicate.apply(route)) {
                filtered.add(route);
                if (filtered.size() >= 100) break;
            }
        }
        notifyDataSetChanged();
    }

    public IRoute get(int position) {
        return filtered.get(position);
    }

    public class ViewHolder extends ItemBaseViewHolder {
        public TextView routeNumberTextView, originTextView, destTextView;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            routeNumberTextView = itemView.findViewById(R.id.routeNumberTextView);
            originTextView = itemView.findViewById(R.id.originTextView);
            destTextView = itemView.findViewById(R.id.destTextView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
