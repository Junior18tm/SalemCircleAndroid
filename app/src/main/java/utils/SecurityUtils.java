package utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SecurityUtils {

    private static final String ENCRYPTED_PREFS_FILE_NAME = "encrypted_prefs";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String USER_ROLE_KEY = "user_role";

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

            return sharedPreferences.getString("access_token", null);
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
            sharedPreferences.edit().remove("access_token").apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean isLoggedIn(Context context) {
        return getAccessToken(context) != null; // User is logged in if a token exists
    }

    public static void storeUserRole(Context context, String role) {
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

            sharedPreferences.edit().putString(USER_ROLE_KEY, role).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // check for user's role (admin or user)
    public static String getUserRole(Context context) {
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

            return sharedPreferences.getString(USER_ROLE_KEY, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isAdmin(Context context) {
        return "admin".equals(getUserRole(context)); // Check if the user role is admin
    }

}