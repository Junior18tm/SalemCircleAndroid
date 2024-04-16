package utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SecurityUtils {

    private static final String ENCRYPTED_PREFS_FILE_NAME = "encrypted_prefs";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String USER_ROLE_KEY = "user_role";
    private static final String USER_ID_KEY = "user_id" ;

    public static void storeAccessToken(Context context, String token) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREFS_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            sharedPreferences.edit().putString("access_token", token).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("SecurityUtils", "Storing access token: " + token);
    }


    public static String getAccessToken(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREFS_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String token = sharedPreferences.getString(ACCESS_TOKEN_KEY, null); // Retrieve token into variable
            Log.d("SecurityUtils", "Retrieved access token: " + token); // Correctly log the token
            return token; // Return the retrieved token
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
    public static void clearAccessToken(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "encrypted_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // Clear the access token from SharedPreferences
            sharedPreferences.edit().remove(ACCESS_TOKEN_KEY).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void clearUserId(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREFS_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // Clear the user ID from SharedPreferences
            sharedPreferences.edit().remove(USER_ID_KEY).apply();
            Log.d("SecurityUtils", "Cleared user ID");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isLoggedIn(Context context) {
        return getAccessToken(context) != null; // User is logged in if a token exists
    }

    // Method to store user ID
    public static void storeUserId(Context context, String userId) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREFS_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            sharedPreferences.edit().putString(USER_ID_KEY, userId).apply();
            Log.d("SecurityUtils", "Stored user ID: " + userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to retrieve user ID
    public static String getUserId(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREFS_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String userId = sharedPreferences.getString(USER_ID_KEY, null);
            Log.d("SecurityUtils", "RetrieveD user ID: " + userId);
            return userId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
