package org.sanilchawla.photoblog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ToolbarWidgetWrapper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.FileStoreAttributeView;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private ImageView mPostImage;
    private EditText mImageDesc;
    private Button mPostButton;

    private Uri PostImageUri=null;

    private Toolbar newPostToolbar;

    private String currentUserId;

    private ProgressBar postProgressBar;
    private FirebaseFirestore mFirebaseFirestore;
    private StorageReference mStorageReference;
    private FirebaseAuth mAuth;

    private Bitmap compressedImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        newPostToolbar=(Toolbar) findViewById(R.id.NewPostToolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPostButton=(Button)findViewById(R.id.PostBtn);
        mImageDesc=(EditText)findViewById(R.id.postDesc);
        mPostImage=(ImageView)findViewById(R.id.PhotoToPost);
        postProgressBar=(ProgressBar)findViewById(R.id.new_post_progress);

        mFirebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        mStorageReference=FirebaseStorage.getInstance().getReference();
        currentUserId=mAuth.getCurrentUser().getUid();

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BringImagePicker();
            }
        });

        //POSTING IMAGE STEP 1:- STORE IMAGE TO BE POSTED IN FIRESTORE
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String desc=mImageDesc.getText().toString();
                if((!TextUtils.isEmpty(desc))&&(PostImageUri!=null))
                {
                    postProgressBar.setVisibility(View.VISIBLE);
                    final String randomName=UUID.randomUUID().toString();

                    StorageReference filePath=mStorageReference.child("post_images").child(randomName+".jpg");
                    filePath.putFile(PostImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                            final String downloadURL=task.getResult().getDownloadUrl().toString();

                            if(task.isSuccessful())
                            {
                                //WHEN IMAGE UPLOADED, CREATING THUMBNAIL BY COMPRESSING INTO BITMAP USING BYTE ARRAY to upload to firestore using put byte
                                File newImageFile=new File(PostImageUri.getPath());
                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(200)
                                            .setMaxWidth(200)
                                            .setQuality(10)
                                            .compressToBitmap(newImageFile);
                                }catch (IOException e)
                                {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                byte[] thumbData =baos.toByteArray();

                                UploadTask uploadTask=mStorageReference.child("post_images/thumbs").
                                        child(randomName+".jpeg").putBytes(thumbData);

                                //IF IMAGE COMPRESSION AND UPLOAD FAILED
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                                //ERROR HANDILING
                                    }
                                });

                                //IF IMAGE SUCCESFULLY COMPRESSED AND UPLOADED
                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        String downloadThumbUri=taskSnapshot.getDownloadUrl().toString();

                                        Map<String, Object> postMap=new HashMap<>();
                                        postMap.put("image_url",downloadURL);
                                        postMap.put("desc",desc);
                                        postMap.put("userId",currentUserId);
                                        postMap.put("thumbURL",downloadThumbUri);
                                        postMap.put("timestamp",FieldValue.serverTimestamp());

                                        mFirebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(NewPostActivity.this,"Post was posted",Toast.LENGTH_LONG).show();
                                                    sendToMain();
                                                }
                                                else {
                                                    String errorMessage=task.getException().getMessage();
                                                    Toast.makeText(NewPostActivity.this,"Error : "+errorMessage,Toast.LENGTH_LONG).show();
                                                }
                                                postProgressBar.setVisibility(View.INVISIBLE);
                                            }
                                        });



                                    }
                                });


                            }else{
                                postProgressBar.setVisibility(View.INVISIBLE);

                            }
                        }
                    });



                }
            }
        });

    }

    //Crop Image Lib - image selector
    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512,512)
                .setAspectRatio(1, 1)
                .start(NewPostActivity.this);
    }

    //Getting result from image picker
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                    PostImageUri=result.getUri();
                    mPostImage.setImageURI(PostImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }

        }
    }

    private void sendToMain(){
        Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

}
