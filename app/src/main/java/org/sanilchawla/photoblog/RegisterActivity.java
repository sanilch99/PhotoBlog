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

public class RegisterActivity extends AppCompatActivity {

    private EditText rEmail;
    private EditText rPass;
    private EditText rCnfPass;
    private Button reg_btn;
    private Button reg_login_btn;
    private ProgressBar mRegProgressbar;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth=FirebaseAuth.getInstance();

        rEmail=(EditText)findViewById(R.id.regEmail);
        rPass=(EditText)findViewById(R.id.regPass);
        rCnfPass=(EditText)findViewById(R.id.regPassConf);
        reg_btn=(Button)findViewById(R.id.regtologbtn);
        reg_login_btn=(Button)findViewById(R.id.alrlogbtn);
        mRegProgressbar=(ProgressBar)findViewById(R.id.progressBarreg);

        reg_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLogin();
            }
        });

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=rEmail.getText().toString();
                String pass=rPass.getText().toString();
                String confpass=rCnfPass.getText().toString();
                if((!TextUtils.isEmpty(email)) &&(!TextUtils.isEmpty(confpass)) &&(!TextUtils.isEmpty(pass)))
                {
                    if(pass.equals(confpass))
                    {
                        mRegProgressbar.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                               if(task.isSuccessful()) {
                                   Intent setupIntent=new Intent(RegisterActivity.this,SetupActivity.class);
                                   startActivity(setupIntent);
                                   finish();
                               }
                               else
                               {
                                   String errorMessage=task.getException().getMessage();
                                   Toast.makeText(RegisterActivity.this,"Error : "+errorMessage,Toast.LENGTH_LONG).show();
                               }
                                mRegProgressbar.setVisibility(View.GONE);
                            }
                        });
                    }
                    else
                    {
                        Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser cUser=mAuth.getCurrentUser();
        if(cUser!=null)
        {
            sendToMain();
        }
    }
    private void sendToMain() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
    private void sendToLogin() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
