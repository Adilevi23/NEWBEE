package com.example.testttt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class AddPhotos extends AppCompatActivity {

    // Request code for picking images
    private static final int PICK_IMAGES_REQUEST_CODE = 1;
    // List to hold URIs of selected images
    private ArrayList<Uri> imageUris = new ArrayList<>();
    // Container to display selected images
    private LinearLayout imagesContainer;
    // Button to save selected photos
    private Button savePhotosButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photos);

        // Initialize views
        imagesContainer = findViewById(R.id.images_container);
        savePhotosButton = findViewById(R.id.btn_save_photos);

        // Button to select images
        Button selectImagesButton = findViewById(R.id.btn_select_photos);
        selectImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to pick images
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGES_REQUEST_CODE);
            }
        });

        // Button to save selected photos and return to the previous activity
        savePhotosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create result intent and attach selected images
                Intent resultIntent = new Intent();
                resultIntent.putParcelableArrayListExtra("selectedImages", imageUris);
                setResult(RESULT_OK, resultIntent);
                // Finish the activity and return to the previous one
                finish();
            }
        });
    }
    // Handle multiple selected images
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {

                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        try {
                            // Copy image to app's private storage
                            Uri copiedUri = copyImageToPrivateStorage(imageUri);
                            if (copiedUri != null) {
                                imageUris.add(copiedUri);
                                displaySelectedImage(copiedUri);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (data.getData() != null) { // Handle single selected image
                    Uri imageUri = data.getData();
                    try {
                        // Copy image to app's private storage
                        Uri copiedUri = copyImageToPrivateStorage(imageUri);
                        if (copiedUri != null) {
                            imageUris.add(copiedUri);
                            displaySelectedImage(copiedUri);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Method to copy selected image to app's private storage
    private Uri copyImageToPrivateStorage(Uri imageUri) throws IOException {
        // Create a new file in app's private storage
        File privateDir = getApplicationContext().getFilesDir();
        File destFile = new File(privateDir, "image_" + System.currentTimeMillis() + ".jpg");

        // Copy image data to the new file
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri);
             OutputStream outputStream = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            // Return URI of the copied file
            return Uri.fromFile(destFile);
        }
    }

    // Method to display selected image in the container
    private void displaySelectedImage(Uri imageUri) {
        // Create a new ImageView
        ImageView imageView = new ImageView(this);
        imageView.setImageURI(imageUri);

        // Set the size of each image
        int imageSize = getResources().getDimensionPixelSize(R.dimen.image_size); // define this in dimens.xml
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                imageSize,
                imageSize
        );

        // Set margins around each image
        int marginSize = getResources().getDimensionPixelSize(R.dimen.image_margin); // define this in dimens.xml
        layoutParams.setMargins(marginSize, marginSize, marginSize, marginSize);

        imageView.setLayoutParams(layoutParams);
        // Add the ImageView to the container
        imagesContainer.addView(imageView);
    }
}

