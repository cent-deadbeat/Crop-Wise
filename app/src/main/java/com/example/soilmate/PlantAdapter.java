package com.example.soilmate;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {
    private Context context;
    private List<PlantModel> plantList = new ArrayList<>();
    private boolean isSelectionMode = false;
    private Set<Integer> selectedItems = new HashSet<>();
    private MyPlants activity;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private StorageReference storageReference;

    public PlantAdapter(Context context, List<PlantModel> plantList, MyPlants activity) {
        this.context = context;
        this.plantList = plantList;
        this.activity = activity;
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance(); // Firestore instead of Realtime Database

    }


    private void fetchPlantsFromFirestore() {
        String userId = auth.getCurrentUser().getUid();

        firestore.collection("plants")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FirestoreDebug", "❌ Firestore error: " + error.getMessage());
                        return;
                    }

                    plantList.clear();
                    if (value != null) {
                        for (DocumentSnapshot document : value.getDocuments()) {
                            PlantModel plant = document.toObject(PlantModel.class);
                            if (plant != null) {
                                // ✅ Ensure ID is set
                                plant.setId(document.getId());

                                Log.d("FirestoreDebug", "✅ Plant ID: " + plant.getId());
                                plantList.add(plant);
                            }
                        }
                    }
                    notifyDataSetChanged();
                });
    }






    public void setSelectionMode(boolean enabled) {
        isSelectionMode = enabled;
        if (!enabled) selectedItems.clear();
        notifyDataSetChanged();
        activity.toggleSelectionMode(enabled);
    }

    public void updateList(List<PlantModel> newList) {
        plantList.clear();
        plantList.addAll(newList);
        notifyDataSetChanged();
    }




    public Set<Integer> getSelectedItems() {
        return selectedItems;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_plant, parent, false);

        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        PlantModel plant = plantList.get(position);
        holder.plantName.setText(plant.getName());
        // Load the image using Glide
        Glide.with(context)
                .load(plant.getImageUrl())
                .placeholder(R.drawable.plant_icon) // Placeholder while loading
                .error(R.drawable.plant_icon)
                .into(holder.plantImage);

        holder.checkBox.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        holder.checkBox.setChecked(selectedItems.contains(position));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedItems.add(position);
            else selectedItems.remove(position);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                setSelectionMode(true);
                selectedItems.add(position);
                notifyDataSetChanged();
            }
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlantDetails.class);
            intent.putExtra("plantId", plant.getId());
            intent.putExtra("plantName", plant.getName());
            intent.putExtra("imageUrl", plant.getImageUrl());
            intent.putExtra("actualImageUrl", plant.getActualImageUrl()); // Actual image
            intent.putExtra("minSoilMoisture", plant.getMinSoilMoisture());

            // Pass other details
            intent.putExtra("soilMoisture", plant.getSoilMoisture());
            intent.putExtra("humidity", plant.getHumidity());
            intent.putExtra("phLevel", plant.getPhLevel());
            intent.putExtra("waterLevel", plant.getWaterLevel());
            intent.putExtra("nitrogen", plant.getNitrogen());
            intent.putExtra("phosphorus", plant.getPhosphorus());
            intent.putExtra("potassium", plant.getPotassium());

            // Pass wateringSchedules as an ArrayList
            if (plant.getWateringSchedules() != null) {
                intent.putStringArrayListExtra("wateringSchedules", new ArrayList<>(plant.getWateringSchedules()));
            } else {
                intent.putStringArrayListExtra("wateringSchedules", new ArrayList<>()); // Pass an empty list if null
            }

            context.startActivity(intent);
        });

    }



    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView plantImage;
        TextView plantName;
        CheckBox checkBox;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantImage = itemView.findViewById(R.id.plantImage);
            plantName = itemView.findViewById(R.id.plantName);
            checkBox = itemView.findViewById(R.id.plantCheckBox);
        }
    }
}



