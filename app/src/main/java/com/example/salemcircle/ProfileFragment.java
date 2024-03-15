package com.example.salemcircle;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;

import android.widget.TextView;
import android.widget.Toast;

import models.UserModel;
import network.ApiService;
import network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SecurityUtils;

public class ProfileFragment extends Fragment {

    private TextView usernameTextView, emailTextView, fullNameTextView;


    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(SecurityUtils.isLoggedIn(getContext()) ? R.layout.fragment_profile : R.layout.fragment_login_signup, container, false);

        // Initialize TextViews
        usernameTextView = view.findViewById(R.id.usernameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        fullNameTextView = view.findViewById(R.id.fullName);

        if (SecurityUtils.isLoggedIn(getContext())) {
            String userId = SecurityUtils.getUserId(getContext());
            displayUserProfile(view, userId);
        } else {
            setupLoginOption(view);
        }
        return view;
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
                } else {
                    Toast.makeText(getContext(), "Failed to fetch user details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
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



