package com.example.salemcircle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.salemcircle.R;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import models.CommentModel;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<CommentModel> comments;
    private Context context;

    // Context is passed here
    public CommentsAdapter(Context context, List<CommentModel> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentModel comment = comments.get(position);
        holder.usernameTextView.setText(comment.getUser().getUsername());
        holder.commentTextView.setText(comment.getText());
        holder.commentTimestamp.setText(formatTimestamp(comment.getCreatedAt()));

        // Using Glide to load the user's profile picture
        Glide.with(context)
                .load(comment.getUser().getProfileImagePath())
                .circleCrop()
                .placeholder(R.drawable.default_profile_pic) // Placeholder image
                .into(holder.commentUserProfileImage);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            SimpleDateFormat targetFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH);
            Date date = originalFormat.parse(timestamp);
            return targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void addComment(CommentModel newComment) {
        this.comments.add(0, newComment); // Add to the top of the list
        notifyItemInserted(0); // Notify adapter of item inserted at the top
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, commentTextView, commentTimestamp;
        ImageView commentUserProfileImage; // Added for profile image

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            commentTimestamp = itemView.findViewById(R.id.commentTimestamp);
            commentUserProfileImage = itemView.findViewById(R.id.commentUserProfileImage); // Initializing the ImageView
        }
    }
}

