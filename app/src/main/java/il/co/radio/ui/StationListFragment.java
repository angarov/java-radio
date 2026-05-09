package il.co.radio.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import il.co.radio.R;
import il.co.radio.api.ApiCallback;
import il.co.radio.api.ApiClient;
import il.co.radio.databinding.FragmentStationListBinding;
import il.co.radio.model.Station;
import il.co.radio.model.StationsResponse;

public class StationListFragment extends Fragment {

    private static final String ARG_API_URL = "api_url";

    public interface OnStationSelectedListener {
        void onStationSelected(Station station);
    }

    private FragmentStationListBinding binding;
    private StationAdapter adapter;
    private OnStationSelectedListener listener;

    public static StationListFragment newInstance(String apiUrl) {
        StationListFragment fragment = new StationListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_API_URL, apiUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnStationSelectedListener) {
            listener = (OnStationSelectedListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnStationSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStationListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        fetchStations();

        ViewCompat.setOnApplyWindowInsetsListener(binding.stationList, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });
    }

    private void setupRecyclerView() {
        adapter = new StationAdapter();
        adapter.setOnStationClickListener(station -> {
            if (listener != null) {
                listener.onStationSelected(station);
            }
        });
        binding.stationList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.stationList.setAdapter(adapter);
    }

    private void fetchStations() {
        String url = getArguments() != null ? getArguments().getString(ARG_API_URL) : null;
        if (url == null) return;

        binding.loadingIndicator.setVisibility(View.VISIBLE);
        ApiClient.getInstance().getStations(url, new ApiCallback<StationsResponse>() {
            @Override
            public void onSuccess(StationsResponse response) {
                if (!isAdded()) return;
                binding.loadingIndicator.setVisibility(View.GONE);
                if (response != null && response.data != null) {
                    adapter.setStations(response.data);
                } else {
                    Toast.makeText(requireContext(), R.string.error_loading, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception error) {
                if (!isAdded()) return;
                binding.loadingIndicator.setVisibility(View.GONE);
                Toast.makeText(requireContext(), R.string.error_loading, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
