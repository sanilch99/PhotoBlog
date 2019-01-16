package org.sanilchawla.photoblog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private Toolbar saToolbar;
    private String Userid;
    private CircleImageView setupImage;
    private Boolean isChanged = false;
    private Uri mainImageUri = null;
    private EditText yourName;
    private Button doneBtn;
    private StorageReference mStorageReference;
    private FirebaseAuth firebaseAuth;
    private ProgressBar setupPro;
    private FirebaseFirestore firebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        saToolbar = (Toolbar) findViewById(R.id.setupToolbar);
        setSupportActionBar(saToolbar);
        getSupportActionBar().setTitle("Account Setup");
        setupImage = (CircleImageView) findViewById(R.id.setup_image);
        yourName = (EditText) findViewById(R.id.your_name);
        doneBtn = (Button) findViewById(R.id.account_setupdone);
        setupPro = (ProgressBar) findViewById(R.id.setup_progress);
        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        Userid = firebaseAuth.getCurrentUser().getUid();
        setupPro.setVisibility(View.VISIBLE);
        doneBtn.setEnabled(false);


        //Retrieving DATA from Firestore
        firebaseFirestore.collection("Users").document(Userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        Toast.makeText(SetupActivity.this, "Data Exists", Toast.LENGTH_LONG).show();
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        yourName.setText(name);
                        mainImageUri=Uri.parse(image);
                        RequestOptions placeholderrequest = new RequestOptions();
                        placeholderrequest.placeholder(R.drawable.default_image);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderrequest).load(image).into(setupImage);
                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore error" + error, Toast.LENGTH_LONG).show();
                }
                setupPro.setVisibility(View.INVISIBLE);
                doneBtn.setEnabled(true);
            }
        });

        //Account Setup done
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String username = yourName.getText().toString();

                //Checking for empty fields
                if ((!TextUtils.isEmpty(username)) && (mainImageUri != null)) {
                    setupPro.setVisibility(View.VISIBLE);
                    //if new profile pic
                    if (isChanged) {

                        Userid = firebaseAuth.getCurrentUser().getUid();
                        StorageReference imagePath = mStorageReference.child("profile_images").child(Userid + ".jpeg");
                        imagePath.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {
                                    storeFirestore(task, username);
                                } else {
                                    String errorMessage = task.getException().getMessage();
                                    setupPro.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SetupActivity.this, "Error" + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    //if no change in profile pic
                    else {
                        storeFirestore(null, username);
                    }
                }
            }
        });

        //Asking for permission + setting Image
        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        BringImagePicker();
                    }
                } else {
                    BringImagePicker();
                }
            }
        });
    }

    //Crop Image Lib - image selector
    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);
    }

    //Getting result from image picker
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                setupImage.setImageURI(mainImageUri);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    //storing data in Firestore
    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String username) {
        Uri downloadUri=mainImageUri;
        //setting new image
        if (task != null) {
            downloadUri = task.getResult().getDownloadUrl();
        }
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", username);
        userMap.put("image", downloadUri.toString());
        //storing data
        firebaseFirestore.collection("Users").document(Userid).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SetupActivity.this, "Users settings have been saved", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore error" + error, Toast.LENGTH_LONG).show();
                }
                setupPro.setVisibility(View.INVISIBLE);
            }
        });
    }
}
