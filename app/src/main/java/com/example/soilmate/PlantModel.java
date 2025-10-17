package com.example.soilmate;

import java.io.Serializable;
import java.util.List;
public class PlantModel implements Serializable {
    private String id;
    private String name;
    private String imageUrl;
    private String actualImageUrl;
    private String minSoilMoisture;
    private String userId; // Add this field
    private List<String> wateringSchedules;

    // Add sensor-related data
    private int soilMoisture;
    private int humidity;
    private float phLevel;
    private int waterLevel;
    private int nitrogen;
    private int phosphorus;
    private int potassium;

    // Default constructor (required for Firestore)
    public PlantModel() {}

    public PlantModel(String id, String name, String imageUrl, String actualImageUrl, String minSoilMoisture, String userId, List<String> wateringSchedules,
                      int soilMoisture, int humidity, float phLevel, int waterLevel, int nitrogen, int phosphorus, int potassium) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.actualImageUrl = actualImageUrl;
        this.minSoilMoisture = minSoilMoisture;
        this.userId = userId; // Initialize userId
        this.wateringSchedules = wateringSchedules;
        this.soilMoisture = soilMoisture;
        this.humidity = humidity;
        this.phLevel = phLevel;
        this.waterLevel = waterLevel;
        this.nitrogen = nitrogen;
        this.phosphorus = phosphorus;
        this.potassium = potassium;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public String getActualImageUrl() { return actualImageUrl; }
    public String getMinSoilMoisture() { return minSoilMoisture; }
    public String getUserId() { return userId; } // Getter for userId
    public void setUserId(String userId) { this.userId = userId; } // Setter for userId
    public List<String> getWateringSchedules() { return wateringSchedules; }

    // Getters for sensor values
    public int getSoilMoisture() { return soilMoisture != 0 ? soilMoisture : 1; }
    public int getHumidity() { return humidity != 0 ? humidity : 1; }
    public float getPhLevel() { return phLevel != 0.0f ? phLevel : 7.0f; }
    public int getWaterLevel() { return waterLevel != 0 ? waterLevel : 1; }
    public int getNitrogen() { return nitrogen != 0 ? nitrogen : 1; }
    public int getPhosphorus() { return phosphorus != 0 ? phosphorus : 1; }
    public int getPotassium() { return potassium != 0 ? potassium : 1; }
}