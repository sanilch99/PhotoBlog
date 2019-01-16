package org.sanilchawla.photoblog;

import java.sql.Timestamp;
import java.util.Date;

public class BlogPost extends BlogPostId{
    public String userId, image_url, desc, thumbURL;
    public Date timestamp;

    public BlogPost() {
        //EMPTY CONSTRUCTOR
    }

    public BlogPost(String userId, String image_url, String desc, String thumbURL, Date timestamp) {
        this.userId = userId;
        this.image_url = image_url;
        this.desc = desc;
        this.thumbURL = thumbURL;
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getThumbURL() {
        return thumbURL;
    }

    public void setThumbURL(String thumbURL) {
        this.thumbURL = thumbURL;
    }

}
