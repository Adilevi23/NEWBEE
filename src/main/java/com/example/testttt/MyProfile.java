package com.example.testttt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import android.widget.ImageView;
import android.util.Patterns;

public class MyProfile extends AppCompatActivity {

    TextInputEditText first_name, last_name, phone_number;
    Button btn_save_profile, back;
    String gender;
    FirebaseAuth mAuth;
    ImageView imageView;
    ImageHandler imageHandler;
    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_profile);

        // Initialize Firebase authentication
        first_name = findViewById(R.id.first_name);
        last_name = findViewById(R.id.last_name);
        phone_number = findViewById(R.id.phone_number);
        gender = "";

        // Initialize radio buttons
        RadioGroup radioGroupGender = findViewById(R.id.radioGroupGender);
        RadioButton radioButtonMale = findViewById(R.id.radio_male);
        RadioButton radioButtonFemale = findViewById(R.id.radio_female);
        btn_save_profile = findViewById(R.id.btn_save_profile);
        back = findViewById(R.id.back);

        imageView = findViewById(R.id.imageView);
        imageHandler = new ImageHandler(this, imageView);

        imageHandler.loadUserImage();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        CollectionReference usersCollectionRef = db.collection("users");
        String userId = user.getUid();
        DocumentReference docRef = usersCollectionRef.document(userId);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String phoneNumber = document.getString("phoneNumber");
                        gender = document.getString("gender");

                        first_name.setText(firstName);
                        last_name.setText(lastName);
                        phone_number.setText(phoneNumber);

                        if (Objects.equals(gender, "male")) {
                            radioGroupGender.check(R.id.radio_male);
                        }
                        if (Objects.equals(gender, "female")) {
                            radioGroupGender.check(R.id.radio_female);
                        }
                    }
                }
            }
        });

        radioGroupGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_male) {
                    gender = "male";
                } else if (checkedId == R.id.radio_female) {
                    gender = "female";
                }
            }
        });

        // Set ImageView clickable to open image picker
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageHandler.openImagePicker(MyProfile.this);
            }
        });

        btn_save_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String first_name_str = String.valueOf(first_name.getText()).trim();
                String last_name_str = String.valueOf(last_name.getText()).trim();
                String phone_str = String.valueOf(phone_number.getText()).trim();

                // Validate first name
                if (TextUtils.isEmpty(first_name_str)) {
                    showAlertDialog("Enter First Name");
                    return;
                }

                // Validate last name
                if (TextUtils.isEmpty(last_name_str)) {
                    showAlertDialog("Enter Last Name");
                    return;
                }

                // Validate phone number
                if (TextUtils.isEmpty(phone_str)) {
                    showAlertDialog("Enter Phone Number");
                    return;
                }
                if (!phone_str.matches("\\d{10}") && !phone_str.matches("\\d{9}")) {  // Ensure phone number is exactly 9 or 10 digits
                    showAlertDialog("Enter a valid 9 or 10-digit Phone Number");
                    return;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", first_name_str);
                updates.put("lastName", last_name_str);
                updates.put("phoneNumber", phone_str);
                updates.put("gender", gender);

                docRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (selectedImageUri != null) {
                            imageHandler.uploadImage(selectedImageUri, new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MyProfile.this, "Profile Successfully Updated", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } else {
                            Toast.makeText(MyProfile.this, "Profile Successfully Updated", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MyProfile.this, "Profile Update Failed", Toast.LENGTH_SHORT).show();
                        Log.d("Firestore", "Profile Update Failed", e);
                    }
                });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        selectedImageUri = imageHandler.handleImageResult(requestCode, resultCode, data, this);
    }

    // Helper method to show an alert dialog
    private void showAlertDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_myprofile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

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
