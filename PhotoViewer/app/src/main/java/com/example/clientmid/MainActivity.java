package com.example.clientmid;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList = new ArrayList<>();

    private String apiUrl = "http://10.0.2.2:8000/api/posts/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostAdapter(postList);
        recyclerView.setAdapter(adapter);

        loadPosts();
    }

    // -----------------------
    // DOWNLOAD 버튼 클릭
    // -----------------------
    public void onClickDownload(View v) {
        loadPosts();
    }

    // -----------------------
    // UPLOAD 버튼 클릭
    // -----------------------
    public void onClickUpload(View v) {
        Intent intent = new Intent(this, UploadActivity.class);
        startActivity(intent);
    }

    // -----------------------
    // POSTS 불러오기
    // -----------------------
    private void loadPosts() {
        new AsyncTask<Void, Void, List<Post>>() {
            @Override
            protected List<Post> doInBackground(Void... voids) {
                List<Post> tempList = new ArrayList<>();
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.connect();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder json = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) json.append(line);
                    reader.close();

                    JSONArray jsonArray = new JSONArray(json.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Post post = new Post(
                                obj.getString("title"),
                                obj.getString("content"),
                                obj.optString("image", null)
                        );
                        tempList.add(post);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return tempList;
            }

            @Override
            protected void onPostExecute(List<Post> result) {
                postList.clear();
                postList.addAll(result);
                adapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Posts Loaded", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPosts(); // refresh when coming back
    }
}
