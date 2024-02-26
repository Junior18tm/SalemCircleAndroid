package com.example.salemcircle;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import android.widget.Toast;

import utils.SecurityUtils;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(SecurityUtils.isLoggedIn(getContext()) ? R.layout.fragment_profile : R.layout.fragment_login_signup, container, false);

        if (SecurityUtils.isLoggedIn(getContext())) {
            displayUserProfile(view);
        } else {
            setupLoginOption(view);
        }
        return view;
    }

    private void displayUserProfile(View view) {
        Button logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {

            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            // Clear the access token
            SecurityUtils.clearAccessToken(getContext());

            // Refresh this fragment to show the login/signup option after logout
            refreshFragment();
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



