package com.example.testttt;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    // Declare UI elements
    private TextInputEditText oldPassword, newPassword, confirmPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge display
        setContentView(R.layout.activity_change_password); // Set the content view

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Find views by their ID
        oldPassword = findViewById(R.id.oldPassword);
        newPassword = findViewById(R.id.newPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        Button btnChangePassword = findViewById(R.id.btn_change_password);
        progressBar = findViewById(R.id.progressBar);

        // Set up password visibility toggles
        setupPasswordVisibilityToggle((TextInputLayout) findViewById(R.id.textInputLayoutOldPassword), oldPassword);
        setupPasswordVisibilityToggle((TextInputLayout) findViewById(R.id.textInputLayoutNewPassword), newPassword);
        setupPasswordVisibilityToggle((TextInputLayout) findViewById(R.id.textInputLayoutConfirmPassword), confirmPassword);

        // Set onClick listener for the change password button
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        // Adjust layout for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Method to set up the password visibility toggle
    private void setupPasswordVisibilityToggle(TextInputLayout textInputLayout, TextInputEditText editText) {
        textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle between password visible and hidden states
                if (editText.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    textInputLayout.setEndIconDrawable(R.drawable.ic_eye_on);  // Set eye open icon
                } else {
                    editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    textInputLayout.setEndIconDrawable(R.drawable.ic_eye_off);  // Set eye closed icon
                }
                editText.setSelection(editText.getText().length());  // Keep cursor at the end
            }
        });
    }

    // Method to handle password change
    private void changePassword() {
        String oldPass = oldPassword.getText().toString().trim();
        String newPass = newPassword.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(oldPass)) {
            showAlertDialog("Enter your old password");
            return;
        }
        if (TextUtils.isEmpty(newPass) || newPass.length() < 6 || newPass.length() > 12) {
            showAlertDialog("New password must be between 6 and 12 characters");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            showAlertDialog("New password and confirmation do not match");
            return;
        }

        progressBar.setVisibility(View.VISIBLE); // Show progress bar

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Update password
                        user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressBar.setVisibility(View.GONE); // Hide progress bar
                                if (task.isSuccessful()) {
                                    // Password update successful
                                    Toast.makeText(ChangePassword.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                    Intent intent = new Intent(ChangePassword.this, Login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Password update failed
                                    Toast.makeText(ChangePassword.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        // Reauthentication failed
                        progressBar.setVisibility(View.GONE); // Hide progress bar
                        showAlertDialog("Old password is incorrect");
                    }
                }
            });
        }
    }

    // Method to show an alert dialog
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_change_password, menu); // Inflate the menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Handle menu item selection
        if (id == R.id.MyProfile) {
            Intent intent = new Intent(getApplicationContext(), MyProfile.class);
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
