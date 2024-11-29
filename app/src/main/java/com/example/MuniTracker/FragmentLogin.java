package com.example.MuniTracker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import com.example.MuniTracker.databinding.FragmentLoginBinding;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import android.widget.TextView;


import java.util.HashMap;
import java.util.Objects;
import java.util.HashMap;

public class FragmentLogin extends AppCompatActivity {

    private FragmentLoginBinding binding;
    private static final String TAG = "LOGIN_OPTIONS_TAG";
    private ProgressDialog progressDialog;
   // private FirebaseAuth firebaseAuth;
   // private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(0);  // Restablece a la visibilidad predeterminada (texto oscuro)
        }*/



        binding = FragmentLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Siusplau espera");
        progressDialog.setCanceledOnTouchOutside(false);

        /*firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.btnGoogle.setSize(SignInButton.SIZE_STANDARD);
        binding.btnGoogle.setColorScheme(SignInButton.COLOR_LIGHT);
        setGoogleButtonText(binding.btnGoogle, getString(R.string.iniciar_amb_google));

        binding.btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutAndRevokeAccess();
            }
        });*/

        binding.buttoninv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MyUtils.toast(LoginActivity.this, "Entrar com a invitat?");
                finish(); //amb finish va a lactivit que hi havia oberta abans
                //startActivity(new Intent(LoginActivity.this,MainActivity.class));
                //finishAffinity();
                //startActivity(new Intent(FragmentLogin.this,MainActivity.class));
            }
        });
    }

    /*private void setGoogleButtonText(SignInButton signInButton, String buttonText) {
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);
            if (v instanceof TextView) {
                ((TextView) v).setText(buttonText);
                return;
            }
        }
    }
    private void signOutAndRevokeAccess() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mGoogleSignInClient.revokeAccess().addOnCompleteListener(LoginActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        beginGoogleLogin();
                    }
                });
            }
        });
    }

    private void beginGoogleLogin() {
        Log.d(TAG, "beginGoogleLogin");
        Intent googleSignInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInnARL.launch(googleSignInIntent);
    }

    private ActivityResultLauncher<Intent> googleSignInnARL = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    Log.d(TAG, "onActivityResult");

                    if (o.getResultCode() == Activity.RESULT_OK) {
                        Intent data = o.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            //inicia be la sessio
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Log.d(TAG, "onActivityResult: ID de la conta: " + account.getId());
                            firebaseAuthWithGoogleAccount(account.getIdToken());
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    } else {
                        Log.d(TAG, "onActivityResult: Cancelant...");
                        MyUtils.toast(LoginActivity.this, "Cancelat...");
                    }
                }
            }
    );

    private void firebaseAuthWithGoogleAccount(String idToken) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: idToken:" + idToken);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        //Iniciar sessio amb google
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                if (authResult.getAdditionalUserInfo().isNewUser()) {
                    //Si es un usuari nou crea la nova conta
                    Log.d(TAG, "onSucces: Nova conta creada...");
                    updateUserInfoDB();
                } else {
                    //Si usuari ja existia, entra directament amb la sessió
                    Log.d(TAG, "onSucces: Iniciant Sessió...");
                    //Si ja existeix, obre un nou MainActivity i tanca els que tenia oberts
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finishAffinity();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: ", e);
            }
        });
    }

    private void updateUserInfoDB() {
        Log.d(TAG, "updateUserInfoDB: ");
        progressDialog.setMessage("Guardant informació de l'usuari...");
        progressDialog.show();

        String idUsuari = firebaseAuth.getUid();
        String emailUsuari = firebaseAuth.getCurrentUser().getEmail();
        String nomUsuari = firebaseAuth.getCurrentUser().getDisplayName();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", idUsuari);
        hashMap.put("email", emailUsuari);
        hashMap.put("name", nomUsuari);
        hashMap.put("profileImageUrl", "");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(idUsuari).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSucces: Usuari guardat...");
                        progressDialog.dismiss();

                        //Si no existeix i el crea correctament, obre un nou MainActivity i tanca els que tenia oberts
                        MyUtils.toast(LoginActivity.this, "updateusaerdb succes");
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(LoginActivity.this, "updateuserdbfailure");
                        MyUtils.toast(LoginActivity.this, "Error en guardar per " + e.getMessage());
                    }
                });
    }*/




    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        /*FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);*/
    }
}