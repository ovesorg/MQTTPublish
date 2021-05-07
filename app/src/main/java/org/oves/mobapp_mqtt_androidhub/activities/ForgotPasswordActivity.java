package org.oves.mobapp_mqtt_androidhub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.oves.mobapp_mqtt_androidhub.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private String email;
    private TextInputLayout email_address;
    private TextView backToLogin;
    private Button resetPassword;
    private boolean isEmailOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
    }

    private void initViews() {
        //Views
        email_address = findViewById(R.id.email_address);
        resetPassword = findViewById(R.id.resetPassword);
        backToLogin = findViewById(R.id.backToLogin);

        //Set click listeners
        resetPassword.setOnClickListener(this);
        backToLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        //Switch case
        switch (view.getId()) {
            case R.id.backToLogin:
                transitionToLoginActivity();
                break;
            case R.id.resetPassword:
                getEmailAddress();
                checkEmail();

                //Check if Email is OK
                if (!isEmailOk) return;
                transitionToLoginActivity();
                break;
        }
    }

    /**
     * Transition to LoginActivity
     */
    private void transitionToLoginActivity() {
        Intent loginActivityIntent = new Intent(getApplicationContext(), LoginActivity.class);
        loginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginActivityIntent);
        finish();
    }

    private void getEmailAddress() {
        email = email_address.getEditText().getText().toString().trim();
    }

    private boolean checkEmail() {
        if (email != null && !email.isEmpty()) {

            Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
            if (matcher.find()) {
                email_address.setErrorEnabled(false);
                email_address.setFocusable(false);
                isEmailOk = true;
                return true;
//                transitionToMain();
            } else {
                email_address.setError("Valid email required!");
                email_address.setErrorEnabled(true);
                email_address.setFocusable(true);
                isEmailOk = false;
                return false;
            }
        } else {
            email_address.setError("Email required!");
            email_address.setErrorEnabled(true);
            email_address.setFocusable(true);
            isEmailOk = false;
            return false;
        }
    }
}