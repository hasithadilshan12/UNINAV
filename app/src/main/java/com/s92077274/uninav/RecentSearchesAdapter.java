package com.s92077274.uninav;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.s92077274.uninav.models.MapPoint;
import java.util.List;

public class RecentSearchesAdapter extends RecyclerView.Adapter<RecentSearchesAdapter.ViewHolder> {


    public interface OnRecentSearchClickListener {
        void onRecentSearchClick(MapPoint location);
    }

    private List<MapPoint> recentLocationList;
    private OnRecentSearchClickListener listener;

    public RecentSearchesAdapter(List<MapPoint> recentLocationList, OnRecentSearchClickListener listener) {
        this.recentLocationList = recentLocationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MapPoint location = recentLocationList.get(position);
        holder.tvRecentLocationName.setText(location.name);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecentSearchClick(location);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recentLocationList.size();
    }


    public void updateData(List<MapPoint> newLocationList) {
        this.recentLocationList = newLocationList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecentLocationName;


        public ViewHolder(View itemView) {
            super(itemView);
            tvRecentLocationName = itemView.findViewById(R.id.tvRecentLocationName);

        }
    }
}
