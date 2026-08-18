package com.example.time4meds;

// ElderlyAdapter: RecyclerView adapter that binds Elderly profile data to item views
// handles profile selection, editing, and deletion
// and navigates to corresponding activities

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ElderlyAdapter extends RecyclerView.Adapter<ElderlyAdapter.ElderlyViewHolder> { // adapter class to connect Elderly data to RecyclerView items

    private final Context context;
    private final List<Elderly> elderlyList;

    public ElderlyAdapter(Context context, List<Elderly> elderlyList) {
        this.context = context; // store the activity/context
        this.elderlyList = elderlyList; // store the list of Elderly objects
    }

    @NonNull // indicates this value must not be null
    @Override // overrides a parent class
    // to set up the item view layout and wraps it in a ViewHolder
    public ElderlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_elderly, parent, false);
        return new ElderlyViewHolder(view);
    }

    @Override
    // fills the RecyclerView item with the correct data from the list based on its position
    public void onBindViewHolder(@NonNull ElderlyViewHolder holder, int position) {
        Elderly elderly = elderlyList.get(position);

        // display the selected profile's name and age
        holder.txtName.setText(elderly.getName());
        holder.txtAge.setText("Age: " + elderly.getAge());

        // load actual photo if available, else placeholder image
        if (elderly.getPhotoUrl() != null && !elderly.getPhotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(elderly.getPhotoUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.placeholder);
        }

        // when click the card profile, then it will go to welcomepage
        holder.itemView.setOnClickListener(v -> {
            SharedPreferences prefs = context.getSharedPreferences("SelectedProfile", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("profileId", elderly.getId());
            editor.putString("profileName", elderly.getName());
            editor.apply();

            // navigate to selected profile's welcomepage
            Intent intent = new Intent(context, ElderlyWelcomepageActivity.class);
            intent.putExtra("elderlyId", elderly.getId());
            intent.putExtra("elderlyName", elderly.getName());
            context.startActivity(intent);
        });

        // for the view/edit button
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddProfileActivity.class); // open AddProfileActivity
            intent.putExtra("elderlyId", elderly.getId());
            intent.putExtra("name", elderly.getName());
            intent.putExtra("age", elderly.getAge());
            intent.putExtra("medicalInfo", elderly.getMedicalInfo());
            intent.putExtra("gender", elderly.getGender());
            intent.putExtra("photoUrl", elderly.getPhotoUrl());

            List<String> contact = elderly.getEmergencyContact(); // get emergency contacts list
            String firstContact = (contact != null && !contact.isEmpty()) ? contact.get(0) : ""; // get first contact or empty
            intent.putExtra("emergencyContact", firstContact); // pass first contact to next activity
            context.startActivity(intent); // start AddProfileActivity
        });

        // for deleting the profile
        holder.btnDelete.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // get current user ID
            DatabaseReference dbRef = FirebaseDatabase.getInstance()
                    .getReference("elderly") // reference to "elderly" in database
                    .child(userId) // current user
                    .child(elderly.getId()); // specific elderly profile
            dbRef.removeValue() // delete the profile

                    // id succeeded
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Profile deleted!", Toast.LENGTH_SHORT).show();
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            elderlyList.remove(pos); // remove from the list
                            notifyItemRemoved(pos); // update RecyclerView
                        }
                    })

                    // if failed
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {return elderlyList.size();} // return the total number of Elderly items

    public static class ElderlyViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtAge;
        ImageView ivPhoto, medicalIcon;
        Button btnEdit, btnDelete;

        public ElderlyViewHolder(@NonNull View itemView) {
            super(itemView);
            // link all the components to the UI elements
            txtName = itemView.findViewById(R.id.tvName);
            txtAge = itemView.findViewById(R.id.tvAge);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
