package com.demo.firebasemobileverification.activity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



import com.demo.firebasemobileverification.broadcast.SmsReceiver;

import com.demo.firebasemobileverification.utils.Constants;
import com.demo.firebasemobileverification.utils.GoogleApiHelper;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.spaceo.demo.firebasemobileverification.R;


import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sotsys-112 on 22/9/17.
 */

public class VerificationActivity extends BaseActivity {

    String name, phoneNumber, mVerificationId;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    FirebaseAuth mAuth;
    @BindView(R.id.edtOTP)
    EditText edtOTP;
    @BindView(R.id.btnVerifyIt)
    Button btnVerifyIt;
    @BindView(R.id.tvMessage)
    TextView tvMessage;
    @BindView(R.id.tvWelcome)
    TextView tvWelcome;
    private GoogleApiClient mGoogleApiClient;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        ButterKnife.bind(this);
        name = getIntent().getStringExtra(Constants.NAME);
        phoneNumber = getIntent().getStringExtra(Constants.CONTACT);


        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Log.d("TAG", "onVerificationCompleted:" + phoneAuthCredential);

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.d("TAG", "onVerificationFailed", e);

                Toast.makeText(VerificationActivity.this, "Some thing went wrong", Toast.LENGTH_SHORT).show();
                onBackPressed();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                    Log.d("TAG", "FirebaseAuthInvalidCredentialsException", e);
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                    Log.d("TAG", "FirebaseTooManyRequestsException", e);
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("TAG", "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

        SmsReceiver.bindListener(new SmsListener() {
            @Override
            public void messageReceive(String messageText) {
                Log.d("Text", messageText);
                messageText = messageText.replace(" is your verification code.", "");
                edtOTP.setText(messageText);
            }
        });
    }


    @OnClick(R.id.btnVerifyIt)
    public void onViewClicked() {
        if (!edtOTP.getText().toString().isEmpty()) {
            String code = edtOTP.getText().toString().trim();
            verifyPhoneNumberWithCode(mVerificationId, code);
        }
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            Log.d("TAG", user + "");
                            Toast.makeText(VerificationActivity.this, "Success Verify ", Toast.LENGTH_SHORT).show();
                            dataShow();
                            finish();
                        } else {
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                edtOTP.setError("Invalid code.");
                            }
                        }
                    }
                });
    }

    private void dataShow() {
        tvWelcome.setVisibility(View.GONE);
        edtOTP.setVisibility(View.GONE);
        btnVerifyIt.setVisibility(View.GONE);

        tvMessage.setVisibility(View.VISIBLE);

        tvMessage.setText(name + "your contact has been verified.");
    }


}
