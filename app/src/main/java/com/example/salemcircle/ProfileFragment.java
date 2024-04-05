package com.example.salemcircle;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;


import models.UserModel;
import network.ApiService;
import network.RetrofitClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SecurityUtils;
import android.Manifest;
import utils.FileUtils;
import java.io.File;


public class ProfileFragment extends Fragment {


    private TextView usernameTextView, emailTextView, fullNameTextView;
    private ShapeableImageView profileImageView;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    public ProfileFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                profileImageView.setImageURI(uri); // Set the selected image on your ImageView.
                uploadImage(uri); // Now, upload the selected image to your server.
            }
        });


        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            boolean allGranted = true;
            for (Boolean granted : permissions.values()) {
                allGranted &= granted;
            }
            if (allGranted) {
                pickImageFromGallery();
            } else {
                showPermissionDeniedExplanation();
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(SecurityUtils.isLoggedIn(getContext()) ? R.layout.fragment_profile : R.layout.fragment_login_signup, container, false);

        // Initialize TextViews
        usernameTextView = view.findViewById(R.id.usernameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        fullNameTextView = view.findViewById(R.id.fullName);
        profileImageView = view.findViewById(R.id.profileImageView); // Initialize the ImageView


        if (SecurityUtils.isLoggedIn(getContext())) {
            String userId = SecurityUtils.getUserId(getContext());
            displayUserProfile(view, userId);

            ImageButton changeProfileImageButton = view.findViewById(R.id.changeProfileImageButton);
            changeProfileImageButton.setOnClickListener(v -> requestImagePermissionAndPickImage());
        } else {
            setupLoginOption(view);
        }
        return view;
    }
    private void requestImagePermissionAndPickImage() {
        // Ensure the fragment is attached to an activity and thus getContext() is not null
        if (isAdded() && getContext() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                    showPermissionDeniedExplanation();
                } else {
                    requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES});
                }
            } else {
                // For Android versions below TIRAMISU, use READ_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showPermissionDeniedExplanation();
                } else {
                    requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
                }
            }
        }
    }

    private void pickImageFromGallery() {
        pickImageLauncher.launch("image/*");
    }
    private void showPermissionDeniedExplanation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Permission Denied")
                .setMessage("This feature requires access to your media files. Please enable the storage permission in the app settings to use this feature.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void uploadImage(Uri imageUri) {
        File file = FileUtils.getFile(getContext(), imageUri);
        if (file != null) {
            RequestBody requestFile = RequestBody.create(MediaType.parse(getContext().getContentResolver().getType(imageUri)), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("profilePicture", file.getName(), requestFile);

            ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
            apiService.uploadProfilePicture(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show();
                        // Assuming you have a method to refresh the profile info
                        refreshFragment();
                    } else {
                        if(isAdded()){
                        Toast.makeText(getContext(), "Failed to upload picture", Toast.LENGTH_SHORT).show();
                    }}
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void displayUserProfile(View view, String userId) {
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);


        apiService.getUserById(userId).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserModel user = response.body();
                    // Update UI with fetched user details
                    usernameTextView.setText(user.getUsername());
                    emailTextView.setText(user.getEmail());
                    fullNameTextView.setText(user.getName());

                    Glide.with(getContext())
                            .load(user.getProfileImagePath()) // URL from the user model
                            .placeholder(R.drawable.default_profile_pic) // Show default image while loading
                            .into(profileImageView);
                } else {
                    if (isAdded()) {
                    Toast.makeText(getContext(), "Failed to fetch user details", Toast.LENGTH_SHORT).show();
                }
                }
            }


            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                if (isAdded()) {
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }}
        });




        Button logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {


            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            // Clear the access token
            SecurityUtils.clearAccessToken(getContext());


            // Refresh this fragment to show the login/signup option after logout
            refreshFragment();
        });
    }


    private void updateUserInfo(String username, String email, String password, String fullName, String role, String profileImagePath) {
        // Assuming you have a UserModel object with the current user's information
        UserModel updatedUser = new UserModel(username, email, password, fullName ,role ,profileImagePath);
        updatedUser.setName(fullName);
        // Set other fields as necessary, e.g., username, email, etc.


        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        Call<UserModel> call = apiService.updateUserInfo(updatedUser);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful()) {
                    // Update was successful, refresh the profile information displayed
                    Toast.makeText(getContext(), "Name updated successfully", Toast.LENGTH_SHORT).show();
                    refreshFragment();
                } else {
                    // Handle the case where the server response indicates an error
                    Toast.makeText(getContext(), "Failed to update name", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                // Handle network error or other issues with the call to the server
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void setupLoginOption(View view) {
        Button loginButton = view.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });


        Button signupButton = view.findViewById(R.id.signupButton); // Add this line
        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SignUpActivity.class); // Make sure you have SignUpActivity
            startActivity(intent);
        });
    }


    private void refreshFragment() {
        // Check if the fragment is attached to a context to avoid IllegalStateException
        if (isAdded()) {
            // Replace this fragment with a new instance of itself
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .commit();
        }
    }
}




