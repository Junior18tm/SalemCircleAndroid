package com.example.salemcircle;

import static android.app.PendingIntent.getActivity;

import static utils.SecurityUtils.getUserId;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import models.CommentModel;
import models.CommentPostRequest;
import models.EventModel;
import models.UserRoleResponse;
import network.ApiService;
import network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SecurityUtils;

public class EventsActivity extends AppCompatActivity {

    private ProgressBar loadingProgressBar;
    private TextView eventNameTextView, eventDescriptionTextView, eventDateTimeTextView, eventCapacityTextView, noCommentsTextView;
    private FloatingActionButton fabEditEvent;
    private Button viewCommentsButton;
    private final ActivityResultLauncher<Intent> editEventLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        if (data.hasExtra("EVENT_DELETED")) {
                            // Navigate back to EventsFragment
                            finish();
                        } else if (data.hasExtra("EVENT_ID")) {
                            // Event was updated; fetch updated details
                            String eventId = data.getStringExtra("EVENT_ID");
                            fetchEventDetails(eventId);
                        }
                    }
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        loadingProgressBar = findViewById(R.id.loadingProgressBar); // Make sure to add this in your XML layout
        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        eventDateTimeTextView = findViewById(R.id.eventDateTimeTextView);
        eventCapacityTextView = findViewById(R.id.eventCapacityTextView);
        fabEditEvent = findViewById(R.id.fab_edit_event);
        viewCommentsButton = findViewById(R.id.viewCommentsButton);
        viewCommentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Assuming you have the event ID available here
                String mongoObjectId = getIntent().getStringExtra("EVENT_MONGO_ID");
                if (mongoObjectId != null && !mongoObjectId.isEmpty()) {
                    fetchEventDetails(mongoObjectId);
                    fetchComments(mongoObjectId); // Assuming you have a method to fetch comments
                } else {
                    Toast.makeText(EventsActivity.this, "Event ID from mongo is missing", Toast.LENGTH_LONG).show();
                    finish(); // Exit if no MongoDB ObjectID is provided
                }
            }
        });

        // Retrieve the event ID passed from EventsAdapter
        String eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId != null && !eventId.isEmpty()) {
            fetchEventDetails(eventId);
        } else {
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_LONG).show();
            finish(); // Exit if no event ID is provided
        }
       // checkUserRoleAndSetupFab(eventId);

    }


    private void fetchEventDetails(String eventId) {
        showLoading(true);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<EventModel> call = apiService.getEventDetails(eventId);

        call.enqueue(new Callback<EventModel>() {
            @Override
            public void onResponse(Call<EventModel> call, Response<EventModel> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    EventModel event = response.body();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    String formattedDateTime = sdf.format(event.getDateTime());

                    eventNameTextView.setText(event.getEventName());
                    eventDescriptionTextView.setText(event.getDescription());
                    eventDateTimeTextView.setText(formattedDateTime); // Use formatted date-time string
                    eventCapacityTextView.setText(String.valueOf(event.getCapacity()));

                    fabEditEvent.setVisibility(View.GONE);
                    checkUserRoleAndSetupFab(eventId);
                } else {
                    //Toast.makeText(EventsActivity.this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EventModel> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EventsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchComments(String mongoObjectId) {
        String eventId = getIntent().getStringExtra("EVENT_ID");
        showLoading(true);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<List<CommentModel>> call = apiService.getCommentsByEventId(mongoObjectId);

        call.enqueue(new Callback<List<CommentModel>>() {

            @Override
            public void onResponse(Call<List<CommentModel>> call, Response<List<CommentModel>> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    List<CommentModel> comments = response.body();
                    // Proceed to display comments
                    showCommentsDialog(comments,mongoObjectId );

                } else {
                    Toast.makeText(EventsActivity.this, "Failed to fetch comments", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CommentModel>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EventsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showCommentsDialog(List<CommentModel> comments, String eventId) {
        Dialog commentsDialog = new Dialog(this, R.style.CustomDialogTheme);
        commentsDialog.setContentView(R.layout.comments_dialog);

        RecyclerView recyclerView = commentsDialog.findViewById(R.id.commentsRecyclerView);
        EditText commentInput = commentsDialog.findViewById(R.id.commentInputEditText);
        Button submitCommentButton = commentsDialog.findViewById(R.id.submitCommentButton);

        CommentsAdapter adapter = new CommentsAdapter(this, comments);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        submitCommentButton.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                String userId = SecurityUtils.getUserId(this); // Retrieve user ID using SecurityUtils
                if (userId != null) {
                    CommentPostRequest commentPostRequest = new CommentPostRequest(eventId, commentText, userId);
                    postComment(commentPostRequest, commentsDialog, commentInput, adapter); // Pass the dialog, input field, and adapter for refreshing
                } else {
                    Toast.makeText(this, "Failed to retrieve user ID", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        commentsDialog.show();
    }

    private void postComment(CommentPostRequest commentPostRequest, Dialog dialog, EditText commentInput, CommentsAdapter adapter) {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<CommentModel> call = apiService.postComment(commentPostRequest);

        call.enqueue(new Callback<CommentModel>() {
            @Override
            public void onResponse(Call<CommentModel> call, Response<CommentModel> response) {
                if (response.isSuccessful()) {
                    CommentModel newComment = response.body();
                    adapter.addComment(newComment);
                    commentInput.setText(""); // Clear input field

                    fetchComments(commentPostRequest.getEventId());
                } else {
                    Toast.makeText(EventsActivity.this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommentModel> call, Throwable t) {
                dialog.dismiss();
                fetchComments(commentPostRequest.getEventId());
                Toast.makeText(EventsActivity.this, "Comment Posted", Toast.LENGTH_SHORT).show();
            }
        });
    }






    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    private void checkUserRoleAndSetupFab(String eventId) {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getUserRole().enqueue(new Callback<UserRoleResponse>() {
            @Override
            public void onResponse(Call<UserRoleResponse> call, Response<UserRoleResponse> response) {
                if (response.isSuccessful() && response.body() != null && "admin".equals(response.body().getRole())) {
                    fabEditEvent.setVisibility(View.VISIBLE);

                    fabEditEvent.setOnClickListener(v -> {
                        Intent intent = new Intent(EventsActivity.this, EditEventActivity.class);
                        intent.putExtra("EVENT_ID", eventId);
                        editEventLauncher.launch(intent);
                    });
                }
            }

            @Override
            public void onFailure(Call<UserRoleResponse> call, Throwable t) {
                // Optionally handle failure
            }
        });
    }
}
