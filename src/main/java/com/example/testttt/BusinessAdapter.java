package com.example.testttt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class BusinessAdapter extends RecyclerView.Adapter<BusinessAdapter.ViewHolder> {
    // List to store business names
    private List<String> businessList;
    // List to store business objects (maps)
    private List<Map<String,Object>> businessObjects;
    // Buttons for editing and deleting business entries
    Button editButton, deleteButton;
    // Context for accessing resources and performing operations
    Context context;

    // Constructor to initialize the adapter with a list of business names and objects
    public BusinessAdapter(List<String> businessList, List<Map<String, Object>> businessObjects) {
        this.businessList = businessList;
        this.businessObjects = businessObjects;
    }

    // Constructor to initialize the adapter with a list of business objects only
    public BusinessAdapter(List<Map<String, Object>> businessObjects) {
        this.businessObjects = businessObjects;
    }

    // ViewHolder class to represent each item in the RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {
        // TextView to display the business name
        TextView nameTextView;

        // Constructor to initialize the ViewHolder
        public ViewHolder(View itemView) {
            super(itemView);
            // Find views by their IDs
            nameTextView = itemView.findViewById(R.id.nameTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            context = itemView.getContext();

            // Set click listener for the edit button
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the adapter position and call the edit function
                    int position = getAdapterPosition();
                    ServiceProvider.editButton(itemView.getContext(), position);
                }
            });

            // Set click listener for the delete button
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        // Show confirmation dialog before deletion
                        showDeleteConfirmationDialog(position);
                    }
                }
            });
        }
    }

    // Method to show a confirmation dialog before deleting a business
    private void showDeleteConfirmationDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context); // 'context' should be your activity context
        builder.setMessage("Are you sure you want to delete the business?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Call method to delete business from database
                deleteBusinessFromDB(position);
                Log.d("position: ", String.valueOf(position));
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Method to delete a business from the database
    private void deleteBusinessFromDB(int position){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        String userId = user.getUid();
        CollectionReference jobsCollectionRef = db.collection("jobs");

        DocumentReference docRef = jobsCollectionRef.document(userId);
        // Remove the business object from the "businessArray" field in Firestore
        docRef.update("buisnessAraay", FieldValue.arrayRemove(this.businessObjects.get(position)))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Object successfully removed from the array
                        businessObjects.remove(position); // Remove from business objects list
                        businessList.remove(position); // Remove from business names list
                        notifyItemRemoved(position); // Notify the adapter that an item has been removed
                        notifyItemRangeChanged(position, businessList.size()); // Notify any other changes in the list
                        Toast.makeText(context, "Business removed from array", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the failure
                        Toast.makeText(context, "Error removing business from array", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each business object
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_business_object, parent, false);
        return new ViewHolder(view);
    }

    // Method to replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the business name for the current position
        String businessName = businessList.get(position);
        // Set the business name to the TextView
        holder.nameTextView.setText(businessName);
    }

    // Method to return the size of the business list (invoked by the layout manager)
    @Override
    public int getItemCount() {
        // Return the size of the business list if it's not null
        if (businessList != null){
            return businessList.size();
        }
        // Return 0 if the business list is null
        else {return 0;}
    }
}
