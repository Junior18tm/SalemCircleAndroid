package utils;

import com.example.salemcircle.R;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import android.content.Context;

public class CloudStorageHelper {

    private Storage storage;

    public CloudStorageHelper(Context context) {
        try {
            String credentialsJson = context.getString(R.string.GCS_Credentials);
            initializeStorage(credentialsJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeStorage(String credentialsJson) throws Exception {
        InputStream credentialsStream = new ByteArrayInputStream(credentialsJson.getBytes());
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
        storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    public void uploadFileToGCS(String bucketName, String objectName, byte[] content) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName).build();
        storage.create(blobInfo, content);
        System.out.println("File uploaded to bucket " + bucketName + " as " + objectName);
    }
}
