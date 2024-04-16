package com.example.salemcircle;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import models.EventModel;
import network.ApiService;
import network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<EventModel> searchResults = new ArrayList<>();
    private String currentFilter = null;
    private String currentQuery = "";
    private Spinner spinnerFilter;
    private String currentDate = null;
    private TextView tvFilterInfo;
    private ImageView imgClearFilters;
    private long lastSearchTime = 0;  // To track the most recent search time
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        setupSearchView(view);
        setupFilterSpinner(view);
        setupDateButton(view);
        setupRecyclerView(view);
        setupFilterDisplay(view);

        return view;
    }
    private void setupFilterDisplay(View view) {
        tvFilterInfo = view.findViewById(R.id.tv_filter_info);
        imgClearFilters = view.findViewById(R.id.img_clear_filters);

        // Set up the clear filters button
        imgClearFilters.setOnClickListener(v -> {
            currentFilter = null;
            currentDate = null;
            spinnerFilter.setSelection(0); // Reset the spinner to "None"
            performSearch(currentQuery, null, null); // Re-perform search without filters
            updateFilterDisplay(); // Update the filter display
        });

        updateFilterDisplay(); // Initial display update
    }


    private void updateFilterDisplay() {
        String filterDisplay = "Filters applied: ";
        if (currentFilter == null && currentDate == null) {
            filterDisplay += "None";
            imgClearFilters.setVisibility(View.GONE); // Hide clear button when no filters are applied
        } else {
            if (currentFilter != null) {
                imgClearFilters.setVisibility(View.VISIBLE); // Show clear button when filters are applied
                filterDisplay += mapQueryParamToDisplayName(currentFilter) + " ";            }
            if (currentDate != null) {
                imgClearFilters.setVisibility(View.VISIBLE);
                filterDisplay += "Date: " + currentDate;
            }
        }
        tvFilterInfo.setText(filterDisplay);
    }
    private String mapQueryParamToDisplayName(String queryParam) {
        switch (queryParam) {
            case "eventName":
                return "Name";
            case "description":
                return "Description";
            case "eventId":
                return "ID";
            default:
                return "None"; // Default case
        }
    }


    private void setupDateButton(View view) {
        Button dateButton = view.findViewById(R.id.btn_date_picker);
        dateButton.setOnClickListener(v -> showDatePicker());
    }

    private void setupSearchView(View view) {
        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query, currentFilter, currentDate);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                // Cancel any previous searchRunnable callbacks
                searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> {
                    // Perform search only if newText is not empty
                    if (!newText.isEmpty()) {
                        performSearch(newText, currentFilter, currentDate);
                    }
                };
                // Post delayed to implement debounce mechanism
                searchHandler.postDelayed(searchRunnable, 500); // Adjust delay time as needed
                return true;
            }
        });
    }
    private void setupFilterSpinner(View view) {
        spinnerFilter = view.findViewById(R.id.spinner_filter);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.search_filters, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = (String) parent.getItemAtPosition(position);
                currentFilter = mapFilterToQueryParam(selectedFilter);
                updateFilterDisplay(); // Update display after filter selection
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentFilter = null;
                updateFilterDisplay(); // Update display when nothing is selected
            }
        });
    }

    private String mapFilterToQueryParam(String filter) {
        switch (filter) {
            case "Name":
                return "eventName";
            case "Description":
                return "description";
            case "ID":
                return "eventId";
            default:
                return null; // Handle "None" or any unexpected value
        }
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.rv_search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(getContext(), searchResults,null);
        recyclerView.setAdapter(eventAdapter);
    }
    private void performSearch(String query, String filter, String date) {
        this.currentQuery = query;
        this.currentFilter = filter;
        this.currentDate = date;

        updateFilterDisplay(); // Update the filter display whenever a search is performed

        final long searchTime = System.currentTimeMillis();
        lastSearchTime = searchTime;

        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        Call<List<EventModel>> call = apiService.searchEvents(query, filter, date);

        call.enqueue(new Callback<List<EventModel>>() {
            @Override
            public void onResponse(Call<List<EventModel>> call, Response<List<EventModel>> response) {
                // Check if this response is for the latest query
                if (searchTime != lastSearchTime) return;

                if (response.isSuccessful() && response.body() != null) {
                    searchResults.clear();
                    searchResults.addAll(response.body());
                    eventAdapter.notifyDataSetChanged();
                } else {
                    searchResults.clear();
                    eventAdapter.notifyDataSetChanged();
                    // Consider changing this to an in-line error message
                    if (!query.isEmpty()) {
                        Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<EventModel>> call, Throwable t) {
                if (searchTime != lastSearchTime) return;

                // Consider using a less obtrusive error notification for live search
                if (!query.isEmpty()) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            // Format the date as needed, here it's formatted as YYYY-MM-DD
            currentDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            if (!currentQuery.isEmpty()) {
                performSearch(currentQuery, currentFilter, currentDate); // Re-perform the search with the selected date
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }


}




