package com.example.testttt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceProvider extends AppCompatActivity {

    Button add_job_btn, back;
    FirebaseAuth mAuth;
    private RecyclerView businessRecyclerView;
    private BusinessAdapter businessAdapter;

    public static void editButton(Context context, int position) {
        Intent intent = new Intent(context, EditBuisness.class);
        intent.putExtra("position", position); // Add position as an extra
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_service_provider);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        add_job_btn = findViewById(R.id.add_job_btn);
        back = findViewById(R.id.back);

        businessRecyclerView = findViewById(R.id.businessRecyclerView);
        businessRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        String userId = user.getUid();
        CollectionReference jobsCollectionRef = db.collection("jobs");

        DocumentReference docRef = jobsCollectionRef.document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> businessNames = new ArrayList<>();

                        // Retrieve the data from Firestore
                        Object businessData = document.get("buisnessAraay");
                        List<Map<String, Object>> businessArray = null;

// Check if the retrieved data is of the expected type
                        if (businessData instanceof List) {
                            // Cast the data to the correct type
                            businessArray = (List<Map<String, Object>>) businessData;

                            // Iterate over each object in the Business array
                            for (Map<String, Object> business : businessArray) {
                                // Assuming the bis_name field is a String
                                String businessName = (String) business.get("bis_name");
                                businessNames.add(businessName);
                                Log.d("name:", businessName);
                            }
                        } else {
                            // Handle the case when the data is not of the expected type
                            Log.d("Invalid Data Type", "Data is not of type List<Map<String, Object>>");
                            // You may want to display an error message or handle this case differently
                        }
                        // Now you have the businessNames List<String> containing the bis_name values
                        // You can pass this list to the adapter or use it as needed
                        for (String b : businessNames) {
                            Log.d("name in businessNames", b);
                        }
                        businessAdapter = new BusinessAdapter(businessNames, businessArray);
                        businessRecyclerView.setAdapter(businessAdapter);
                    } else {
                        // Handle the case when the document doesn't exist
                    }
                } else {
                    // Handle the case when the task is not successful
                }
            }
        });

        //add job btn
        add_job_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddBuisness.class);
                startActivity(intent);
                finish();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
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
        if (id == R.id.SwitchToClient){
            Intent intent = new Intent(getApplicationContext(),Client.class);
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