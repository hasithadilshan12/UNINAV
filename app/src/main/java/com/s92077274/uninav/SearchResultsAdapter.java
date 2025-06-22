package com.s92077274.uninav;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.s92077274.uninav.R;
import com.s92077274.uninav.models.MapPoint;

import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    public interface OnLocationClickListener {
        void onLocationClick(MapPoint location);
    }

    private List<MapPoint> locationList;
    private OnLocationClickListener listener;

    public SearchResultsAdapter(List<MapPoint> locationList, OnLocationClickListener listener) {
        this.locationList = locationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchResultsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultsAdapter.ViewHolder holder, int position) {
        MapPoint location = locationList.get(position);
        holder.name.setText(location.name);
        holder.description.setText(location.description);
        holder.itemView.setOnClickListener(v -> listener.onLocationClick(location));

    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, description;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvLocationName);
            description = itemView.findViewById(R.id.tvLocationDescription);
        }
    }
}
