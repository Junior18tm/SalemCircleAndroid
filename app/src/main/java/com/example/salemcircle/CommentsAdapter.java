package com.example.salemcircle;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
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
import com.example.salemcircle.R;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import models.CommentModel;
import network.ApiService;
import network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SecurityUtils;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<CommentModel> comments;
    private Context context;
    private final String currentUserId;
    private final boolean isAdmin;

    // Context is passed here
    public CommentsAdapter(Context context, List<CommentModel> comments, String currentUserId, boolean isAdmin) {
        this.context = context;
        this.comments = comments;
        this.currentUserId = currentUserId;
        this.isAdmin = isAdmin;
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

        if(comment.getUser().getId ().equals(currentUserId) || isAdmin) {
            holder.deleteCommentButton.setVisibility(View.VISIBLE);

            holder.deleteCommentButton.setOnClickListener(v -> {
                // Implement the API call to delete the comment
                deleteComment(comment.getId(), position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            originalFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Set the original format to UTC because the timestamp ends with 'Z'

            SimpleDateFormat targetFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH);
            targetFormat.setTimeZone(TimeZone.getTimeZone("America/New_York")); // Set target format to EST

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

    private void deleteComment(String commentId, int position) {
        ApiService apiService = RetrofitClient.getClient(context).create(ApiService.class);
        String authToken = SecurityUtils.getAccessToken(context); // Retrieve the stored access token
        if (authToken == null) {
            Toast.makeText(context, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }
        String authHeader = "Bearer " + authToken; // Prepare the Authorization header
        Log.d("AuthHeader", "Bearer " + authToken);

        // Assuming 'apiService' is an instance of ApiService and properly initialized
        Call<Void> call = apiService.deleteComment(commentId);
        call.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Successfully deleted the comment from the backend
                    comments.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, comments.size());
                    Toast.makeText(context, "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle failure
                    Log.d("API_CALL", "Failed: " + response.code() + " " + response.message());
                    Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Network error or exception thrown
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }




    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, commentTextView, commentTimestamp;
        ImageView commentUserProfileImage; // Added for profile image
        ImageView deleteCommentButton;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            commentTimestamp = itemView.findViewById(R.id.commentTimestamp);
            commentUserProfileImage = itemView.findViewById(R.id.commentUserProfileImage);
            deleteCommentButton = itemView.findViewById(R.id.deleteCommentButton);
        }
    }
}

