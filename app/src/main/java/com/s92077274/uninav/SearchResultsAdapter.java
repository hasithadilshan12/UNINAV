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

    // Interface for handling click events on individual location items
    public interface OnLocationClickListener {
        void onLocationClick(MapPoint location);
    }

    private List<MapPoint> locationList;
    private OnLocationClickListener listener;

    // Constructor to initialize the adapter with data and a click listener
    public SearchResultsAdapter(List<MapPoint> locationList, OnLocationClickListener listener) {
        this.locationList = locationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    // Creates and inflates the item view for each list item
    public SearchResultsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    // Binds the data from the MapPoint object to the TextViews in the ViewHolder
    public void onBindViewHolder(@NonNull SearchResultsAdapter.ViewHolder holder, int position) {
        MapPoint location = locationList.get(position);
        holder.name.setText(location.name);
        holder.description.setText(location.description);
        // Set click listener for the entire item view
        holder.itemView.setOnClickListener(v -> listener.onLocationClick(location));
    }

    @Override
    // Returns the total number of items in the list
    public int getItemCount() {
        return locationList.size();
    }

    // ViewHolder class to hold references to the UI components of each item view
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, description;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvLocationName);
            description = itemView.findViewById(R.id.tvLocationDescription);
        }
    }
}
