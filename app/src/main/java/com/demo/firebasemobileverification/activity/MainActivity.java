package com.demo.firebasemobileverification.activity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
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
import android.widget.Toast;


import com.demo.firebasemobileverification.utils.Constants;
import com.demo.firebasemobileverification.utils.GoogleApiHelper;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.spaceo.demo.firebasemobileverification.R;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.edtFirstName)
    EditText edtFirstName;
    @BindView(R.id.edtLastName)
    EditText edtLastName;
    @BindView(R.id.edtEmail)
    EditText edtEmail;
    @BindView(R.id.edtPassword)
    EditText edtPassword;
    @BindView(R.id.edtContact)
    EditText edtContact;
    @BindView(R.id.btnVerify)
    Button btnVerify;
    @BindView(R.id.btnClear)
    Button btnClear;

    String first_name, last_name, email, password, contact_no;

    private static final int RESOLVE_HINT = 105;
    private GoogleApiClient apiClient;
    private static String TAG = "log_tag";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();
        //Request to get number
        requestHint();

        ButterKnife.bind(this);
    }

    @OnClick({R.id.btnVerify, R.id.btnClear,R.id.edtContact})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnVerify:
                if (edtFirstName.getText().toString().isEmpty() && edtLastName.getText().toString().isEmpty() && edtEmail.getText().toString().isEmpty() && edtPassword.getText().toString().isEmpty() && edtContact.getText().toString().isEmpty()) {
                    Toast.makeText(this, R.string.alert_message, Toast.LENGTH_SHORT).show();
                } else if (!edtFirstName.getText().toString().isEmpty() && !edtLastName.getText().toString().isEmpty() && !edtEmail.getText().toString().isEmpty() && !edtPassword.getText().toString().isEmpty() && !edtContact.getText().toString().isEmpty()) {
                    first_name = edtFirstName.getText().toString().trim();
                    last_name = edtLastName.getText().toString().trim();
                    email = edtEmail.getText().toString().trim();
                    password = edtPassword.getText().toString().trim();
                    contact_no = edtContact.getText().toString().trim();
                    Intent intentVerify = new Intent(this, VerificationActivity.class);
                    intentVerify.putExtra(Constants.NAME, first_name);
                    intentVerify.putExtra(Constants.CONTACT, contact_no);
                    startActivity(intentVerify);
                    clearAllData();
                } else {
                    Toast.makeText(this, R.string.alert_message, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnClear:
                clearAllData();
                break;
            case R.id.edtContact:
                requestHint();
                break;
        }
    }

    private void clearAllData() {
        edtFirstName.setText("");
        edtLastName.setText("");
        edtEmail.setText("");
        edtPassword.setText("");
        edtContact.setText("");
    }

    private void requestHint() {

        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(
                apiClient, hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(), RESOLVE_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                Log.d(TAG, "phone number :" + credential.getId());
                edtContact.setText(credential.getId());
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended:" + i);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
