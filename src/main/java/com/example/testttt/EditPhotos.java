package com.example.testttt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditPhotos extends AppCompatActivity {
    private static final int REQUEST_CODE_ADD_PHOTOS = 1; // Request code for adding photos

    private RecyclerView recyclerView; // RecyclerView to display photos
    private PhotoAdapter photoAdapter; // Adapter for the RecyclerView
    private List<String> imageUrls; // List to hold image URLs
    private int position; // Position of the business in the Firestore document
    private FirebaseFirestore db; // Firestore database instance
    private FirebaseAuth mAuth; // Firebase Authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photos); // Set the content view to the layout activity_edit_photos

        // Initialize UI elements
        recyclerView = findViewById(R.id.recycler_view_photos);
        Button buttonAddPhoto = findViewById(R.id.button_add_photo);
        Button buttonDone = findViewById(R.id.button_done);

        // Get the position passed from the previous activity
        position = getIntent().getIntExtra("position", -1);
        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Authentication
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        // Initialize the adapter with an empty list and a delete click listener
        photoAdapter = new PhotoAdapter(new ArrayList<>(), new PhotoAdapter.OnPhotoClickListener() {
            @Override
            public void onDeleteClick(int position) {
                deletePhoto(position); // Call deletePhoto method when delete button is clicked
            }
        });

        // Set the layout manager and adapter for the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(photoAdapter);

        loadBusinessPhotos(); // Load photos from Firestore

        // Set click listener for the add photo button
        buttonAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPhoto(); // Call addPhoto method to add new photos
            }
        });

        // Set click listener for the done button
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity(); // Call finishActivity method to finish the activity
            }
        });
    }

    // Method to load business photos from Firestore
    private void loadBusinessPhotos() {
        FirebaseUser user = mAuth.getCurrentUser(); // Get current authenticated user
        if (user != null) {
            String userId = user.getUid(); // Get user ID
            DocumentReference docRef = db.collection("jobs").document(userId); // Reference to the user's document
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        // Get the business array from the document
                        List<Map<String, Object>> businessArray = (List<Map<String, Object>>) documentSnapshot.get("buisnessAraay");
                        if (businessArray != null && !businessArray.isEmpty()) {
                            // Get the business at the specified position
                            Map<String, Object> business = businessArray.get(position);
                            imageUrls = (List<String>) business.get("imageUrls"); // Get image URLs
                            if (imageUrls == null) {
                                imageUrls = new ArrayList<>(); // Initialize if null
                            }
                            // Update the adapter with the loaded data
                            photoAdapter.setImageUrls(imageUrls);
                            photoAdapter.notifyDataSetChanged(); // Notify adapter of data change
                        }
                    }
                }
            });
        }
    }

    // Method to delete a photo
    private void deletePhoto(int position) {
        String imageUrlToDelete = imageUrls.remove(position); // Remove the image URL from the list
        photoAdapter.notifyItemRemoved(position); // Notify adapter of item removal
        updateBusinessPhotos(imageUrls); // Update Firestore with updated image URLs
        deleteImageFromStorage(imageUrlToDelete); // Delete image from Firebase Storage
    }

    // Method to delete an image from Firebase Storage
    private void deleteImageFromStorage(String imageUrl) {
        FirebaseStorage storage = FirebaseStorage.getInstance(); // Get Firebase Storage instance
        StorageReference storageRef = storage.getReferenceFromUrl(imageUrl); // Get reference to the image
        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("EditPhotos", "Image deleted from storage successfully"); // Log success message
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("EditPhotos", "Failed to delete image from storage: " + exception.getMessage()); // Log failure message
            }
        });
    }

    // Method to add a photo
    private void addPhoto() {
        Intent intent = new Intent(this, AddPhotos.class); // Create an intent to start AddPhotos activity
        startActivityForResult(intent, REQUEST_CODE_ADD_PHOTOS); // Start activity for result
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_PHOTOS && resultCode == RESULT_OK) {
            if (data != null) {
                // Get selected images from the result data
                ArrayList<Uri> selectedImages = data.getParcelableArrayListExtra("selectedImages");
                if (selectedImages != null && !selectedImages.isEmpty()) {
                    uploadImagesToStorage(selectedImages); // Upload selected images to storage
                }
            }
        }
    }

    // Method to upload images to Firebase Storage
    private void uploadImagesToStorage(ArrayList<Uri> selectedImages) {
        FirebaseStorage storage = FirebaseStorage.getInstance(); // Get Firebase Storage instance
        StorageReference storageReference = storage.getReference(); // Get reference to the storage root

        List<String> newImageUrls = new ArrayList<>(); // List to hold new image URLs
        for (Uri fileUri : selectedImages) {
            // Create a reference for each image
            StorageReference imageRef = storageReference.child("images/" + System.currentTimeMillis() + "_" + fileUri.getLastPathSegment());
            imageRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
                // Get download URL of the uploaded image
                imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    newImageUrls.add(downloadUri.toString()); // Add URL to the list
                    if (newImageUrls.size() == selectedImages.size()) {
                        imageUrls.addAll(newImageUrls); // Add new URLs to the existing list
                        photoAdapter.notifyDataSetChanged(); // Notify adapter of data change
                        updateBusinessPhotos(imageUrls); // Update Firestore with updated image URLs
                    }
                });
            }).addOnFailureListener(e -> Toast.makeText(EditPhotos.this, "Failed to upload image: " + fileUri.getLastPathSegment(), Toast.LENGTH_SHORT).show());
        }
    }

    // Method to update business photos in Firestore
    private void updateBusinessPhotos(List<String> newImageUrls) {
        FirebaseFirestore db = FirebaseFirestore.getInstance(); // Get Firestore instance
        FirebaseAuth mAuth = FirebaseAuth.getInstance(); // Get Firebase Authentication instance
        FirebaseUser user = mAuth.getCurrentUser(); // Get current authenticated user
        if (user != null) {
            String userId = user.getUid(); // Get user ID
            CollectionReference jobsCollectionRef = db.collection("jobs"); // Reference to the jobs collection

            jobsCollectionRef.document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Get the business array from the document
                    List<Map<String, Object>> businessArray = (List<Map<String, Object>>) documentSnapshot.get("buisnessAraay");
                    if (businessArray != null && position < businessArray.size()) {
                        // Get the business at the specified position
                        Map<String, Object> business = businessArray.get(position);
                        if (business != null) {
                            business.put("imageUrls", newImageUrls); // Update image URLs in the business object

                            // Update the business array in Firestore
                            businessArray.set(position, business);
                            db.collection("jobs").document(userId)
                                    .update("buisnessAraay", businessArray)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(EditPhotos.this, "Business photos updated successfully", Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK); // Set result to OK
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(EditPhotos.this, "Failed to update business photos", Toast.LENGTH_SHORT).show();
                                        Log.e("UpdateError", e.getMessage(), e); // Log error
                                    });
                        }
                    } else {
                        Toast.makeText(EditPhotos.this, "Business data not found or invalid position", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditPhotos.this, "Document does not exist", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(EditPhotos.this, "Error fetching business data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FetchError", e.getMessage(), e); // Log error
            });
        } else {
            Toast.makeText(EditPhotos.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to finish the


    private void finishActivity() {
        // Finish the current activity and return to EditBusiness activity
        setResult(RESULT_OK);
        finish();
    }
}
