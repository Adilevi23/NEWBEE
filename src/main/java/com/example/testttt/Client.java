package com.example.testttt;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client extends AppCompatActivity {
    private RecyclerView businessRecyclerView;
    private SearchView searchView;
    private BusinessAdapterClient adapter;
    private List<Buisness> allBusinesses = new ArrayList<>();

    FirebaseAuth mAuth;
    Button back;

    private Spinner citySpinner, categorySpinner;
    private ArrayAdapter<String> cityAdapter, categoryAdapter;
    private List<String> citiesList = new ArrayList<>();
    private List<String> categoriesList = new ArrayList<>();
    private Button btnFilter;

    private List<Buisness> filteredBusinesses = new ArrayList<>();

    public enum SortOption {
        BY_NAME,
        BY_CATEGORY,
        BY_CITY,
        BY_LOW_PRICE
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        citySpinner = findViewById(R.id.client_city_spinner);
        categorySpinner = findViewById(R.id.client_category_spinner);
        btnFilter = findViewById(R.id.btn_filter);

        // Set up adapter for the city spinner
        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, citiesList);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter);

        // Set up adapter for the category spinner
        categoriesList.add("All"); // Add "All" as the default option
        categoriesList.addAll(getCategoriesList()); // Add other categories
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Load cities from GeoNames API
        loadCitiesFromGeoNames();

        // Initialize SearchView
        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search when the user submits the query
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Perform search as the user types
                performSearch(newText);
                return false;
            }
        });

        // Setup RecyclerView and Adapter
        businessRecyclerView = findViewById(R.id.businessRecyclerView);
        businessRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BusinessAdapterClient(allBusinesses);
        businessRecyclerView.setAdapter(adapter);

        // Load businesses from Firestore
        loadBusinessesFromFirestore();
        // Set up filter button click listener
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterBusinesses();
            }
        });

        back = findViewById(R.id.back);
        businessRecyclerView = findViewById(R.id.businessRecyclerView);
        businessRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        db.collection("jobs").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Buisness> allBusinesses = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.contains("buisnessAraay")) {
                            List<Map<String, Object>> arrayList = (List<Map<String, Object>>) document.get("buisnessAraay");
                            for (Map<String, Object> map : arrayList) {
                                Buisness business = new Buisness(map);
                                allBusinesses.add(business);
                            }
                        }
                    }
                    BusinessAdapterClient adapter = new BusinessAdapterClient(allBusinesses);
                    businessRecyclerView.setAdapter(adapter);
                } else {
                    Log.w("Firestore", "Error getting documents.", task.getException());
                }
            }
        });

        // Find the sort button and set click listener
        Button btnSort = findViewById(R.id.btn_sort);
        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortOptionsDialog();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void loadBusinessesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("jobs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allBusinesses.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.contains("buisnessAraay")) {
                        List<Map<String, Object>> arrayList = (List<Map<String, Object>>) document.get("buisnessAraay");
                        for (Map<String, Object> map : arrayList) {
                            Buisness business = new Buisness(map);
                            allBusinesses.add(business);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.w("Firestore", "Error getting documents.", task.getException());
            }
        });
    }

    private void performSearch(String query) {
        Log.d("performSearch", "Query submitted: " + query);

        List<Buisness> filteredBusinesses = new ArrayList<>();
        String lowerQuery = query.toLowerCase(); // Convert query to lowercase for case-insensitive search

        if (lowerQuery.isEmpty()) {
            filteredBusinesses.addAll(allBusinesses); // Display all businesses when query is empty
            Log.d("performSearch", "Query is empty. Showing all businesses.");
        } else {
            for (Buisness business : allBusinesses) {
                if (business.getBis_name().toLowerCase().contains(lowerQuery)) {
                    filteredBusinesses.add(business);
                }
            }
            Log.d("performSearch", "Filtered businesses count: " + filteredBusinesses.size());
        }

        // Update RecyclerView with filtered list
        adapter.filterList(filteredBusinesses);
        businessRecyclerView.setAdapter(adapter);
    }
    private void loadCitiesFromGeoNames() {
        String geonamesRequestUrl = "http://api.geonames.org/searchJSON?country=IL&maxRows=200&username=adilevi";

        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... voids) {
                try {
                    URL url = new URL(geonamesRequestUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        return parseCities(new JSONObject(stringBuilder.toString()));
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage(), e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<String> cities) {
                super.onPostExecute(cities);
                if (cities != null) {
                    citiesList.clear();
                    citiesList.add("All");
                    citiesList.addAll(cities);
                    cityAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(Client.this, "Failed to load cities", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private List<String> parseCities(JSONObject json) {
        List<String> cities = new ArrayList<>();
        try {
            JSONArray geonames = json.getJSONArray("geonames");
            for (int i = 0; i < geonames.length(); i++) {
                JSONObject geoname = geonames.getJSONObject(i);
                String cityName = geoname.getString("name");
                cities.add(cityName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cities;
    }

    private List<String> getCategoriesList() {
        List<String> categories = new ArrayList<>();
        categories.add("Cosmetics");
        categories.add("Plumbing");
        categories.add("Private Tutor");
        categories.add("Electrician");
        categories.add("Transportation Services");
        categories.add("DJ");
        categories.add("Exterminator");
        categories.add("Babysitter");
        categories.add("Cleaning Service");
        categories.add("Handyman");
        categories.add("Physical Therapy");
        categories.add("Massage");
        categories.add("Psychology");
        categories.add("Landscaping");
        categories.add("Pet Grooming");
        categories.add("Catering");
        categories.add("Graphic Design");
        categories.add("Web Development");
        categories.add("Accounting");
        categories.add("Legal Services");
        categories.add("Real Estate");
        categories.add("Event Planning");
        categories.add("Fitness Training");
        categories.add("Photography");
        categories.add("Carpentry");
        categories.add("Interior Design");
        categories.add("Tailoring");
        categories.add("Auto Repair");
        categories.add("IT Support");
        categories.add("Marketing Consulting");
        categories.add("Hairdressing");
        categories.add("Nail Salon");
        categories.add("Yoga Instruction");
        categories.add("Music Lessons");
        categories.add("Dance Instruction");
        categories.add("Painting (Residential/Commercial)");
        categories.add("Roofing Services");
        categories.add("Window Cleaning");
        categories.add("Snow Removal");
        categories.add("Pool Cleaning");
        categories.add("Security Services");
        categories.add("Locksmith");
        categories.add("Childcare Center");
        categories.add("Elder Care Services");
        categories.add("Courier Services");
        categories.add("Translation Services");
        categories.add("Pet Sitting");
        categories.add("Tutoring Center");
        categories.add("Mobile Car Wash");
        categories.add("HVAC Services");
        categories.add("Bakery");
        categories.add("Florist");
        categories.add("Barber Shop");
        categories.add("Driving School");
        categories.add("Speech Therapy");
        categories.add("Occupational Therapy");
        categories.add("Chiropractic Services");
        categories.add("Dietitian/Nutritionist");
        categories.add("Personal Chef");
        categories.add("Life Coaching");
        categories.add("Business Consulting");
        categories.add("Tax Preparation");
        categories.add("Insurance Agency");
        categories.add("Virtual Assistant");
        categories.add("Mobile Repair Services");
        categories.add("Window Installation");
        categories.add("Moving Services");
        categories.add("Financial Planning");
        categories.add("SEO Services");
        categories.add("House Sitting");
        categories.add("Antique Restoration");
        categories.add("Bicycle Repair");
        categories.add("Car Detailing");
        categories.add("Bookkeeping");
        categories.add("Writing/Editing Services");
        categories.add("Else");
        return categories;
    }

    private void filterBusinesses() {
        String selectedCity = citySpinner.getSelectedItem().toString();
        String selectedCategory = categorySpinner.getSelectedItem().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference businessesRef = db.collection("jobs");

        businessesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Buisness> filteredBusinesses = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.contains("buisnessAraay")) {
                        List<HashMap<String, Object>> businesses = (List<HashMap<String, Object>>) document.get("buisnessAraay");
                        for (HashMap<String, Object> businessMap : businesses) {
                            String city = (String) businessMap.get("city");
                            String category = (String) businessMap.get("buisness_category");

                            // Check if the selected city matches and handle "All" category selection
                            if ((selectedCity.equals(city) || selectedCity.equals("All")) &&
                                    (selectedCategory.equals(category) || selectedCategory.equals("All"))) {

                                // Create a Buisness object from the map
                                String bisName = (String) businessMap.get("bis_name");
                                String ownerName = (String) businessMap.get("owner_name");
                                String phoneNumber = (String) businessMap.get("phone_number");
                                String email = (String) businessMap.get("email");
                                // Handle integer parsing correctly
                                int minPrice = ((Number) businessMap.get("min_price")).intValue();
                                int maxPrice = ((Number) businessMap.get("max_price")).intValue();
                                String description = (String) businessMap.get("description");
                                String userId = (String) businessMap.get("uid");
                                List<String> imageUrls = (List<String>) businessMap.get("imageUrls");
                                Buisness buisness = new Buisness(bisName, category, ownerName, phoneNumber, email, city, minPrice, maxPrice, description, userId, imageUrls);

                                filteredBusinesses.add(buisness);
                            }
                        }
                    }
                }
                displayFilteredBusinesses(filteredBusinesses);
            } else {
                Toast.makeText(Client.this, "Failed to filter businesses", Toast.LENGTH_SHORT).show();
            }
        });

        businessesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                filteredBusinesses.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.contains("buisnessAraay")) {
                        List<HashMap<String, Object>> businesses = (List<HashMap<String, Object>>) document.get("buisnessAraay");
                        for (HashMap<String, Object> businessMap : businesses) {
                            String city = (String) businessMap.get("city");
                            String category = (String) businessMap.get("buisness_category");

                            // Check if the selected city matches and handle "All" category selection
                            if ((selectedCity.equals(city) || selectedCity.equals("All")) &&
                                    (selectedCategory.equals(category) || selectedCategory.equals("All"))) {

                                // Create a Buisness object from the map
                                String bisName = (String) businessMap.get("bis_name");
                                String ownerName = (String) businessMap.get("owner_name");
                                String phoneNumber = (String) businessMap.get("phone_number");
                                String email = (String) businessMap.get("email");
                                // Handle integer parsing correctly
                                int minPrice = ((Number) businessMap.get("min_price")).intValue();
                                int maxPrice = ((Number) businessMap.get("max_price")).intValue();
                                String description = (String) businessMap.get("description");
                                String userId = (String) businessMap.get("uid");
                                List<String> imageUrls = (List<String>) businessMap.get("imageUrls");
                                Buisness buisness = new Buisness(bisName, category, ownerName, phoneNumber, email, city, minPrice, maxPrice, description, userId, imageUrls);

                                filteredBusinesses.add(buisness);
                            }
                        }
                    }
                }
                sortAndDisplayBusinesses(SortOption.BY_NAME);
            } else {
                Toast.makeText(Client.this, "Failed to filter businesses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayFilteredBusinesses(List<Buisness> filteredBusinesses) {
        // Implement this method to display the filtered businesses to the user
        // For example, you could display them in a RecyclerView or ListView
        for (Buisness business : filteredBusinesses) {
            Log.d("FILTERED_BUSINESS", business.getBis_name() + " - " + business.getCity());
        }
        BusinessAdapterClient adapter2 = new BusinessAdapterClient(filteredBusinesses);
        businessRecyclerView.setAdapter(adapter2);
    }

    private void sortBusinesses(SortOption sortOption, List<Buisness> businesses) {
        switch (sortOption) {
            case BY_NAME:
                Collections.sort(businesses, (b1, b2) -> b1.getBis_name().compareToIgnoreCase(b2.getBis_name()));
                break;
            case BY_CATEGORY:
                Collections.sort(businesses, (b1, b2) -> b1.getBuisness_category().compareToIgnoreCase(b2.getBuisness_category()));
                break;
            case BY_CITY:
                Collections.sort(businesses, (b1, b2) -> b1.getCity().compareToIgnoreCase(b2.getCity()));
                break;
            case BY_LOW_PRICE:
                Collections.sort(businesses, Comparator.comparingInt(Buisness::getMin_price));
                break;
            default:
                break;
        }
    }

    private void showSortOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort Options")
                .setItems(R.array.sort_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SortOption sortOption = SortOption.values()[which];
                        sortAndDisplayBusinesses(sortOption);
                    }
                });
        builder.create().show();
    }

    private void sortAndDisplayBusinesses(SortOption sortOption) {
        sortBusinesses(sortOption, filteredBusinesses); // Apply sorting
        displayFilteredBusinesses(filteredBusinesses); // Update RecyclerView
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.MyProfile) {
            Intent intent = new Intent(getApplicationContext(), MyProfile.class);
            startActivity(intent);
            finish();
        }
        if (id == R.id.SwitchToServiceProvider) {
            Intent intent = new Intent(getApplicationContext(), ServiceProvider.class);
            startActivity(intent);
            finish();
        }

        if (id == R.id.ChangePassword) {
            Intent intent = new Intent(getApplicationContext(), ChangePassword.class);
            startActivity(intent);
            finish();
        }

        if (id == R.id.Logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        if (id == R.id.home) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        return true;
    }
}