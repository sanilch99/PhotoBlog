package org.sanilchawla.photoblog;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FloatingActionButton mPost;
    private FirebaseFirestore mFirebaseFirestore;
    private String currentUserId;
    private BottomNavigationView mBottomNavigationView;

    private HomeFragment mHomeFragment;
    private AccountFragment mAccountFragment;
    private NotificationFragement mNotificationFragement;
    private ListenerRegistration registration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.main_Toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Photo Blog App");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore=FirebaseFirestore.getInstance();

        mPost=(FloatingActionButton)findViewById(R.id.add_post);
        mBottomNavigationView=(BottomNavigationView)findViewById(R.id.mainBottomNav);

        if(mAuth.getCurrentUser()!=null) {
            //FRAGMENTS
            mHomeFragment = new HomeFragment();
            mAccountFragment = new AccountFragment();
            mNotificationFragement = new NotificationFragement();

            replaceFragment(mHomeFragment);

            mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.btm_menu_home:
                            replaceFragment(mHomeFragment);
                            return true;
                        case R.id.btm_menu_account:
                            replaceFragment(mAccountFragment);
                            return true;
                        case R.id.btm_menu_notif:
                            replaceFragment(mNotificationFragement);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            //Adding posts
            mPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent NewPostActIntent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(NewPostActIntent);
                }
            });
        }

    }



    //Checking if already signed in
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user==null) {
            sendToLogin();
        }
        else
        {
            currentUserId=mAuth.getCurrentUser().getUid();
            mFirebaseFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        if(!task.getResult().exists())
                        {
                            sendToSetup();
                        }

                    }else {
                        String errorMessage=task.getException().getMessage();
                        Toast.makeText(MainActivity.this,"Error :"+errorMessage,Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    //Options menu :: inflater
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //Activity transitions
    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
    private void sendToSetup() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        startActivity(setupIntent);
        finish();
    }


    //Logout
    private void logOut() {
        mAuth.signOut();
        sendToLogin();
    }

    //Options Menu selector
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logoutBtn: {
                logOut();
                return true;
            }
            case R.id.account_settingsBtn:{
                Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(setupIntent);
            }
            default:
                return true;
        }
    }

    private void replaceFragment(android.support.v4.app.Fragment fragment){
        android.support.v4.app.FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();
    }

}
