package com.IOT.fingerprint.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.IOT.fingerprint.ui.home.MainActivity;
import com.IOT.fingerprint.data.SharedPrefManager;
import com.IOT.fingerprint.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;

    @BindView(R.id.login_email)
    EditText emailET;
    @BindView(R.id.login_password)
    EditText passwordET;
    @BindView(R.id.login_button)
    Button loginButton;
    private ProgressDialog progressDialog;

    static {
        // to offline firebase database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        this.mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // you are already login
            openMainActivity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.open_sign_up_tv)
    void openSignupActivity() {
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.login_button)
    void login() {

        if (!isValidEmailAndPassword()) {
            onLoginFailed();
            return;
        }

        loginButton.setEnabled(false);
        showProgressDialog(LoginActivity.this, "login...");

        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();

        this.mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new loginTask());

    }

    private void onLoginFailed() {
        Toast.makeText(getBaseContext(), "login failed", Toast.LENGTH_LONG).show();
        hideProgressDialog();
        loginButton.setEnabled(true);
    }

    private boolean isValidEmailAndPassword() {
        boolean valid = true;

        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { // check email validation
            emailET.setError("enter a valid email address");
            valid = false;
        } else {
            emailET.setError(null);
        }

        if (password.isEmpty() || password.length() < 8 || password.length() > 15) { // check password validation
            passwordET.setError("wrong password");
            valid = false;
        } else {
            passwordET.setError(null);
        }

        return valid;
    }

    private class loginTask implements OnCompleteListener<AuthResult> {

        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {
                Log.d(TAG, "signInWithEmail:success");
                loginButton.setEnabled(true);
                hideProgressDialog();
                saveUserDataInSharedPreferences();
            } else {
                Log.w(TAG, "signInWithEmail:failure", task.getException());
                onLoginFailed();
            }
        }
    }

    public void showProgressDialog(Context context, String message) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = new ProgressDialog(context,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @OnClick(R.id.forgot_password_tv)
    void showSendEmailCustomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.activity_forgot_password);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final Button sendEmail = dialog.findViewById(R.id.send_email_button);

        final EditText emailET = dialog.findViewById(R.id.forgot_password_email_et);

        sendEmail.setOnClickListener(v -> {
            String email = emailET.getText().toString();
            sendEmail(email);

            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void sendEmail(String emailAddress) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast toast = Toast.makeText(LoginActivity.this, "the email is sent", Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
    }

    private void openMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        LoginActivity.this.startActivity(intent);
        finish();
    }

    public void saveUserDataInSharedPreferences() {
        String userId = FirebaseAuth.getInstance().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ref.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    String email = dataSnapshot.child("Email").getValue().toString();
                    String username = dataSnapshot.child("username").getValue().toString();

                    SharedPrefManager.getInstance(LoginActivity.this).userLogin(username,email);
                    openMainActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}