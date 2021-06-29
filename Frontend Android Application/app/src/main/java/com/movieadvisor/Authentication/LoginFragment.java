package com.movieadvisor.Authentication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.movieadvisor.Constants;
import com.movieadvisor.Main.MainActivity;
import com.movieadvisor.R;

public class LoginFragment extends Fragment {

    private EditText emailEditText;
    private EditText passwordEditText;
    private ProgressBar loadingProgressBar;
    private ConstraintLayout contentConstraintLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        loadingProgressBar = view.findViewById(R.id.loading_progress_bar);
        contentConstraintLayout = view.findViewById(R.id.content);

        emailEditText = view.findViewById(R.id.email_et);
        passwordEditText = view.findViewById(R.id.password_et);

        passwordEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login();
                    return true;
                }
                return false;
            }
        });

        Button loginButton = view.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        return view;
    }

    private void login() {
        InputMethodManager service = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        service.hideSoftInputFromWindow(getView().getWindowToken(), 0);

        if (emailEditText.getText().toString().isEmpty()) {
            Constants.toast(getContext(), "Please enter a valid e-mail address.");
        } else if (passwordEditText.getText().toString().isEmpty()) {
            Constants.toast(getContext(), "Please enter a valid password.");
        } else {
            Constants.hide(contentConstraintLayout);
            Constants.show(loadingProgressBar);

            FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            } else {
                                Constants.toast(getContext(), task.getException().getMessage());
                                Constants.hide(loadingProgressBar);
                                Constants.show(contentConstraintLayout);
                            }
                        }
                    });
        }
    }

}