package com.example.salemcircle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import utils.SecurityUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_events) {
                selectedFragment = new EventsFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
          //  } else if (itemId == R.id.nav_search) {
          //      selectedFragment = new SearchFragment();
            } else if (itemId == R.id.nav_favorites) {
                if (SecurityUtils.isLoggedIn(MainActivity.this)) {
                    selectedFragment = new FavoritesFragment();
                } else {
                    promptLoginOrRegister();
                    return true;  // Return true but do not set the fragment; this keeps the user on the current visible fragment.
                }
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }

            return true; // true to display the item as the selected item
        });

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new WelcomeFragment()).commit();
        }
        // LoginActivity, after successful login
        if (getIntent().getBooleanExtra("SHOW_PROFILE", false)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .commit();
        }
    }
    //If user is not logged in when clicking favorite fragment
    private void promptLoginOrRegister() {
        new AlertDialog.Builder(this)
                .setTitle("Access Restricted")
                .setMessage("You need to log in to add favorites.")
                .setPositiveButton("Login", (dialog, which) -> {
                    startActivity(new Intent(this, LoginActivity.class));
                })
                .setNegativeButton("Register", (dialog, which) -> {
                    startActivity(new Intent(this, SignUpActivity.class));
                })
                .show();
    }

    ActivityResultLauncher<Intent> createEventLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    boolean eventCreated = result.getData().getBooleanExtra("EVENT_CREATED_SUCCESSFULLY", false);
                    if(eventCreated) {
                        // Code to switch to EventsFragment
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new EventsFragment())
                                .commit();
                    }
                }
            });

}


