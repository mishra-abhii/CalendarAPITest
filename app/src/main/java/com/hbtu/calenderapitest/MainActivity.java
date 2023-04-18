package com.hbtu.calenderapitest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "523813956625-41374d211ku3v1c2dcdmlr3cplph2j5a.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "AIzaSyA4Q-mMz-vu0eCBJYCl2kbTrR42lDDwfOk";

    Button btnHasEvents;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        btnHasEvents = findViewById(R.id.btnHasEvents);
        btnHasEvents.setOnClickListener(v -> {
            fetchCalendarEvents(account);
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchCalendarEvents(GoogleSignInAccount account) {
        new AsyncTask<Void, Void, List<Event>>() {
            @Override
            protected List<Event> doInBackground(Void... params) {
                try {
                    // Build the Calendar client
                    HttpTransport transport = new NetHttpTransport();
                    JsonFactory jsonFactory = new JacksonFactory();
                    GoogleCredential credential = new GoogleCredential.Builder()
                            .setTransport(transport)
                            .setJsonFactory(jsonFactory)
                            .setClientSecrets(
                                    CLIENT_ID,
                                    CLIENT_SECRET)
                            .build();
                    credential.setAccessToken(account.getIdToken());

                    Calendar service =
                            new Calendar.Builder(
                                    transport,
                                    jsonFactory,
                                    credential)
                                    .setApplicationName("CalenderAPITest")
                                    .build();

                    // Fetch the user's calendar events
                    DateTime now = new DateTime(System.currentTimeMillis());
                    DateTime twentyMinutesLater = new DateTime(System.currentTimeMillis() + (20*60*1000));
                    // Set the time range to the current time only.
                    Events events = service.events().list("primary")
                            .setTimeMin(now)
                            .setTimeMax(twentyMinutesLater)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute();
                    List<Event> items = events.getItems();
                    return items;
                }
                catch (Exception e) {
                    Log.e("tag", "Error fetching calendar events: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Event> items) {
                if (items != null) {
                    // Handle the calendar events here
                    Toast.makeText(MainActivity.this, "Yes, there is an event", Toast.LENGTH_SHORT).show();
                    Log.e("tag", "Calendar events found.");
                } else {
                    Toast.makeText(MainActivity.this, "No event found", Toast.LENGTH_SHORT).show();
                    Log.e("tag", "No calendar events found.");
                }
            }
        }.execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, GoogleSignInActivity.class);
            startActivity(intent);
        }
    }
}