package hk.kllstudio.eta.adapters;

import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import hk.kllstudio.eta.R;

public abstract class RouteEtaAdapter extends RecyclerView.Adapter<RouteEtaAdapter.ViewHolder> implements View.OnClickListener {
    private OnItemClickListener mOnItemClickListener;

    public abstract void setLocation(Location location);

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, (int) v.getTag());
        }
    }

    @NonNull
    @Override
    public abstract ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(@NonNull ViewHolder holder, int position);

    @Override
    public abstract int getItemCount();

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends ItemBaseViewHolder {
        TextView stopNameTextView;
        TextView distanceTextView;
        TextView[] etaTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            stopNameTextView = itemView.findViewById(R.id.stopNameTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
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
