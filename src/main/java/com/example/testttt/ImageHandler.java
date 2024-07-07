package com.example.testttt;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.HashMap;
import java.util.Map;

public class ImageHandler {

    private static final int PICK_IMAGE = 100; // Request code for picking an image
    private final AppCompatActivity activity; // Reference to the activity that uses this handler
    private final ImageView imageView; // ImageView to display the user's image
    private final FirebaseUser currentUser; // Currently authenticated Firebase user
    private final FirebaseFirestore db; // Firestore database instance
    private final FirebaseStorage storage; // Firebase Storage instance

    // Constructor to initialize the ImageHandler with the activity and ImageView
    public ImageHandler(AppCompatActivity activity, ImageView imageView) {
        this.activity = activity;
        this.imageView = imageView;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser(); // Get the current authenticated user
        this.db = FirebaseFirestore.getInstance(); // Get the Firestore instance
        this.storage = FirebaseStorage.getInstance(); // Get the Firebase Storage instance
    }

    // Method to load the user's image from Firestore
    public void loadUserImage() {
        db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get the image URL from the user's document
                String imageUrl = task.getResult().getString("imageUrl");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // Load the image into the ImageView using Glide
                    Glide.with(activity).load(imageUrl).into(imageView);
                }
            }
        });
    }

    // Method to open the image picker
    public void openImagePicker(Activity activity) {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI); // Create an intent to pick an image
        activity.startActivityForResult(gallery, PICK_IMAGE); // Start the image picker activity
    }

    // Method to handle the result of the image picker
    @Nullable
    public Uri handleImageResult(int requestCode, int resultCode, @Nullable Intent data, Activity activity) {
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            // Get the selected image URI
            Uri imageUri = data.getData();
            imageView.setImageURI(imageUri); // Set the image URI to the ImageView
            return imageUri; // Return the image URI
        }
        return null; // Return null if no image was selected
    }

    // Method to upload the selected image to Firebase Storage
    public void uploadImage(Uri fileUri, Runnable onSuccess) {
        if (fileUri == null) return; // If file URI is null, do nothing

        // Get a reference to the storage
        StorageReference storageRef = storage.getReference();
        // Create a reference to the user's image
        StorageReference userImageRef = storageRef.child("images/" + currentUser.getUid() + ".jpg");

        // Upload the file to Firebase Storage
        userImageRef.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get the download URL of the uploaded image
                userImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageUrl = uri.toString(); // Convert the URI to a string
                        // Create a map to update the user's document
                        Map<String, Object> update = new HashMap<>();
                        update.put("imageUrl", imageUrl);

                        // Update the user's document with the new image URL
                        db.collection("users").document(currentUser.getUid()).set(update, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> onSuccess.run()) // Call onSuccess runnable on success
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle failure
            }
        });
    }
}

