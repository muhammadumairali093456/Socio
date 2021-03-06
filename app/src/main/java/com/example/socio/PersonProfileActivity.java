package com.example.socio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName,userProfName,userCountry,userGender,userDob,userRelation,userStatus;
    private CircleImageView userProfileImage;
    private Button SendFriendReqbutton , DeclineFriendRequestbutton;

    private DatabaseReference FriendRequestRef ,UsersRef ,FriendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId ,receiverUserId , CURRENT_STATE ,saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        UsersRef= FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("FriendRequest");
        FriendsRef =  FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Friends");

        senderUserId = mAuth.getCurrentUser().getUid();

        IntializeFields();

        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myDoB = dataSnapshot.child("dob").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.with(PersonProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userName.setText(myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userCountry.setText("Country: "+myCountry);
                    userGender.setText("gender: "+myGender);
                    userDob.setText("DoB: "+myDoB);
                    userRelation.setText("Relationship: "+myRelationStatus);

                    MaintanaceofButton();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestbutton.setEnabled(false);

        if(!senderUserId.equals(receiverUserId))
        {
            SendFriendReqbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendFriendReqbutton.setEnabled(false);
                    if(CURRENT_STATE .equals("not_friends"))
                    {
                        SendFriendRequestToaPerson();
                    }
                    if(CURRENT_STATE.equals("request_sent"))
                    {
                        CancelFriendrequest();
                    }
                    if(CURRENT_STATE.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("Friends"))
                    {
                        UnFriendAnExistingFriend();
                    }
                }
            });
        }
        else
        {
            DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
            SendFriendReqbutton.setVisibility(View.INVISIBLE);
        }

    }

    private void UnFriendAnExistingFriend() {

        FriendsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendFriendReqbutton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendReqbutton.setText("Send Friend Request");

                                                DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestbutton.setEnabled(false);

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());
        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                FriendRequestRef.child(receiverUserId).child(senderUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    SendFriendReqbutton.setEnabled(true);
                                                                    CURRENT_STATE = "Friends";
                                                                    SendFriendReqbutton.setText("Unfriend this Person");

                                                                    DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                                                    DeclineFriendRequestbutton.setEnabled(false);

                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void CancelFriendrequest() {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendFriendReqbutton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendReqbutton.setText("Send Friend Request");

                                                DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestbutton.setEnabled(false);

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void MaintanaceofButton() {
        FriendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.hasChild(receiverUserId))
                        {
                            String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if(request_type.equals("sent"))
                            {
                                CURRENT_STATE = "request_sent";
                                SendFriendReqbutton.setText("Cancel Friend Request");

                                DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                DeclineFriendRequestbutton.setEnabled(false);
                            }
                            else if (request_type.equals("received"))
                            {
                                CURRENT_STATE = "request_received";
                                SendFriendReqbutton.setText("Accept Friend Request");

                                DeclineFriendRequestbutton.setVisibility(View.VISIBLE);
                                DeclineFriendRequestbutton.setEnabled(true);
                                DeclineFriendRequestbutton.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v) {
                                        CancelFriendrequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            FriendsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot)
                                        {
                                            if (dataSnapshot.hasChild(receiverUserId))
                                            {
                                                CURRENT_STATE = "Friends";
                                                SendFriendReqbutton.setText("UnFriend this Person");

                                                DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestbutton.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError)
                                        {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void SendFriendRequestToaPerson() {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendFriendReqbutton.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                SendFriendReqbutton.setText("Cancel Friend Request");

                                                DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestbutton.setEnabled(false);

                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void IntializeFields() {
        userName = (TextView) findViewById(R.id.person_username);
        userProfName = (TextView) findViewById(R.id.person_full_name);
        userProfileImage = (CircleImageView) findViewById(R.id.person_profile_pic);
        userStatus = (TextView) findViewById(R.id.person_profile_status);
        userCountry = (TextView) findViewById(R.id.person_country);
        userGender = (TextView) findViewById(R.id.person_Gender);
        userDob = (TextView) findViewById(R.id.person_dob);
        userRelation = (TextView) findViewById(R.id.person_relationship_status);
        SendFriendReqbutton = (Button) findViewById(R.id.person_send_friend_request_btn);
        DeclineFriendRequestbutton = (Button) findViewById(R.id.person_decline_friend_request);

        CURRENT_STATE = "not_friends";

    }
}