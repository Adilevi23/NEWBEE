package com.example.testttt;

// Imports
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Register extends AppCompatActivity {

    // Declaring UI elements
    TextInputEditText editTextEmail, editTextPassword, confirmPassword, first_name, last_name, phone_number;
    Button btn_reg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    String gender;

    private static final String TAG = "RegisterActivity";

    // Check if user is already logged in onStart
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);

        // Set layout
        setContentView(R.layout.activity_register);

        // Initialize Firebase authentication
        mAuth = FirebaseAuth.getInstance();

        // Find views by their IDs
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        btn_reg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);
        first_name = findViewById(R.id.first_name);
        last_name = findViewById(R.id.last_name);
        phone_number = findViewById(R.id.phone_number);
        gender = "";

        // Initialize radio buttons
        RadioGroup radioGroupGender = findViewById(R.id.radioGroupGender);
        RadioButton radioButtonMale = findViewById(R.id.radio_male);
        RadioButton radioButtonFemale = findViewById(R.id.radio_female);

        // Set click listener for "Login Now" text
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        // Adjust layout for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextInputLayout textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        textInputLayoutPassword.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextPassword.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    editTextPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    textInputLayoutPassword.setEndIconDrawable(R.drawable.ic_eye_off);  // change to eye open icon
                } else {
                    editTextPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    textInputLayoutPassword.setEndIconDrawable(R.drawable.ic_eye_on);  // change to eye closed icon
                }
                editTextPassword.setSelection(editTextPassword.getText().length());  // to keep cursor at the end of text
            }
        });

        TextInputLayout textInputLayoutConfirmPassword = findViewById(R.id.textInputLayoutConfirmPassword);
        textInputLayoutConfirmPassword.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmPassword.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    confirmPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    textInputLayoutConfirmPassword.setEndIconDrawable(R.drawable.ic_eye_off);  // change to eye open icon
                } else {
                    confirmPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    textInputLayoutConfirmPassword.setEndIconDrawable(R.drawable.ic_eye_on);  // change to eye closed icon
                }
                confirmPassword.setSelection(confirmPassword.getText().length());  // to keep cursor at the end of text
            }
        });


        // Listen for radio button changes
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

        // Handle registration button click
        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String confirmPasswordText = confirmPassword.getText().toString().trim();
                String firstName = first_name.getText().toString().trim();
                String lastName = last_name.getText().toString().trim();
                String phoneNumber = phone_number.getText().toString().trim();

                // Validate first name
                if (TextUtils.isEmpty(firstName)) {
                    showAlertDialog("Enter First Name");
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Validate last name
                if (TextUtils.isEmpty(lastName)) {
                    showAlertDialog("Enter Last Name");
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Validate phone number
                if (TextUtils.isEmpty(phoneNumber)) {
                    showAlertDialog("Enter Phone Number");
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (!phoneNumber.matches("\\d{10}") && !phoneNumber.matches("\\d{9}")) {  // Ensure phone number is exactly 10 digits
                    showAlertDialog("Enter a valid 9 or 10-digit Phone Number");
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Validate email
                if (TextUtils.isEmpty(email)) {
                    showAlertDialog("Enter Email");
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    showAlertDialog("Enter a valid Email");
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Validate password
                if (TextUtils.isEmpty(password)) {
                    showAlertDialog("Enter Password");
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (password.length() < 6 || password.length() > 12) {
                    showAlertDialog("Password must be between 6 and 12 characters");
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Validate confirm password
                if (TextUtils.isEmpty(confirmPasswordText)) {
                    showAlertDialog("Enter Password Confirmation");
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (!password.equals(confirmPasswordText)) {
                    showAlertDialog("Passwords do not match");
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Create user with email and password
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        // If user is successfully created, add user details to Firestore
                                        String userId = user.getUid();
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        DocumentReference userRef = db.collection("users").document(userId);

                                        User userData = new User(email, password, firstName, lastName, phoneNumber, gender);

                                        userRef.set(userData)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // User details added successfully
                                                        Toast.makeText(Register.this, "Account created",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Error occurred while adding user details
                                                        Toast.makeText(Register.this, "Failed to add user details",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(Register.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }

    // Helper method to show an alert dialog
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
}
