package org.sanilchawla.photoblog;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

public class BlogPostId{
    @Exclude
    public String BlogPostId;
    public <T extends BlogPostId> T withId(@android.support.annotation.NonNull final String id){
        this.BlogPostId=id;
        return (T) this;
    }
}
