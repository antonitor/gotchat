package com.antonitor.gotchat.ui.chatroom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.antonitor.gotchat.R;
import com.antonitor.gotchat.databinding.ActivityChatRoomBinding;
import com.antonitor.gotchat.model.Message;
import com.antonitor.gotchat.sync.FirebaseDatabaseRepository;
import com.antonitor.gotchat.sync.FirebaseAuthHelper;
import com.vanniktech.emoji.EmojiPopup;

import java.util.List;


public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "CHAT_ACTIVITY";
    private static final int RC_PHOTO_PICKER = 1985;
    private static final int RC_CAMERA_ACTION = 2020;
    private ActivityChatRoomBinding dataBinding;
    private ChatViewModel viewModel;
    private ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat_room);

        EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(dataBinding.getRoot())
                .build(dataBinding.messageEditText);
        dataBinding.emojiPicker.setOnClickListener(view -> emojiPopup.toggle());

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.setChatRoom(getIntent().getExtras().getParcelable(getString(R.string.key_chatroom)));
        setTitle(viewModel.getChatRoom().getTitle());

        adapter = new ChatAdapter(this);
        dataBinding.messageRecycleView.setAdapter(adapter);
        viewModel.getMessages().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                adapter.swapMessages(messages);
                Log.d(TAG, "-------------------->>>>>>>> New messages: " + messages.size());
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        dataBinding.messageRecycleView.setLayoutManager(layoutManager);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = adapter.getItemCount();
                int lastVisiblePosition =
                        layoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    dataBinding.messageRecycleView.scrollToPosition(positionStart);
                }
            }
        });

        //Handle user input
        dataBinding.sendButton.setOnClickListener(ChatActivity.this::takePhotoListener);
        dataBinding.photoPickerButton.setOnClickListener(ChatActivity.this::sendImageListener);
        dataBinding.messageEditText.addTextChangedListener(userInputWatcher());
    }

    /**
     * Sets listeners and behaviour for all user input views
     * @return TextWatcher object
     */
    private TextWatcher userInputWatcher() {
            return new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.toString().trim().length() > 0) {
                        dataBinding.sendButton.setImageResource(R.drawable.ic_send_white_24dp);
                        dataBinding.sendButton.setOnClickListener(ChatActivity.this::sendTextListener);
                        dataBinding.photoPickerButton.setVisibility(View.INVISIBLE);
                    } else {
                        dataBinding.sendButton.setImageResource(R.drawable.ic_camera_grey_24dp);
                        dataBinding.sendButton.setOnClickListener(ChatActivity.this::takePhotoListener);
                        dataBinding.photoPickerButton.setVisibility(View.VISIBLE);
                        dataBinding.photoPickerButton.setOnClickListener(ChatActivity.this::sendImageListener);
                    }
                }
                @Override
                public void afterTextChanged(Editable editable) {
                }
            };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_PHOTO_PICKER:
                if (resultCode == RESULT_OK) {
                    Uri localImage = data.getData();
                    Message tempMsg = new Message(
                            null,
                            viewModel.getChatRoom().getId(),
                            FirebaseAuthHelper.getInstance().getFirebaseUser().getPhoneNumber(),
                            null,
                            localImage.toString(),
                            null);
                    Message message= FirebaseDatabaseRepository.getInstance().postMessage(tempMsg);
                    break;
                }
            case RC_CAMERA_ACTION:
                /*
                if (resultCode == RESULT_OK)  {
                    viewModel.setBitmap((Bitmap) data.getExtras().get("data"));
                    viewModel.uploadImage(FirebaseStorageRepository.getInstance()
                            .getMsgImageStorageReference(), );
                    viewModel.getImageUrl().observe(this, url -> {
                        Message message = new Message(viewModel.getChatRoom().getId(),
                                null,
                                FirebaseDatabaseRepository.getInstance().getFirebaseUser().getPhoneNumber(),
                                url);
                        viewModel.postMessage(message);
                    });
                }

                 */
        }
    }

    private void sendTextListener(View view) {
        Log.d(TAG, "SEND CLICKED --------------------------------");
        String text = dataBinding.messageEditText.getText().toString()
                .replaceFirst("\\s+$", "")
                .replaceFirst("^\\s+", "");
        String roomId = viewModel.getChatRoom().getId();
        String user = FirebaseAuthHelper.getInstance().getFirebaseUser()
                .getPhoneNumber();
        Message message = new Message(null, roomId, user, text, null,null);
        dataBinding.messageEditText.setText("");
        FirebaseDatabaseRepository.getInstance().postMessage(message);
    }

    private void sendImageListener(View view) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
        startActivityForResult(chooserIntent, RC_PHOTO_PICKER);
    }

    private void takePhotoListener(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, RC_CAMERA_ACTION);
        }
    }
}
