package il.co.radio.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import il.co.radio.databinding.ItemStationBinding;
import il.co.radio.model.Station;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> {

    private final List<Station> stations = new ArrayList<>();
    private OnStationClickListener listener;

    public interface OnStationClickListener {
        void onStationClick(Station station);
    }

    public void setOnStationClickListener(OnStationClickListener listener) {
        this.listener = listener;
    }

    public void setStations(List<Station> newStations) {
        stations.clear();
        stations.addAll(newStations);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStationBinding binding = ItemStationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Station station = stations.get(position);
        holder.bind(station);
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemStationBinding binding;

        ViewHolder(ItemStationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Station station) {
            binding.stationName.setText(station.getName());

            String imageUrl = station.getImageUrl();
            if (imageUrl != null) {
                Glide.with(binding.getRoot().getContext())
                        .load(imageUrl)
                        .placeholder(il.co.radio.R.drawable.ic_radio)
                        .error(il.co.radio.R.drawable.ic_radio)
                        .into(binding.stationImage);
            } else {
                binding.stationImage.setImageResource(il.co.radio.R.drawable.ic_radio);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStationClick(station);
                }
            });
        }
    }
}
