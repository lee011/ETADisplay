package hk.kllstudio.eta.adapters;

import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import hk.kllstudio.eta.DistanceCalculation;
import hk.kllstudio.eta.R;
import hk.kllstudio.eta.apiget.kmb.responses.ETA;
import hk.kllstudio.eta.apiget.kmb.responses.Route;
import hk.kllstudio.eta.apiget.kmb.responses.Stop;
import hk.kllstudio.eta.room.Bookmark;

public class BookmarkedEtaAdapter extends RecyclerView.Adapter<BookmarkedEtaAdapter.ViewHolder> implements View.OnClickListener {
    private List<ETA> etas;
    private List<Stop> stopList;
    private List<Bookmark> bookmarks;
    private List<Route> routes;
    private OnItemClickListener mOnItemClickListener;
    private final String timeDisplayStyle;
    private Location location;

    public BookmarkedEtaAdapter(List<Bookmark> bookmarks, List<Route> routes, List<ETA> etas, List<Stop> stopList, String timeDisplayStyle) {
        this.etas = etas;
        this.stopList = stopList;
        this.bookmarks = bookmarks;
        this.routes = routes;
        this.timeDisplayStyle = timeDisplayStyle;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
        notifyDataSetChanged();
    }

    public void setLocation(Location location) {
        this.location = location;
        notifyDataSetChanged();
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

    public void removeBookmark(int position) {
        bookmarks.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmarked_eta_item_layout, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkedEtaAdapter.ViewHolder holder, int position) {
        Bookmark bookmark = getBookmark(position);
        Route route = getRoute(position);
        if (route != null) {
            holder.routeNumberTextView.setText(route.getRoute());
            holder.destTextView.setText(route.getDestTc());
        }
        List<ETA> filteredETA = new ArrayList<>();
        Stop stop = findStop(bookmark.getStop());
        for (ETA eta : etas) {
            if (eta.getDir().equals(bookmark.getDir())
                    && eta.getRoute().equals(bookmark.getRoute())
                    && eta.getServiceType().equals(bookmark.getServiceType())
                    && eta.getSeq() == bookmark.getSeq()) {
                filteredETA.add(eta);
            }
        }
        holder.clearEtaText();
        for (int i = 0; i < filteredETA.size(); i++) {
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
                holder.etaTextView[i].setSelected(true);
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
        holder.stopNameTextView.setText(String.format("%d. %s", bookmark.getSeq(), stop == null ? "" : stop.getNameTc()));
        holder.itemView.setTag(position);
    }

    private int getMinutes(Date date) {
        long startMs = new Date().getTime();
        long endMs = date.getTime();
        long totalMillis = endMs - startMs;
        return ((int) Math.ceil((totalMillis / 1000.0) / 60)) % 60;
    }

    public List<ETA> getEtas(int position) {
        Bookmark bookmark = getBookmark(position);
        List<ETA> filteredETA = new ArrayList<>();
        for (ETA eta : etas) {
            if (eta.getDir().equals(bookmark.getDir())
                    && eta.getRoute().equals(bookmark.getRoute())
                    && eta.getServiceType().equals(bookmark.getServiceType())
                    && eta.getSeq() == bookmark.getSeq()) {
                filteredETA.add(eta);
            }
        }
        return filteredETA;
    }

    public Route getRoute(int position) {
        Bookmark bookmark = getBookmark(position);
        Route route = null;
        for (Route r:routes) {
            if (r.getRoute().equals(bookmark.getRoute()) && r.getBound().equals(bookmark.getDir()) && r.getServiceType().equals(bookmark.getServiceType()))
                route = r;
        }
        return route;
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
        return findStop(bookmarks.get(position).getStop());
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    public Bookmark getBookmark(int position) {
        return bookmarks.get(position);
    }

    public static class ViewHolder extends ItemBaseViewHolder {
        TextView stopNameTextView;
        TextView distanceTextView;
        TextView routeNumberTextView;
        TextView destTextView;
        TextView[] etaTextView;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            stopNameTextView = itemView.findViewById(R.id.stopNameTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            routeNumberTextView = itemView.findViewById(R.id.routeNumberTextView);
            destTextView = itemView.findViewById(R.id.destTextView);
            etaTextView = new TextView[]{itemView.findViewById(R.id.eta1TextView), itemView.findViewById(R.id.eta2TextView), itemView.findViewById(R.id.eta3TextView)};
            imageView = itemView.findViewById(R.id.imageView);
        }

        public void clearEtaText() {
            for (TextView textView : etaTextView) {
                textView.setText("");
                textView.setVisibility(View.GONE);
            }
        }
    }
}
