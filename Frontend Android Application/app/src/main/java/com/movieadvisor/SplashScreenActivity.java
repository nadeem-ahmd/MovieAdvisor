package com.movieadvisor;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.movieadvisor.Authentication.AuthenticationActivity;
import com.movieadvisor.Main.MainActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash_screen);

        new StatusCheck().execute();
    }

    private class StatusCheck extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                URL url = new URL(Constants.MOVIE_ADVISOR_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                return connection.getResponseCode() == 200;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean online) {
            if (online)
                if (FirebaseAuth.getInstance().getCurrentUser() == null)
                    startActivity(new Intent(getApplicationContext(), AuthenticationActivity.class));
                else
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
            else
                Toast.makeText(getApplicationContext(), "Unable to connect, try again later.", Toast.LENGTH_LONG).show();

            finish();
        }
    }


}
