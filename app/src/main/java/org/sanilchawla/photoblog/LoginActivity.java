package org.sanilchawla.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText loginEmail;
    private EditText loginpass;
    private Button loginbtn;
    private Button loginregbtn;
    private FirebaseAuth mAuth;
    private ProgressBar loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();
        loginProgress=(ProgressBar)findViewById(R.id.login_progress);
        loginEmail=(EditText)findViewById(R.id.login_email);
        loginbtn=(Button)findViewById(R.id.login);
        loginpass=(EditText)findViewById(R.id.login_password);
        loginregbtn=(Button)findViewById(R.id.login_register);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText=loginEmail.getText().toString();
                final String passText=loginpass.getText().toString();

                if((!TextUtils.isEmpty(emailText)) && (!TextUtils.isEmpty(passText))) {
                    loginProgress.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(emailText,passText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {
                                sendToMain();
                            }
                            else
                            {
                                String errorMessage=task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error : " + errorMessage,Toast.LENGTH_LONG).show();
                                loginEmail.getText().clear();
                                loginpass.getText().clear();
                            }
                            loginProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
        loginregbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToReg();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            sendToMain();
        }
    }
    private void sendToMain(){
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
    private void sendToReg(){
        Intent RegIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(RegIntent);
        finish();
    }
}


