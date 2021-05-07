package org.oves.mobapp_mqtt_androidhub.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.apollographql.apollo.ApolloClient;
import com.auth0.android.jwt.JWT;
import com.google.android.material.textfield.TextInputLayout;

import org.oves.mobapp_mqtt_androidhub.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

//import com.example.SignInUserMutation;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private String userName;
    private String userPassword;
    private TextInputLayout email;
    private TextInputLayout passwordText;
    private TextView forgotPass;
    private Button loginButton;
    private TextView signUpTextView;
    private Boolean isValidEmail = false;
    private Boolean isValidPass = false;
    private Intent mainActivityIntent;
    private Intent forgotPasswordIntent;
    private ProgressDialog progressDialog;
    private ApolloClient apolloClient;
    private String accessToken;
    private JWT jwt;
    private String role;
    private String emailAddress;
    private String roleId;
    private String iat;
    private String exp;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initializations
        initViews();

        progressDialog = new ProgressDialog(this);
    }

    /**
     * Setup Clients
     */
//    private void setupClients() {
//
//        SignInUserMutation signInUserMutation = SignInUserMutation.builder()
//                .email(userName)
//                .password(userPassword)
//                .build();
//
//        apolloClient = ApolloClient.builder()
//                .serverUrl(AppConstant.BASE_URL)
//                .build();
//
//        apolloClient.mutate(signInUserMutation).enqueue(new ApolloCall.Callback<SignInUserMutation.Data>() {
//            @Override
//            public void onResponse(@NotNull Response<SignInUserMutation.Data> response) {
//                Log.e("onResponse: ", response.toString());
//                if (response.getData() != null) {
//                    //Decode data..
//                    accessToken = response.getData().signInLoginUser().accessToken().trim();
//                    jwt = new JWT(accessToken);
//                    computeData(accessToken);
//                    //Check if User is an Admin
//                    if (role.equals("ADMIN")) {
//                        message = "Only Distributors and Agents allowed to use the app ";
//                    } else {
////                        message = "onResponse: " + accessToken;
//                        //Login User...
//                        login();
//                    }
//                } else {
//                    message = "Please enter valid credentials";
//                    //Response can be null because of mismatch or omission of required data.
//                    //Handle error code
//                    Log.e("email: ", userName);
//                    Log.e("password: ", userPassword);
//                    if (accessToken != null) {
//                        computeData(accessToken);
//                        Log.e("role: ", role);
//                        Log.e("email: ", emailAddress);
//                        Log.e("roleId: ", roleId);
//                        Log.e("iat: ", iat);
//                        Log.e("exp: ", exp);
//                    } else {
////                        message = "AccessToken is " + accessToken;
//                        message = "Register to access the application";
//                    }
//                }
//                RunOnUiThreadMethod(message);
//            }
//
//            @Override
//            public void onFailure(@NotNull ApolloException e) {
//                progressDialog.dismiss();
//                message = e.getMessage();
//                Log.e("onFailure: ", e.getMessage());
//                //Run on UI thread
//                RunOnUiThreadMethod(message);
//            }
//        });
//    }

    /**
     * Create method to run on UI Thread for toasting messages to the user in different situations.
     */
    private void RunOnUiThreadMethod(String message) {
        //Run on UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toasty.error(getApplicationContext(), message, Toasty.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * User Shared Preference data
     */
    private void computeData(String accessToken) {
        // Get JWT data.
        role = jwt.getClaim("roleName").asString();
        emailAddress = jwt.getClaim("email").asString();
        roleId = jwt.getClaim("roleId").asString();
        iat = jwt.getClaim("iat").asString();
        exp = jwt.getClaim("exp").asString();

        //TODO: Delete this Logcat items once done testing
        Log.e("role: ", role);
        Log.e("email: ", emailAddress);
        Log.e("roleId: ", roleId);
        Log.e("iat: ", iat);
        Log.e("exp: ", exp);

        // Add data to sharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("emailAddress", emailAddress);
        editor.putString("role", role);
        editor.putString("roleId", roleId);
        editor.putString("iat", iat);
        editor.putString("exp", exp);
        editor.apply();
    }

    /**
     * Initialize activity widgets and set click listeners
     */
    private void initViews() {
        // Views
        email = findViewById(R.id.user_name);
        passwordText = findViewById(R.id.user_password);
        loginButton = findViewById(R.id.loginButton);
        forgotPass = findViewById(R.id.forgotpass);
        signUpTextView = findViewById(R.id.signUp);

        //Click Listeners
        loginButton.setOnClickListener(this);
        forgotPass.setOnClickListener(this);
        signUpTextView.setOnClickListener(this);
    }

    /**
     * Handle click events
     */
    @Override
    public void onClick(View view) {
        getUserStrings();

        switch (view.getId()) {
            case R.id.loginButton:
                //Check if login details isValid
                initProgressDialog();
                isValidDetails();
                break;

            case R.id.forgotpass:
                forgotPassword(view);
                break;

            case R.id.signUp:
                transitionToSignUpActivity();
                break;
        }
    }

    /**
     * Transition
     */
    private void transitionToSignUpActivity() {
        Intent signUpIntent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(signUpIntent);
//        Uri webPage = Uri.parse("www.google.com");
//        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivity(intent);
//        }
    }

    private void getUserStrings() {
        // Text
        userName = email.getEditText().getText().toString().trim();
        userPassword = passwordText.getEditText().getText().toString().trim();
    }

    private void forgotPassword(View view) {
        forgotPasswordIntent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(forgotPasswordIntent);
    }

    private void isValidDetails() {
        checkUserName();
        checkPassword();

        if (!isValidPass || !isValidEmail) return;
        //Set up Clients
//        setupClients();

    }

    /**
     * Handle Login process
     */
    private void login() {
        //Transition to MainActivity
        transitionToMainActivity();
    }

    private void initProgressDialog() {
        progressDialog.setMessage("Logging in " + userName);
        progressDialog.show();
    }

    private void transitionToMainActivity() {
        mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.putExtra("UserName", userName);
        mainActivityIntent.putExtra("UserPassword", userPassword);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        progressDialog.dismiss();
        finish();
    }

    private void checkPassword() {
        if (userPassword != null && !userPassword.isEmpty()) {
            if (userPassword.length() < 8) {
                progressDialog.dismiss();
                passwordText.setError("Minimum 8 character password required!");
                passwordText.setErrorEnabled(true);
                passwordText.setFocusable(true);
                isValidPass = false;
            } else {
                passwordText.setErrorEnabled(false);
                passwordText.setFocusable(false);
                isValidPass = true;
            }
        } else {
            progressDialog.dismiss();
            passwordText.setError("Password required!");
            passwordText.setErrorEnabled(true);
            passwordText.setFocusable(true);
            isValidPass = false;
        }
    }

    private void checkUserName() {
        if (userName != null && !userName.isEmpty()) {
            Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(userName);
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
            email.setError("Email required!");
            email.setErrorEnabled(true);
            email.setFocusable(true);
            isValidEmail = false;
        }
    }

}
