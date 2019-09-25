package org.bonkerfield.loft.immediacy.activities;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bonkerfield.loft.immediacy.R;
import org.bonkerfield.loft.immediacy.adapters.ChatAdapter;
import org.bonkerfield.loft.immediacy.constants.Constant;
import org.bonkerfield.loft.immediacy.models.Message;
import org.bonkerfield.loft.immediacy.models.UserProfile;
import org.bonkerfield.loft.immediacy.utils.DatabaseUtils;


public class ChatActivity extends AppCompatActivity {

    public static final String PARTNER_USER_PROFILE = "PARTNER_USER_PROFILE";

    private String conversationId;

    private List<Message> messages;

    private ChatAdapter chatAdapter;

    private EditText messageEditView;
    private ImageButton messageSendButton;
    private final TextWatcher editMessageTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (s.length() == 0) {
                messageSendButton.setEnabled(false);
            } else {
                messageSendButton.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private ListView messageListView;
    private ProgressBar progressBar;
    private final ChildEventListener messageListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            Message message = dataSnapshot.getValue(Message.class);

            if (message != null) {
                chatAdapter.add(message);
                messageListView.setSelection(messages.size() - 1);
            } else {
                Log.w(Constant.NEARBY_CHAT, "No messages");
            }

            if (progressBar.getVisibility() == View.VISIBLE) {
                hideProgressBar();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(Constant.NEARBY_CHAT, "loadPost:onCancelled", databaseError.toException());
        }
    };
    private UserProfile conversationPartner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // spinner

        progressBar = (ProgressBar) findViewById(R.id.chat_spinner);

        conversationPartner = (UserProfile) getIntent().getSerializableExtra(PARTNER_USER_PROFILE);

        messageEditView = (EditText) findViewById(R.id.message_edit);
        messageEditView.addTextChangedListener(editMessageTextWatcher);

        messageSendButton = (ImageButton) findViewById(R.id.message_send);
        messageSendButton.setEnabled(false);
        messageSendButton.setOnClickListener(v -> sendMessage());

        messages = new ArrayList<>();

        conversationId = getConversationId(conversationPartner.getId());

        DatabaseUtils.getMessagesByConversationId(conversationId)
                .addChildEventListener(messageListener);

        chatAdapter = new ChatAdapter(this, messages);
        messageListView = (ListView) findViewById(R.id.message_list);
        messageListView.setVisibility(View.GONE);

        messageListView.setAdapter(chatAdapter);

        // set conversation title
        setTitle(conversationPartner.getUserName());

        // hide keyboard by default
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }


    private void sendMessage() {
        if(messages.size()>=6){
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setTitle("Max number of messages reached");

            alertBuilder.setMessage("You've reached the max number of allowed messages.  In the future, try to make contact with your friend with more Immediacy.").setNegativeButton("Ok", (dialogInterface, i) -> {
                dialogInterface.cancel();
            });

            AlertDialog alertDialog = alertBuilder.create();
            alertDialog.show();
            return;
        }

        Message newMessage = new Message();

        // text message
        String textContent = messageEditView.getText().toString();
        newMessage.setType(Message.Type.TEXT);
        newMessage.setContent(textContent);

        messageEditView.setText("");


        newMessage.setDate(new Date());
        newMessage.setSenderId(DatabaseUtils.getCurrentUUID());

        String id = DatabaseUtils.getMessagesByConversationId(conversationId)
                .push()
                .getKey();

        newMessage.setId(id);

        DatabaseUtils.getMessagesByConversationId(conversationId)
                .child(id)
                .setValue(newMessage);
    }


    private String getConversationId(String partnerId) {
        String myId = DatabaseUtils.getCurrentUUID();

        if (myId.compareTo(partnerId) < 0) {
            return myId + "-" + partnerId;
        } else {
            return partnerId + "-" + myId;
        }
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);

        messageListView.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseUtils.getMessagesByConversationId(conversationId).removeEventListener(messageListener);
    }
}
