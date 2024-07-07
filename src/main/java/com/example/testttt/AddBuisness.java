package com.example.testttt;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddBuisness extends AppCompatActivity {
    // Declare UI elements
    TextInputEditText bis_name, owner_name, phone_number, email, description;
    EditText min_price, max_price;
    Button btn_add_buisness, back;
    // Firebase
    FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    // Spinners for city and business category selection
    private Spinner citySpinner, bisCategorySpinner;
    private ArrayAdapter<String> cityAdapter, categoryAdapter;
    private List<String> citiesList = new ArrayList<>();
    private List<String> categoriesList = new ArrayList<>();
    private String businessCategoryStr;

    private ArrayList<Uri> selectedImages = new ArrayList<>();
    private static final int REQUEST_CODE_ADD_PHOTOS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_buisness);


//ui
        citySpinner = findViewById(R.id.city_spinner);
        bisCategorySpinner = findViewById(R.id.bis_category_spinner);

        // Set up ArrayAdapter for citySpinner
        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, citiesList);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter);

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getCategoriesList());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bisCategorySpinner.setAdapter(categoryAdapter);

        // Load cities from GeoNames API
        loadCitiesFromGeoNames();
// Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
// Initialize UI elements from layout
        bis_name = findViewById(R.id.bis_name);
        owner_name = findViewById(R.id.owner_name);
        phone_number = findViewById(R.id.phone_number);
        email = findViewById(R.id.email);
        min_price = findViewById(R.id.min_price);
        max_price = findViewById(R.id.max_price);
        description = findViewById(R.id.Description);
        btn_add_buisness = findViewById(R.id.btn_add_buisness);
        back = findViewById(R.id.back);

        Button btn_add_photos = findViewById(R.id.btn_add_photos);
        btn_add_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddBuisness.this, AddPhotos.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_PHOTOS);
            }
        });


        btn_add_buisness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve input values from UI fields
                String bis_name_str = bis_name.getText().toString().trim();
                businessCategoryStr = bisCategorySpinner.getSelectedItem().toString();
                String owner_name_str = owner_name.getText().toString().trim();
                String phone_number_str = phone_number.getText().toString().trim();
                String email_str = email.getText().toString().trim();
                String city_str = citySpinner.getSelectedItem().toString().trim();
                String min_price_str = min_price.getText().toString().trim();
                String max_price_str = max_price.getText().toString().trim();
                String description_str = description.getText().toString().trim();
// Retrieve current Firebase user
                FirebaseUser user = mAuth.getCurrentUser();
                // Validate input fields
                if (user != null) {
                    if (bis_name_str.isEmpty()) {
                        showAlertDialog("Business name is required.");
                        return;
                    }
                    if (owner_name_str.isEmpty()) {
                        showAlertDialog("Owner name is required.");
                        return;
                    }
                    if (phone_number_str.isEmpty()) {
                        showAlertDialog("Phone number is required.");
                        return;
                    }
                    if (email_str.isEmpty()) {
                        showAlertDialog("Email is required.");
                        return;
                    }
                    if (city_str.isEmpty()) {
                        showAlertDialog("City is required.");
                        return;
                    }
                    if (min_price_str.isEmpty()) {
                        showAlertDialog("Minimum price is required.");
                        return;
                    }
                    if (max_price_str.isEmpty()) {
                        showAlertDialog("Maximum price is required.");
                        return;
                    }

                    // Validate email format
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email_str).matches()) {
                        showAlertDialog("Invalid email format.");
                        return;
                    }

                    // Validate phone number length
                    if (phone_number_str.length() < 9 || phone_number_str.length() > 10) {
                        showAlertDialog("Phone number must be 9 or 10 digits.");
                        return;
                    }

                    // Parse prices to int after all the above validations
                    int min_price_int;
                    int max_price_int;
                    try {
                        min_price_int = Integer.parseInt(min_price_str);
                        max_price_int = Integer.parseInt(max_price_str);
                    } catch (NumberFormatException e) {
                        showAlertDialog("Price must be a valid number.");
                        return;
                    }

                    // Validate min_price_int is less than max_price_int
                    if (min_price_int >= max_price_int) {
                        showAlertDialog("Minimum price must be less than maximum price.");
                        return;
                    }
// If all validations pass, proceed to upload images and save business data
                    String userId = user.getUid();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference jobsCollectionRef = db.collection("jobs");
                    DocumentReference specificDocumentRef = jobsCollectionRef.document(userId);

                    uploadImagesToStorage(specificDocumentRef, bis_name_str, businessCategoryStr, owner_name_str, phone_number_str, email_str, city_str, min_price_int, max_price_int,description_str ,userId);
                }
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
    // Handle result from AddPhotos activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if result is from AddPhotos activity and if it's OK
        if (requestCode == REQUEST_CODE_ADD_PHOTOS && resultCode == RESULT_OK) {
            // Retrieve selected images from intent data
            if (data != null) {
                ArrayList<Uri> selectedImages = data.getParcelableArrayListExtra("selectedImages");
                if (selectedImages != null && !selectedImages.isEmpty()) {
                    // Add the selected images to the list
                    this.selectedImages.addAll(selectedImages);
                    Log.d("SELECTED_IMAGES", "Selected images URIs: " + this.selectedImages.toString());
                }
            }
        }
    }

    private void showAlertDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Method to resolve content URI to file URI
    private void uploadImagesToStorage(DocumentReference documentReference, String bisName, String businessCategory, String ownerName, String phoneNumber, String email, String city, int minPrice, int maxPrice, String description, String userId) {
        if (selectedImages.isEmpty()) {
            saveBusinessToFirestore(documentReference, bisName, businessCategory, ownerName, phoneNumber, email, city, minPrice, maxPrice, description, userId, new ArrayList<>());
            return;
        }

        List<String> downloadUrls = new ArrayList<>();
        // Iterate through each selected image URI and upload to Firebase Storage
        for (Uri fileUri : selectedImages) {

            StorageReference imageRef = storageReference.child("images/" + System.currentTimeMillis() + "_" + fileUri.getLastPathSegment());
            // Upload image file to Firebase Storage
            imageRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
                // On successful upload, get download URL
                imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    // Add download URL to list
                    downloadUrls.add(downloadUri.toString());
                    // If all images uploaded, save business data to Firestore
                    if (downloadUrls.size() == selectedImages.size()) {
                        saveBusinessToFirestore(documentReference, bisName, businessCategory, ownerName, phoneNumber, email, city, minPrice, maxPrice, description, userId, downloadUrls);
                    }
                });
            }).addOnFailureListener(e -> Toast.makeText(AddBuisness.this, "Failed to upload image: " + fileUri.getLastPathSegment(), Toast.LENGTH_SHORT).show());
        }
    }




    // Method to save business data to Firestore
    private void saveBusinessToFirestore(DocumentReference documentReference, String bisName, String businessCategory, String ownerName, String phoneNumber, String email, String city, int minPrice, int maxPrice, String description, String userId, List<String> imageUrls) {
        if (description == null){
            description = "";
        }
        Buisness business = new Buisness(bisName, businessCategory, ownerName, phoneNumber, email, city, minPrice, maxPrice, description, userId, imageUrls);

        documentReference.set(new HashMap<String, Object>() {{
                    put("buisnessAraay", FieldValue.arrayUnion(business));
                }}, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddBuisness.this, "Business Added Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), ServiceProvider.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddBuisness.this, "Business Addition Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCitiesFromGeoNames() {
        // Replace 'your_username' with the username you registered on GeoNames
        String geonamesRequestUrl = "http://api.geonames.org/searchJSON?country=IL&maxRows=200&username=adilevi";

        // Use AsyncTask, Volley, Retrofit, or any other method to perform the network request
        // This is a simple example using AsyncTask, but it's deprecated and you should use a more modern approach
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.MyProfile){
            Intent intent = new Intent(getApplicationContext(), MyProfile.class);
            startActivity(intent);
            finish();
        }

        if(id == R.id.SwitchToClient){
            Intent intent = new Intent(getApplicationContext(), Client.class);
            startActivity(intent);
            finish();
        }

        if(id == R.id.ChangePassword){
            Intent intent = new Intent(getApplicationContext(), ChangePassword.class);
            startActivity(intent);
            finish();
        }
        if(id == R.id.Logout){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        if(id == R.id.home){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        return true;
    }



}
