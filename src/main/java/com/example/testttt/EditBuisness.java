package com.example.testttt;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditBuisness extends AppCompatActivity {
    TextInputEditText bis_name, owner_name, phone_number, email, description;
    EditText min_price, max_price;
    Spinner spinner_bis_category, spinner_city;
    Button btn_edit_photos, btn_save_buisness, back;
    FirebaseAuth mAuth;
    Map<String, Object> business;
    private ArrayAdapter<String> cityAdapter;
    private List<String> citiesList = new ArrayList<>();
    private static final int EDIT_PHOTOS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_buisness);

        mAuth = FirebaseAuth.getInstance();
        bis_name = findViewById(R.id.bis_name);
        spinner_bis_category = findViewById(R.id.spinner_bis_category);
        owner_name = findViewById(R.id.owner_name);
        phone_number = findViewById(R.id.phone_number);
        email = findViewById(R.id.email);
        spinner_city = findViewById(R.id.spinner_city);
        min_price = findViewById(R.id.min_price);
        max_price = findViewById(R.id.max_price);
        description =findViewById(R.id.Description);
        btn_edit_photos = findViewById(R.id.btn_edit_photos);
        btn_save_buisness = findViewById(R.id.btn_save_buisness);
        back = findViewById(R.id.back);

        // Set up adapters for spinners
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.bis_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_bis_category.setAdapter(categoryAdapter);

        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, citiesList);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_city.setAdapter(cityAdapter);

        // Load cities from GeoNames API
        loadCitiesFromGeoNames();

        // Retrieve position from intent extras
        int position = getIntent().getIntExtra("position", -1);
        Log.d("position edit: ", String.valueOf(position));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        String userId = user.getUid();
        CollectionReference jobsCollectionRef = db.collection("jobs");

        DocumentReference docRef = jobsCollectionRef.document(userId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    List<Map<String, Object>> arrayBuisness = (List<Map<String, Object>>) documentSnapshot.get("buisnessAraay");
                    if (arrayBuisness != null && !arrayBuisness.isEmpty()) {
                        business = arrayBuisness.get(position); // Accessing the first map object
                        bis_name.setText((CharSequence) business.get("bis_name"));
                        owner_name.setText((CharSequence) business.get("owner_name"));
                        phone_number.setText((CharSequence) business.get("phone_number"));
                        email.setText((CharSequence) business.get("email"));
                        min_price.setText(String.valueOf(business.get("min_price")));
                        max_price.setText(String.valueOf(business.get("max_price")));
                        String category = (String) business.get("buisness_category");
                        String city = (String) business.get("city");
                        description.setText((CharSequence) business.get("description"));
                        Log.d("EditBuisness", "Retrieved city from database: " + city);

                        setSpinnerDefaultValue(spinner_bis_category, category);
                        setSpinnerDefaultValue(spinner_city, city);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle failure
            }
        });

        btn_edit_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditPhotos.class);
                intent.putExtra("position", position);
                startActivityForResult(intent, EDIT_PHOTOS_REQUEST);
            }
        });

        btn_save_buisness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBusinessData();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ServiceProvider.class);
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

    private void saveBusinessData() {
        String bis_name_str = String.valueOf(bis_name.getText());
        String buisness_category_str = spinner_bis_category.getSelectedItem().toString();
        String owner_name_str = String.valueOf(owner_name.getText());
        String phone_number_str = String.valueOf(phone_number.getText());
        String email_str = String.valueOf(email.getText());
        String city_str = spinner_city.getSelectedItem().toString();
        int min_price_int = Integer.parseInt(String.valueOf(min_price.getText()));
        int max_price_int = Integer.parseInt(String.valueOf(max_price.getText()));

        String description_str;
        if (description == null){
            description_str = "";
        } else {
            description_str = String.valueOf(description.getText());
        }

        Map<String, Object> newData = new HashMap<>();
        newData.put("bis_name", bis_name_str);
        newData.put("buisness_category", buisness_category_str);
        newData.put("owner_name", owner_name_str);
        newData.put("phone_number", phone_number_str);
        newData.put("email", email_str);
        newData.put("city", city_str);
        newData.put("min_price", min_price_int);
        newData.put("max_price", max_price_int);
        newData.put("description", description_str);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        String userId = user.getUid();
        newData.put("uid", userId);

        CollectionReference jobsCollectionRef = db.collection("jobs");
        DocumentReference docRef = jobsCollectionRef.document(userId);

        int position = getIntent().getIntExtra("position", -1);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    List<Map<String, Object>> businessArray = (List<Map<String, Object>>) documentSnapshot.get("buisnessAraay");
                    if (businessArray != null && !businessArray.isEmpty()) {
                        // Get existing imageUrls from Firestore and add them to newData
                        Map<String, Object> businessData = businessArray.get(position);
                        List<String> imageUrls = (List<String>) businessData.get("imageUrls");
                        newData.put("imageUrls", imageUrls);

                        // Update other fields in newData
                        businessArray.set(position, newData);
                        docRef.update("buisnessAraay", businessArray)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(EditBuisness.this, "Data updated successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), ServiceProvider.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditBuisness.this, "Failed to update data", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PHOTOS_REQUEST) {
            if (resultCode == RESULT_OK) {
                // Handle the data returned from EditPhotos activity if needed
            }
        }
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
                    citiesList.addAll(cities);
                    cityAdapter.notifyDataSetChanged();
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

    private void setSpinnerDefaultValue(Spinner spinner, String value) {
        if (value != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
            int position = adapter.getPosition(value);
            spinner.setSelection(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
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
        if (id == R.id.SwitchToClient) {
            Intent intent = new Intent(getApplicationContext(), Client.class);
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
