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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.movieadvisor.Constants;
import com.movieadvisor.Main.MainActivity;
import com.movieadvisor.R;

public class RegisterFragment extends Fragment {

    private FirebaseAuth mAuth;

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private ProgressBar loadingProgressBar;
    private ConstraintLayout contentConstraintLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();

        loadingProgressBar = view.findViewById(R.id.loading_progress_bar);
        contentConstraintLayout = view.findViewById(R.id.content);

        nameEditText = view.findViewById(R.id.name_et);
        emailEditText = view.findViewById(R.id.email_et);
        passwordEditText = view.findViewById(R.id.password_et);
        confirmPasswordEditText = view.findViewById(R.id.confirm_password_et);

        confirmPasswordEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    register();
                    return true;
                }
                return false;
            }
        });

        Button registerButton = view.findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        return view;
    }

    private void register() {
        InputMethodManager service = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        service.hideSoftInputFromWindow(getView().getWindowToken(), 0);

        if (nameEditText.getText().toString().equals("") || nameEditText.getText().toString().length() < 3) {
            Constants.toast(getContext(), "Enter a name of at least 3 characters.");
        } else if (emailEditText.getText().toString().equals("")) {
            Constants.toast(getContext(), "Enter a valid email.");
        } else if (passwordEditText.getText().toString().equals("")) {
            Constants.toast(getContext(), "Enter a valid password.");
        } else if (confirmPasswordEditText.getText().toString().equals("")) {
            Constants.toast(getContext(), "Confirm your password.");
        } else if (!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())) {
            Constants.toast(getContext(), "Passwords do not match.");
        } else {
            Constants.hide(contentConstraintLayout);
            Constants.show(loadingProgressBar);

            mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                UserProfileChangeRequest updateRequest = new UserProfileChangeRequest.Builder().setDisplayName(nameEditText.getText().toString()).build();
                                firebaseUser.updateProfile(updateRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                        startActivity(intent);
                                        getActivity().finish();
                                    }
                                });
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