package com.example.soilmate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TrashedPlantAdapter extends RecyclerView.Adapter<TrashedPlantAdapter.ViewHolder> {
    private List<PlantModel> trashedPlantList;
    private List<PlantModel> originalTrashedPlantList; // ✅ Keep original list

    private Context context;

    public TrashedPlantAdapter(Context context, List<PlantModel> trashedPlantList) {
        this.context = context;
        this.trashedPlantList = trashedPlantList;
        this.originalTrashedPlantList = new ArrayList<>(trashedPlantList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trashed_plant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlantModel plant = trashedPlantList.get(position);
        holder.plantName.setText(plant.getName());
        Glide.with(context).load(plant.getImageUrl()).into(holder.plantImage);


        // Restore Button
        holder.restoreButton.setOnClickListener(v -> showRestoreDialog(plant, position));
    }

    private void showRestoreDialog(PlantModel plant, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Retrieve Plant")
                .setMessage("Would you like to retrieve " + plant.getName() + "?")
                .setPositiveButton("Retrieve", (dialog, which) -> restorePlant(plant, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void updateList(List<PlantModel> newList) {
        trashedPlantList.clear();
        trashedPlantList.addAll(newList);
        notifyDataSetChanged();
    }





    private void restorePlant(PlantModel plant, int position) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Move plant back to the "plants" collection
        firestore.collection("plants").document(plant.getId())
                .set(plant)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreDebug", "Plant restored to plants collection: " + plant.getId());

                    // Remove plant from the "trash" collection
                    firestore.collection("trash").document(plant.getId())
                            .delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d("FirestoreDebug", "Plant removed from trash: " + plant.getId());

                                // Remove from local list and notify adapter
                                trashedPlantList.remove(position);
                                notifyItemRemoved(position);

                                // Send back to MyPlants
                                Intent intent = new Intent(context, MyPlants.class);
                                intent.putExtra("restored_plant", plant);
                                context.startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirestoreDebug", "Failed to remove plant from trash: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDebug", "Failed to restore plant to plants collection: " + e.getMessage());
                });
    }

    @Override
    public int getItemCount() {
        return trashedPlantList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView plantImage;
        TextView plantName;
        ImageButton restoreButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            plantImage = itemView.findViewById(R.id.trashedPlantImage);
            plantName = itemView.findViewById(R.id.trashedPlantName);
            restoreButton = itemView.findViewById(R.id.restorePlant);
        }
    }
}
