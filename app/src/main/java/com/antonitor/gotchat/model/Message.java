package com.antonitor.gotchat.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.ServerValue;

import androidx.databinding.BindingAdapter;

public class Message implements Parcelable {

    private String messageUUID;
    private String roomID;
    private String text;
    private String author;
    private String localPhotoUrl;
    private String photoUrl;
    private Object timeStamp;

    public Message() {
    }

    public Message(String messageUUID, String roomId, String author, String text, String localPhotoUrl, String photoUrl) {
        this.messageUUID = messageUUID;
        this.roomID = roomId;
        this.text = text;
        this.author = author;
        this.localPhotoUrl = localPhotoUrl;
        this.photoUrl = photoUrl;
        this.timeStamp = ServerValue.TIMESTAMP;
    }

    protected Message(Parcel in) {
        messageUUID = in.readString();
        roomID = in.readString();
        text = in.readString();
        author = in.readString();
        localPhotoUrl = in.readString();
        photoUrl = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public String getMessageUUID() {
        return messageUUID;
    }

    public void setMessageUUID(String messageUUID) {
        this.messageUUID = messageUUID;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLocalPhotoUrl() {
        return localPhotoUrl;
    }

    public void setLocalPhotoUrl(String localPhotoUrl) {
        this.localPhotoUrl = localPhotoUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Object getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Object timeStamp) {
        this.timeStamp = timeStamp;
    }

    /*
    @BindingAdapter("android:visibility")
    public static void setVisibility(View view, Boolean value) {
        view.setVisibility(value ? View.VISIBLE : View.GONE);
    }
     */

    @BindingAdapter("messageImage")
    public static void loadImage(ImageView view, String photoUrl) {
        Glide.with(view.getContext())
                .load(photoUrl).apply(new RequestOptions().circleCrop())
                .into(view);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(messageUUID);
        parcel.writeString(roomID);
        parcel.writeString(text);
        parcel.writeString(author);
        parcel.writeString(localPhotoUrl);
        parcel.writeString(photoUrl);
    }
}
