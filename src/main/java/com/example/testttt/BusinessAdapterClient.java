package com.example.testttt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class BusinessAdapterClient extends RecyclerView.Adapter<BusinessAdapterClient.BusinessViewHolder> {

    // List to hold the business objects
    private List<Buisness> businessList;

    // Constructor to initialize the adapter with a list of business objects
    public BusinessAdapterClient(List<Buisness> businessList) {
        this.businessList = businessList;
    }

    @NonNull
    @Override
    public BusinessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_business_client, parent, false);
        return new BusinessViewHolder(view);
    }

    // Method to update the adapter with a filtered list of business objects
    public void filterList(List<Buisness> filteredList) {
        Log.d("filterList", "Updating adapter with filtered list of size: " + filteredList.size());
        businessList = filteredList;
        notifyDataSetChanged(); // Notify the adapter that the data set has changed
    }

    @Override
    public void onBindViewHolder(@NonNull BusinessViewHolder holder, int position) {
        // Get the business object at the current position
        Buisness business = businessList.get(position);
        // Set the business details to the respective TextViews
        holder.textViewBisName.setText(business.getBis_name());
        holder.textViewBusinessCategory.setText(business.getBuisness_category());
        holder.textViewCity.setText(business.getCity());
        holder.textViewMinPrice.setText(String.format(Locale.getDefault(), "%d", business.getMin_price()));
        holder.textViewMaxPrice.setText(String.format(Locale.getDefault(), "%d", business.getMax_price()));
        // Set click listener for the details button
        holder.detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the edit dialog with business details
                showEditDialog(business, holder.itemView.getContext());
            }
        });
    }

    // Method to show an edit dialog with business details
    private void showEditDialog(Buisness business, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_details_business, null);
        builder.setView(dialogView);

        // Find and set the business details in the dialog
        TextView editTextOwnerName = dialogView.findViewById(R.id.editTextOwnerName);
        TextView editTextPhoneNumber = dialogView.findViewById(R.id.editTextPhoneNumber);
        TextView editTextEmail = dialogView.findViewById(R.id.editTextEmail);
        TextView min = dialogView.findViewById(R.id.min);
        TextView max = dialogView.findViewById(R.id.max);
        TextView description = dialogView.findViewById(R.id.description);
        LinearLayout imageContainer = dialogView.findViewById(R.id.imageContainer);

        editTextOwnerName.setText(business.getOwner_name());
        editTextPhoneNumber.setText(business.getPhone_number());
        editTextEmail.setText(business.getEmail());
        min.setText(String.valueOf(business.getMin_price()));
        max.setText(String.valueOf(business.getMax_price()));
        description.setText(business.getDescription());

        // Load business images into the dialog
        List<String> imageUrls = business.getImageUrls();
        for (String imageUrl : imageUrls) {
            ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    200, 200
            );
            lp.setMargins(10, 0, 10, 0);
            imageView.setLayoutParams(lp);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.get().load(imageUrl).into(imageView);
            imageContainer.addView(imageView);
        }

        // Make email address clickable and blue
        String emailAddress = business.getEmail();
        if (emailAddress != null && !emailAddress.isEmpty()) {
            SpannableString spannableEmailAddress = new SpannableString(emailAddress);
            spannableEmailAddress.setSpan(new ForegroundColorSpan(Color.parseColor("#007FFF")), 0, emailAddress.length(), 0); // Blue color
            spannableEmailAddress.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:" + emailAddress));
                    context.startActivity(emailIntent);
                }
            }, 0, emailAddress.length(), 0);
            editTextEmail.setText(spannableEmailAddress);
            editTextEmail.setMovementMethod(LinkMovementMethod.getInstance());
        }

        // Make phone number clickable and light blue
        String phoneNumber = business.getPhone_number();
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            SpannableString spannablePhoneNumber = new SpannableString(phoneNumber);
            spannablePhoneNumber.setSpan(new ForegroundColorSpan(Color.parseColor("#007FFF")), 0, phoneNumber.length(), 0); // Light blue color
            editTextPhoneNumber.setText(spannablePhoneNumber);
            editTextPhoneNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:" + phoneNumber));
                    context.startActivity(dialIntent);
                }
            });
        }

        // Set up the button to send a WhatsApp message
        Button buttonWhatsApp = dialogView.findViewById(R.id.buttonWhatsApp);
        buttonWhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = business.getPhone_number();
                String countryCode = "+972"; // israel code
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.whatsapp");
                    String url = "https://api.whatsapp.com/send?phone=" + countryCode + phoneNumber + "&text=" + Uri.encode("Hey, I am contacting you through NEWBEE App and I would like to receive more information about the service");
                    intent.setData(Uri.parse(url));
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Phone number is not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up the button to go to the business profile
        Button buttonGoToProfile = dialogView.findViewById(R.id.buttonGoToProfile);
        buttonGoToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String profile_id = business.getUid();
                if (profile_id != null) {
                    Intent intent = new Intent(context, ProfileView.class);
                    intent.putExtra("profile_id", profile_id);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Profile ID is null", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set the cancel button for the dialog
        builder.setNegativeButton("Cancel", null);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Method to get the total number of business objects
    @Override
    public int getItemCount() {
        return businessList.size();
    }

    // ViewHolder class to represent each item in the RecyclerView
    public static class BusinessViewHolder extends RecyclerView.ViewHolder {
        // TextViews to display the business details
        TextView textViewBisName, textViewBusinessCategory, textViewCity, textViewMinPrice, textViewMaxPrice;
        // Button to show details
        Button detailsButton;

        // Constructor to initialize the ViewHolder
        public BusinessViewHolder(View itemView) {
            super(itemView);
            // Find views by their IDs
            textViewBisName = itemView.findViewById(R.id.textViewBisName);
            textViewBusinessCategory = itemView.findViewById(R.id.textViewBusinessCategory);
            textViewCity = itemView.findViewById(R.id.textViewCity);
            textViewMinPrice = itemView.findViewById(R.id.textViewMinPrice);
            textViewMaxPrice = itemView.findViewById(R.id.textViewMaxPrice);
            detailsButton = itemView.findViewById(R.id.editButton);

            // Set click listener for the details button
            detailsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // Use an interface or directly call a method to handle the click event
                        // For example: listener.onEditClicked(position);
                    }
                }
            });
        }
    }
}
