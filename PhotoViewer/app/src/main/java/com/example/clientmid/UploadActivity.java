package com.example.clientmid;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int PERMISSION_REQUEST = 10;

    private Uri selectedImageUri;
    private ImageView imagePreview;
    private EditText titleInput, contentInput;

    private String uploadUrl = "http://10.0.2.2:8000/api/posts/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        imagePreview = findViewById(R.id.imagePreview);
        titleInput = findViewById(R.id.titleInput);
        contentInput = findViewById(R.id.contentInput);

        checkPermission();
    }

    private void checkPermission() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission required to upload image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onClickSelectImage(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE &&
                resultCode == Activity.RESULT_OK &&
                data != null) {

            selectedImageUri = data.getData();
            imagePreview.setImageURI(selectedImageUri);
        }
    }

    public void onClickUpload(View v) {

        if (titleInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        new UploadTask().execute();
    }

    private class UploadTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] imageBytes = baos.toByteArray();

                String boundary = "----AndroidClient" + System.currentTimeMillis();
                URL url = new URL(uploadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data; boundary=" + boundary);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                String title = titleInput.getText().toString();
                String content = contentInput.getText().toString();

                writeFormField(dos, boundary, "title", title);
                writeFormField(dos, boundary, "content", content);
                writeFileField(dos, boundary, "image", "upload.jpg", imageBytes);

                dos.writeBytes("--" + boundary + "--\r\n");
                dos.flush();
                dos.close();

                int responseCode = conn.getResponseCode();
                return responseCode == 201 || responseCode == 200;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private void writeFormField(DataOutputStream dos, String boundary,
                                    String name, String value) throws Exception {
            dos.writeBytes("--" + boundary + "\r\n");
            dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
            dos.writeBytes(value + "\r\n");
        }

        private void writeFileField(DataOutputStream dos, String boundary,
                                    String name, String filename, byte[] bytes) throws Exception {

            dos.writeBytes("--" + boundary + "\r\n");
            dos.writeBytes("Content-Disposition: form-data; name=\"" + name
                    + "\"; filename=\"" + filename + "\"\r\n");
            dos.writeBytes("Content-Type: image/jpeg\r\n\r\n");
            dos.write(bytes);
            dos.writeBytes("\r\n");
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(UploadActivity.this,
                        "Upload successful", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(UploadActivity.this,
                        "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
