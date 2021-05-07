package org.oves.mobapp_mqtt_androidhub.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.oves.mobapp_mqtt_androidhub.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    //Field Variables
    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private TextInputLayout firstName;
    private TextInputLayout lastName;
    private TextInputLayout email;
    private TextInputLayout password;
    private Button signUpButton;
    private TextView loginButton;
    private String firstNameText;
    private String lastNameText;
    private String emailText;
    private String passwordText;
    private boolean isValidInputs;
    private boolean isValidFirstName;
    private boolean isValidEmail;
    private boolean isValidPass;
    private boolean isValidLastName;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initViews();
    }

    /**
     * Initialize view objects
     */
    private void initViews() {
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signUpButton = findViewById(R.id.signUpButton);
        loginButton = findViewById(R.id.loginButton);

        //Set Click Listeners
        signUpButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);

        //ProgressDialog.
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signUpButton:
                signUpUser();
                break;

            case R.id.loginButton:
                transitionToLoginActivity();
                break;
        }
    }

    /**
     * Transition
     */
    private void transitionToLoginActivity() {
        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    /**
     * Sign up user
     */
    private void signUpUser() {
        getUserData();

        validateUserData();

        if (!isValidInputs) return;

        //TODO: SignUp user with appropriate endpoint.
        Toasty.success(getApplicationContext(),
                "First Name: " + firstNameText + "\n" +
                        "Last Name: " + lastNameText + "\n" +
                        "Email: " + emailText + "\n", Toast.LENGTH_LONG).show();
        transitionToMainActivity();
    }

    /**
     * Transition to MainActivity
     */
    private void transitionToMainActivity() {
        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivityIntent);
    }

    private void getUserData() {
        firstNameText = firstName.getEditText().getText().toString().trim();
        lastNameText = lastName.getEditText().getText().toString().trim();
        emailText = email.getEditText().getText().toString().trim();
        passwordText = password.getEditText().getText().toString().trim();
    }

    /**
     * Validate Inputs
     */
    private void validateUserData() {
        initProgressDialog();
        checkFirstName();
        checkLastName();
        checkEmail();
        checkPassword();

        if (!isValidFirstName || !isValidLastName || !isValidEmail || !isValidPass) return;

        isValidInputs = true;
    }

    private void checkPassword() {
        if (passwordText != null && !passwordText.isEmpty()) {
            if (passwordText.length() < 8) {
                progressDialog.dismiss();
                password.setError("Minimum 8 character password required!");
                password.setErrorEnabled(true);
                password.setFocusable(true);
                isValidPass = false;
            } else {
                password.setErrorEnabled(false);
                password.setFocusable(false);
                isValidPass = true;
            }
        } else {
            progressDialog.dismiss();
            password.setError("Password is required!");
            password.setErrorEnabled(true);
            password.setFocusable(true);
            isValidPass = false;
        }
    }

    private void checkEmail() {
        if (emailText != null && !emailText.isEmpty()) {
            Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailText);
            if (matcher.find()) {
                email.setErrorEnabled(false);
                email.setFocusable(false);
                isValidEmail = true;
            } else {
                progressDialog.dismiss();
                email.setError("Valid email required!");
                email.setErrorEnabled(true);
                email.setFocusable(true);
                isValidEmail = false;
            }
        } else {
            progressDialog.dismiss();
            email.setError("Email is required!");
            email.setErrorEnabled(true);
            email.setFocusable(true);
            isValidEmail = false;
        }
    }

    private void checkLastName() {
        if (lastNameText != null && !lastNameText.isEmpty()) {
            if (lastNameText.length() <= 2) {
                progressDialog.dismiss();
                lastName.setErrorEnabled(true);
                lastName.setError("The name is too short");
                lastName.setFocusable(true);
                isValidLastName = false;
            } else {
                lastName.setErrorEnabled(false);
                lastName.setFocusable(false);
                isValidLastName = true;
            }
        } else {
            progressDialog.dismiss();
            lastName.setErrorEnabled(true);
            lastName.setError("Last Name is required");
            lastName.setFocusable(true);
            isValidLastName = false;
        }
    }

    private void checkFirstName() {
        if (firstNameText != null && !firstNameText.isEmpty()) {
            if (firstNameText.length() <= 2) {
                progressDialog.dismiss();
                firstName.setErrorEnabled(true);
                firstName.setError("The name is too short");
                firstName.setFocusable(true);
                isValidFirstName = false;
            } else {
                firstName.setErrorEnabled(false);
                firstName.setFocusable(false);
                isValidFirstName = true;
            }
        } else {
            progressDialog.dismiss();
            firstName.setErrorEnabled(true);
            firstName.setError("First Name is required");
            firstName.setFocusable(true);
            isValidFirstName = false;
        }
    }

    private void initProgressDialog() {
        progressDialog.setMessage("Signing up " + emailText);
        progressDialog.show();
    }
}