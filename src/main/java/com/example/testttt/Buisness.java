package com.example.testttt;

import android.net.Uri;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Buisness {

    // Business attributes
    String bis_name;
    String buisness_category;
    String owner_name;
    String phone_number;
    String email;
    String city;
    int min_price;
    int max_price;
    String description;
    String Uid;
    // List to store image URLs
    private List<String> imageUrls;

    // Constructor with basic details
    public Buisness(String bis_name, String buisness_category, String owner_name, String phone_number, String email, String city, int min_price, int max_price, String Uid){
        this.bis_name = bis_name;
        this.buisness_category = buisness_category;
        this.owner_name = owner_name;
        this.phone_number = phone_number;
        this.email = email;
        this.city = city;
        this.min_price = min_price;
        this.max_price = max_price;
        this.Uid = Uid;
    }

    // Constructor with additional details including description and image URLs
    public Buisness(String bis_name, String buisness_category, String owner_name, String phone_number, String email, String city, int min_price, int max_price, String description, String Uid, List<String> imageUrls) {
        this.bis_name = bis_name;
        this.buisness_category = buisness_category;
        this.owner_name = owner_name;
        this.phone_number = phone_number;
        this.email = email;
        this.city = city;
        this.min_price = min_price;
        this.max_price = max_price;
        this.description = description;
        this.Uid = Uid;
        this.imageUrls = imageUrls;
    }

    // No-argument constructor
    public Buisness() {
    }

    // Constructor to initialize from a map
    public Buisness(Map<String, Object> map) {
        this.bis_name = (String) map.get("bis_name");
        this.buisness_category = (String) map.get("buisness_category");
        this.owner_name = (String) map.get("owner_name");
        this.phone_number = (String) map.get("phone_number");
        this.email = (String) map.get("email");
        this.city = (String) map.get("city");
        // Handle conversion from Double to Integer for min_price and max_price
        Number minPriceObj = (Number) map.get("min_price");
        Number maxPriceObj = (Number) map.get("max_price");
        if (minPriceObj != null) {
            this.min_price = minPriceObj.intValue();
        } else {
            this.min_price = 0; // or handle default value as per your app logic
        }
        if (maxPriceObj != null) {
            this.max_price = maxPriceObj.intValue();
        } else {
            this.max_price = 0; // or handle default value as per your app logic
        }
        // Initialize imageUrls list
        this.imageUrls = new ArrayList<>();
        if (map.containsKey("imageUrls")) {
            List<String> imageUrlsFromMap = (List<String>) map.get("imageUrls");
            if (imageUrlsFromMap != null) {
                this.imageUrls.addAll(imageUrlsFromMap);
            }
        }
        this.description = (String) map.get("description");
        this.Uid = (String) map.get("uid");
    }

    // Getter and setter methods
    public String getBis_name() {
        return bis_name;
    }

    public void setBis_name(String bis_name) {
        this.bis_name = bis_name;
    }

    public String getBuisness_category() {
        return buisness_category;
    }

    public void setBuisness_category(String buisness_category) {
        this.buisness_category = buisness_category;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public void setOwner_name(String owner_name) {
        this.owner_name = owner_name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getMin_price() {
        return min_price;
    }

    public void setMin_price(int min_price) {
        this.min_price = min_price;
    }

    public int getMax_price() {
        return max_price;
    }

    public void setMax_price(int max_price) {
        this.max_price = max_price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUid() {
        return this.Uid;
    }

    public void setUid(String Uid) {
        this.Uid = Uid;
    }

    // Getter and setter for image URLs
    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
