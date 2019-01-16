package org.sanilchawla.photoblog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public Context context;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseAuth mAuth;


    public BlogRecyclerAdapter(List<BlogPost> blog_list) {

        this.blog_list = blog_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
        context = parent.getContext();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final String blogPostId = blog_list.get(position).BlogPostId;
        final String currentUserId = mAuth.getCurrentUser().getUid();
        String desc_data = blog_list.get(position).getDesc();
        holder.setDescText(desc_data);

        String imageUrl = blog_list.get(position).getImage_url();
        String thumbUri = blog_list.get(position).getThumbURL();
        holder.setImage(imageUrl, thumbUri);


        String userId = blog_list.get(position).getUserId();
        mFirebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String UserNameF = task.getResult().getString("name");
                    String Userimage = task.getResult().getString("image");
                    holder.setUserNameandImage(UserNameF, Userimage);

                }
            }
        });

        long milliseconds = blog_list.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(milliseconds)).toString();
        holder.setDate(dateString);

        if (mAuth.getCurrentUser()!=null) {
            //updateicon
            mFirebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (documentSnapshot.exists()) {
                        holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_red));

                    } else {
                        holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_gray));
                    }
                }
            });

            //likes
            holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFirebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (!task.getResult().exists()) {
                                Map<String, Object> LikesMap = new HashMap<>();
                                LikesMap.put("timestamp", FieldValue.serverTimestamp());
                                mFirebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(LikesMap);
                            } else {
                                mFirebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();
                            }
                        }
                    });
                    Map<String, Object> LikesMap = new HashMap<>();
                    LikesMap.put("timestamp", FieldValue.serverTimestamp());

                    mFirebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(LikesMap);

                }
            });
            mFirebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (!documentSnapshots.isEmpty()) {
                        int count = documentSnapshots.size();
                        holder.updateLikesCount(count);
                    } else {
                        holder.updateLikesCount(0);
                    }
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView descView;
        private ImageView blogImageView;
        private TextView BlogPostUsername;
        private CircleImageView UserProfileImage;
        private TextView BlogDate;
        private ImageView blogLikeBtn;
        private TextView blogLikeCount;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);


        }

        public void setDescText(String descText) {
            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);

        }

        public void setImage(String downloadURI, String thumbUri) {
            blogImageView = mView.findViewById(R.id.blog_image);
            RequestOptions placeholderOptions = new RequestOptions();
            placeholderOptions.placeholder(R.drawable.default_image);
            Glide.with(context).applyDefaultRequestOptions(placeholderOptions).load(downloadURI).thumbnail(
                    Glide.with(context).load(thumbUri)).into(blogImageView);
        }

        public void setUserNameandImage(String Username, String UserProfilePic) {
            BlogPostUsername = mView.findViewById(R.id.blog_user_name);
            BlogPostUsername.setText(Username);
            UserProfileImage = mView.findViewById(R.id.blog_userImage);
            RequestOptions placeholderOptions = new RequestOptions();
            placeholderOptions.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOptions).load(UserProfilePic).into(UserProfileImage);
        }

        public void setDate(String Date) {
            BlogDate = mView.findViewById(R.id.blog_date);
            BlogDate.setText(Date);
        }

        public void updateLikesCount(int count) {
            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            blogLikeCount.setText(count + " Likes");
        }

    }
}