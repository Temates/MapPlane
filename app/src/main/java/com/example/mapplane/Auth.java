package com.example.mapplane;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Auth extends AppCompatActivity {

    ImageView google_img;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);


        google_img = findViewById(R.id.google);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.my_server_client_id))
                .requestEmail()
                .build();


        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        gsc = GoogleSignIn.getClient(this,gso);
        google_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SignIn();

            }
        });

    }
    private void SignIn() {
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent, 100);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 ){
            // Result returned from launching the Google Sign-In Intent
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
            // Google Sign-In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);

//                // Get the user's email and access token
//                String email = account.getEmail();
//                String accessToken = account.getIdToken(); // or account.getServerAuthCode() if available
//
//                // Add debug statements to check the values
//                Log.d("Auth", "Email: " + email);
//                Log.d("Auth", "Access Token: " + accessToken);
//                // Make a POST request to your Flask API to perform the login
//                performLogin(email, accessToken);

//            performLogin(user.getEmail(), idToken);
            new MainActivity();
            }catch (ApiException e){
                Toast.makeText(this, "Error",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void performLogin(String email, String accessToken) {
        try {
            // Construct the request URL
            String urlString = "http://192.168.4.199:8000/login";
//            String urlString = "http://192.168.1.6:8000/login";
            URL url = new URL(urlString);

            // Create the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken); // Add the Firebase ID token to the Authorization header
            connection.setDoOutput(true);

            // Construct the request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("email", email);
            requestBody.put("idToken", accessToken);
            String requestBodyString = requestBody.toString();

            // Execute the network operation on a background thread
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    // Write the request body to the connection
                    OutputStream outputStream = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    writer.write(requestBodyString);
                    writer.flush();
                    writer.close();
                    outputStream.close();

                    // Get the response from the connection
                    int responseCode = connection.getResponseCode();
                    InputStream inputStream;
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        inputStream = connection.getInputStream();
                    } else {
                        inputStream = connection.getErrorStream();
                    }

                    // Read the response from the input stream
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    reader.close();
                    String responseString = responseBuilder.toString();
                    // Store the session cookies

                    // Save the session cookie to SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("user_id", responseString); // Store the JSON string in SharedPreferences
                    editor.apply();

                    // Add a debug statement to check the response
                    Log.d("Auth", "Response: " + responseString);
                        runOnUiThread(() -> {
                            // Update UI or perform actions on the main thread
                            new MainActivity();
                        });


                    // Close the connection
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        // Show a toast or perform actions on the main thread
//                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
//            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign-in success, update UI with the signed-in user's information
                            Log.d("Auth", "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                // Get the access token
                                user.getIdToken(false)
                                        .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                                if (task.isSuccessful()) {
                                                    // Access token retrieved successfully
                                                    String accessToken = task.getResult().getToken();
                                                    Log.d("Auth", accessToken);
                                                    // Use the access token for authentication or API calls
                                                    // You can pass the access token to your server for authentication

                                                    // Example: Call your Flask API passing the access token
                                                    performLogin(user.getEmail(), accessToken);
                                                } else {
                                                    // Failed to retrieve access token
                                                    // Handle the error
                                                }
                                            }
                                        });
                            }

//                            String idToken = user.getIdToken(false).getResult().getToken();
//                            // Perform necessary actions with the idToken and accessToken
//                            performLogin(user.getEmail(), idToken);
                            MainActivity();
                        } else {
                            // Sign-in failed, display a message to the user
                            Log.w("Auth", "signInWithCredential:failure", task.getException());
                            Toast.makeText(Auth.this, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if the user is already signed in (e.g., if they have an active session)
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, proceed to the main activity
            MainActivity();
        }
    }

    private void MainActivity() {
        finish();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }
}