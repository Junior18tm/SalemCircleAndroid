package com.example.salemcircle;

import static android.app.PendingIntent.getActivity;

import static utils.SecurityUtils.getUserId;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import models.CommentModel;
import models.CommentPostRequest;
import models.EventModel;
import models.ParticipationRequest;
import models.ParticipationResponse;
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
    private Button viewCommentsButton, joinLeaveEventButton;
    private static final String LOG_TAG = "UserRoleCheck";
    private String currentUserId;
    private boolean isAdmin, isUserParticipating = false;


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
        joinLeaveEventButton = findViewById(R.id.joinLeaveEventButton);
        viewCommentsButton = findViewById(R.id.viewCommentsButton);

        joinLeaveEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showJoinConfirmationDialog();
            }
        });

        viewCommentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mongoObjectId = getIntent().getStringExtra("EVENT_MONGO_ID");
                if (mongoObjectId != null && !mongoObjectId.isEmpty()) {
                    fetchEventDetails(mongoObjectId);
                    fetchComments(mongoObjectId);
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

                    //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                     //String formattedDateTime = sdf.format(event.getDateTime());

                    eventNameTextView.setText(event.getEventName());
                    eventDescriptionTextView.setText(event.getDescription());

                    String formattedDateTime = formatDateWithOrdinalIndicator(event.getDateTime());
                    eventDateTimeTextView.setText(formattedDateTime);

                    String capacityText = String.format(Locale.getDefault(), "Capacity: %d/%d Participants",
                            event.getParticipants().size(),
                            event.getCapacity());
                    eventCapacityTextView.setText(capacityText);

                    fabEditEvent.setVisibility(View.GONE);
                    checkUserRoleAndSetupFab(eventId);

                    //check if user is a current participant of this event
                    String currentUserId = SecurityUtils.getUserId(EventsActivity.this);
                    boolean isUserParticipating = event.getParticipants().contains(currentUserId);
                    updateJoinLeaveButton(isUserParticipating, event);

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
    private void updateJoinLeaveButton(boolean userHasJoinedEvent, EventModel event) {
        Button joinLeaveEventButton = findViewById(R.id.joinLeaveEventButton);
        if (userHasJoinedEvent) {
            joinLeaveEventButton.setText("Leave Event");
            joinLeaveEventButton.setOnClickListener(v -> showLeaveConfirmationDialog());
        } else if (event.getParticipants().size() >= event.getCapacity()) {
            joinLeaveEventButton.setEnabled(false);
            joinLeaveEventButton.setText("Event Full");
        } else {
            joinLeaveEventButton.setText("Join Event");
            joinLeaveEventButton.setOnClickListener(v -> showJoinConfirmationDialog());
        }
    }
    private void showLeaveConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Event")
                .setMessage("Want to leave this event?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        leaveEvent(); // Call your method to join the event
                    }
                })
                .show();
    }

    private void leaveEvent() {
        String userId = SecurityUtils.getUserId(EventsActivity.this);
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Error: User ID not found.", Toast.LENGTH_LONG).show();
            return;
        }
        String eventId = getIntent().getStringExtra("EVENT_MONGO_ID");
        String eventId2 = getIntent().getStringExtra("EVENT_ID");

        ParticipationRequest request = new ParticipationRequest(eventId, userId);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<ParticipationResponse> call = apiService.leaveEvent(request);

        call.enqueue(new Callback<ParticipationResponse>() {
            @Override
            public void onResponse(Call<ParticipationResponse> call, Response<ParticipationResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EventsActivity.this, "Left the event successfully.", Toast.LENGTH_SHORT).show();

                    // Update UI to reflect that user has left the event
                    fetchEventDetails(eventId2);
                } else {
                    Toast.makeText(EventsActivity.this, "Failed to leave the event.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ParticipationResponse> call, Throwable t) {
                Toast.makeText(EventsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showJoinConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Join Event")
                .setMessage("Want to join this event?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        joinEvent(); // Call your method to join the event
                    }
                })
                .show();
    }

    private void joinEvent() {
        String eventId = getIntent().getStringExtra("EVENT_MONGO_ID");
        String eventId2 = getIntent().getStringExtra("EVENT_ID");
        String userId = SecurityUtils.getUserId(this);

        if (eventId == null || userId == null) {
            Toast.makeText(this, "Error: Missing event ID or user ID.", Toast.LENGTH_LONG).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        ParticipationRequest request = new ParticipationRequest(eventId, userId);
        Call<ParticipationResponse> call = apiService.participateInEvent(request);

        call.enqueue(new Callback<ParticipationResponse>() {
            @Override
            public void onResponse(Call<ParticipationResponse> call, Response<ParticipationResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EventsActivity.this, "Joined the event successfully!", Toast.LENGTH_SHORT).show();
                    fetchEventDetails(eventId2);
                } else {
                    Toast.makeText(EventsActivity.this, "Failed to join the event", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ParticipationResponse> call, Throwable t) {
                Toast.makeText(EventsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

        currentUserId = SecurityUtils.getUserId(EventsActivity.this);
        CommentsAdapter adapter = new CommentsAdapter(this, comments, currentUserId,isAdmin);
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

                    currentUserId = SecurityUtils.getUserId(EventsActivity.this);
                    isAdmin = "admin".equals(response.body().getRole());
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

    private String formatDateWithOrdinalIndicator(Date date) {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault()); // Month as a string
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault()); // Year

        int day = Integer.parseInt(new SimpleDateFormat("d", Locale.getDefault()).format(date)); // Day of the month as an integer
        String daySuffix = getDaySuffix(day);


        return String.format(Locale.getDefault(), "%s %d%s, %s",
                monthFormat.format(date),
                day,
                daySuffix,
                yearFormat.format(date));
    }


    private String getDaySuffix(final int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

}
