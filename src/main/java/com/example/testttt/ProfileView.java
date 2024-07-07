package com.example.testttt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProfileView extends AppCompatActivity {
    TextView first_name, last_name, phone_number, genderView;
    Button back;
    String gender;
    FirebaseAuth mAuth;
    ImageView imageView;
    ImageHandler imageHandler;
    Uri selectedImageUri;
    private RecyclerView businessRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        // Get the Intent that started this activity and extract the profile_id
        Intent intent = getIntent();
        String profile_id = intent.getStringExtra("profile_id");

        // Log the profile_id to debug
        Log.d("ProfileView", "Received profile_id: " + profile_id);

        if (profile_id == null) {
            Toast.makeText(this, "Profile ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize UI elements and fetch data from Firestore
        initializeUI(profile_id);
    }

    private void initializeUI(String profile_id) {
        // Initialize UI elements and Firebase components
        TextView first_name = findViewById(R.id.first_name);
        TextView last_name = findViewById(R.id.last_name);
        TextView phone_number = findViewById(R.id.phone_number);
        TextView genderView = findViewById(R.id.genderView);
        ImageView imageView = findViewById(R.id.imageView);
        Button back = findViewById(R.id.back);
        businessRecyclerView = findViewById(R.id.businessRecyclerView);
        businessRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(profile_id);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String phoneNumber = document.getString("phoneNumber");
                        String gender = document.getString("gender");
                        String imageUrl = document.getString("imageUrl");

                        first_name.setText(firstName);
                        last_name.setText(lastName);
                        phone_number.setText(phoneNumber);
                        genderView.setText(gender);
                        // Load image using an image loading library like Glide or Picasso
                        Glide.with(ProfileView.this).load(imageUrl).into(imageView);
                    } else {
                        Log.d("ProfileView", "No such document");
                    }
                } else {
                    Log.d("ProfileView", "get failed with ", task.getException());
                }
            }
        });

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
                                if (business.getUid()!=null){
                                    if(business.getUid().equals(profile_id)){
                                        allBusinesses.add(business);
                                    }
                                }

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
        // Set the back button listener
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Client.class);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.MyProfile){
            Intent intent = new Intent(getApplicationContext(), MyProfile.class);
            startActivity(intent);
            finish();
        }
        if(id == R.id.SwitchToServiceProvider){
            Intent intent = new Intent(getApplicationContext(), ServiceProvider.class);
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