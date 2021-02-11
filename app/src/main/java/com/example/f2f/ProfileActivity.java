package com.example.f2f;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private String recieverUserID="", recieverUserImage="", recieverUserName="";

    private ImageView background_profile_view;
    private TextView name_profile;
    private Button add_friend, decline_friend_request;

    private FirebaseAuth mAuth;
    private String senderUserId;
    private String currentState = "new";
    private DatabaseReference friendRequestRef, contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();


        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Request");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        recieverUserID = getIntent().getExtras().get("visit_user_id").toString();
        recieverUserImage = getIntent().getExtras().get("profile_image").toString();
        recieverUserName = getIntent().getExtras().get("profile_name").toString();

        background_profile_view = findViewById(R.id.background_profile_view);
        name_profile  = findViewById(R.id.name_profile);
        add_friend = findViewById(R.id.add_friend);
        decline_friend_request = findViewById(R.id.decline_friend_request);

        Picasso.get().load(recieverUserImage).into(background_profile_view);
        name_profile.setText(recieverUserName);

        manageClickEvents();
    }



    private void manageClickEvents() {

        friendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(recieverUserID)){
                            String requestType = dataSnapshot.child(recieverUserID)
                                    .child("request_type").getValue().toString();

                            if(requestType.equals("sent")){
                                currentState = "request_sent";
                                add_friend.setText("Cancel Friend Request");
                            }
                            else if(requestType.equals("recieved")){
                                currentState = "request_recieved";
                                add_friend.setText("Accept Friend Request");

                                decline_friend_request.setVisibility(View.VISIBLE);
                                decline_friend_request.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelFriendRequest();
                                    }
                                });
                            }
                        }

                        else{
                            contactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(recieverUserID)){
                                                currentState = "friends";
                                                add_friend.setText("Delete Contact");
                                            }
                                            else{
                                                currentState = "new";
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if(senderUserId.equals(recieverUserID)){
            add_friend.setVisibility(View.GONE);
        }
        else{
            add_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentState.equals("new")) {
                        sendFriendRequest();
                    }
                    if(currentState.equals("request_sent")){
                        cancelFriendRequest();
                    }
                    if(currentState.equals("request_recieved")){
                        acceptFriendRequest();
                    }
                    if(currentState.equals("request_sent")){
                        cancelFriendRequest();
                    }
                }
            });
        }

    }



    private void acceptFriendRequest() {
        contactsRef.child(senderUserId).child(recieverUserID)
                .child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactsRef.child(recieverUserID).child(senderUserId)
                                    .child("Contact").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                friendRequestRef.child(senderUserId).child(recieverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    friendRequestRef.child(recieverUserID).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        currentState = "friends";
                                                                                        add_friend.setText("Delete Contact");

                                                                                        decline_friend_request.setVisibility(View.GONE);
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
                    }
                });
    }



    private void cancelFriendRequest() {
        friendRequestRef.child(senderUserId).child(recieverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(recieverUserID).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                currentState = "new";
                                                add_friend.setText("Add Friend");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void sendFriendRequest() {
        friendRequestRef.child(senderUserId).child(recieverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(recieverUserID).child(senderUserId)
                                    .child("request_type").setValue("recieved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                currentState = "request_sent";
                                                add_friend.setText("Cancel Friend Request");
                                                Toast.makeText(ProfileActivity.this,
                                                        "Friend Request Sent",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }

                    }
                });
    }
}
