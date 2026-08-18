package com.example.time4meds;

// TutorialAdapter: Adapter to display tutorial steps with images and tips

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.github.chrisbanes.photoview.PhotoView;
import java.util.List;

public class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.ViewHolder> {

    private List<TutorialModel> list;

    public TutorialAdapter(List<TutorialModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tutorial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TutorialModel model = list.get(position);

        holder.tvTitle.setText(model.getTitle());
        holder.tvInstruction.setText(model.getInstruction());
        holder.tvTip.setText(model.getTip());
        holder.ivScreenshot.setImageResource(model.getImageResId());

        // click listener to open the zoom popup (images)
        // opens a fullscreen popup with zoom when clicked
        holder.ivScreenshot.setOnClickListener(v -> {
            showImagePopup(v.getContext(), model.getImageResId());
        });
    }

    // this helper method handles the popup logic
    private void showImagePopup(Context context, int imageRes) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_image_zoom);

        PhotoView photoView = dialog.findViewById(R.id.photoView);
        ImageButton btnClose = dialog.findViewById(R.id.btnClose);

        photoView.setImageResource(imageRes); // sets the tutorial screenshot in the zoomable view
        btnClose.setOnClickListener(v -> dialog.dismiss()); // closes the dialog when the user clicks the close button
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInstruction, tvTip;
        ImageView ivScreenshot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvStepTitle);
            ivScreenshot = itemView.findViewById(R.id.ivScreenshot);
            tvInstruction = itemView.findViewById(R.id.tvInstruction);
            tvTip = itemView.findViewById(R.id.tvTip);
        }
    }
}