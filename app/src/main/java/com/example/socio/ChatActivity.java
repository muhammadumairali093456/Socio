package com.example.socio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar chattoolbar;
    private ImageButton SendMessageButton , SendImageFileButton;
    private EditText userMessageInput;
    private RecyclerView userMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messageAdapter;

    private String messageReceiverID , messageReceiverName  , messageSenderID ,saveCurrentDate ,saveCurrentTime;

    private TextView receiverName , userLastseen;
    private CircleImageView receiverProfileImage;

    private DatabaseReference RootRef ,UsersRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        RootRef = FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference();
        UsersRef= FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Users");


        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("userName").toString();

        IntializeFields();

        DisplayReceiverInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
        FetchMessages();

    }

    private void FetchMessages()
    {
        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        if(dataSnapshot.exists())
                        {
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            messageAdapter.notifyDataSetChanged();
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

                    }
                });
    }

    private void SendMessage() {

        updateUserStatus("online");

        final String messagesText = userMessageInput.getText().toString();
        if(TextUtils.isEmpty(messagesText))
        {
            Toast.makeText(ChatActivity.this,"Please type a message first.......",Toast.LENGTH_SHORT).show();
        }
        else
        {
            String message_sender_ref = "Messages/" + messageSenderID + "/" +messageReceiverID;
            String message_receiver_ref = "Messages/" + messageReceiverID + "/" +messageSenderID;

            DatabaseReference user_messages_key = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();
            String message_push_id = user_messages_key.getKey();

            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(calFordDate.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messagesText);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("type", "Text");
            messageTextBody.put("from", messageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" +message_push_id,messageTextBody);
            messageBodyDetails.put(message_receiver_ref + "/" +message_push_id,messageTextBody);
            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this,"message sent successfully.....", Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(ChatActivity.this,"Error:"+message,Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    }
                }
            });
        }
    }

    public void updateUserStatus(String state)
    {
        String saveCurrentDate , saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        UsersRef.child(messageSenderID).child("userState")
                .updateChildren(currentStateMap);
    }

    private void DisplayReceiverInfo()
    {
        receiverName.setText(messageReceiverName);
        RootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                    final String type = dataSnapshot.child("userState").child("type").getValue().toString();
                    final String lastDate = dataSnapshot.child("userState").child("date").getValue().toString();
                    final String lastTime = dataSnapshot.child("userState").child("time").getValue().toString();

                    if(type.equals("online"))
                    {
                        userLastseen.setText("online");
                    }
                    else
                    {
                        userLastseen.setText("last seen:"+ lastTime +"  "+ lastDate);
                    }

                    Picasso.with(ChatActivity.this).load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void IntializeFields()
    {
        chattoolbar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(chattoolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);


        receiverName = (TextView) findViewById(R.id.custom_profile_name);
        userLastseen = (TextView)findViewById(R.id.custom_user_last_seen);
        receiverProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        SendImageFileButton = (ImageButton) findViewById(R.id.send_image_file_button);
        userMessageInput = (EditText) findViewById(R.id.input_message);

        messageAdapter = new MessagesAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.messages_list_user);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setHasFixedSize(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

    }
}