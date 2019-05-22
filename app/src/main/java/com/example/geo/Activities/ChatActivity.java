package com.example.geo.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.geo.*;
import com.example.geo.Models.ChatModel;
import com.example.geo.Models.User;
import com.example.geo.Models.UserDetails;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends MenuActivity {
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    DatabaseReference messageRef, userRef;
    boolean type = false;
    RecyclerView chatRecView;
    Toolbar toolbar;
    String chatwithEmail;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        user = (User) getIntent().getSerializableExtra("userobj");
        chatwithEmail = getIntent().getStringExtra("buddy");
        //Toast.makeText(ChatActivity.this, chatwithEmail, Toast.LENGTH_SHORT).show();
        sendButton = (ImageView) findViewById(R.id.sendButton);
        messageArea = (EditText) findViewById(R.id.messageArea);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        chatRecView = (RecyclerView) findViewById(R.id.chat_recycler_view);
        toolbar = (Toolbar) findViewById(R.id.chat_with_toolbar);

        getSupportActionBar().setTitle(UserDetails.chatwithID);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecView.setHasFixedSize(true);
        chatRecView.setLayoutManager(layoutManager);

        messageRef = FirebaseDatabase.getInstance().getReference("/messages");
        userRef = FirebaseDatabase.getInstance().getReference("/Users");

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if (!messageText.equals("")) {
                    Map<String, String> map = new HashMap<>();
                    map.put("message", messageText);
                    map.put("user", UserDetails.userID);

                    /*if(UserDetails.chatRef == null){
                        String type1 = UserDetails.userID + "_" + UserDetails.chatwithID;
                        UserDetails.userType = "type1";
                        messageRef.child(type1).setValue("none");
                        UserDetails.chatRef = FirebaseDatabase.getInstance().getReference("/messages").child(type1);
                        *//*UserDetails.userType = "type1";
                        UserDetails.chatRef = FirebaseDatabase.getInstance().getReference("/messages").child(UserDetails.userID + "_" + UserDetails.chatwithID);*//*
                    }
*/
                    UserDetails.chatRef.push().setValue(map);
                    messageArea.
                            setText("");
                    messageArea.
                            setHint("Message");
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //int count = 0;
        valueEventListener(userRef, chatwithEmail);

    }




    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView chatMessage;
        TextView userText;
        LinearLayout linearLayout;

        public ChatViewHolder(View itemView) {
            super(itemView);
            chatMessage = (TextView) itemView.findViewById(R.id.text_chat);
            userText = (TextView) itemView.findViewById(R.id.user_chat);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.lin_lay);
        }

        public void setChatMessage(String message) {
            chatMessage.setText(message);
        }


        public void setUserText(String userName, boolean type) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
            if (type) {
                //current logged in user
                params.gravity = Gravity.END;
                userText.setText("You");
                linearLayout.setGravity(Gravity.END);
                linearLayout.setBackgroundResource(R.drawable.bubble_in);
            } else {
                params.gravity = Gravity.START;
                userText.setText(UserDetails.chatwithEmail);
                linearLayout.setGravity(Gravity.START);
                linearLayout.setBackgroundResource(R.drawable.bubble_out);
            }
            linearLayout.setLayoutParams(params);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        User curruser=user;//UserDetails.user;
        Bundle bundle = new Bundle();
        bundle.putSerializable("userobj",curruser);
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        this.startActivity(intent);
    //TODO: Send user object back to activity
        //Utils.intentWithClear(ChatActivity.this, MapsActivity.class);
    }

    public void valueEventListener(DatabaseReference dbref, final String checkChild) {
        messageRef = FirebaseDatabase.getInstance().getReference("/messages");

        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    User current = child.getValue(User.class);

                    Log.i("CHECK", current.getEmailID() + ","+checkChild);

                    if (current.getEmailID().equals(checkChild)) {
                        UserDetails.chatwithID = child.getKey();
                        final String type1, type2;
                        type1 = UserDetails.userID + "_" + UserDetails.chatwithID;
                        type2 = UserDetails.chatwithID + "_" + UserDetails.userID;

                        messageRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                DataSnapshot child1 = dataSnapshot.child(type1);
                                DataSnapshot child2 = dataSnapshot.child(type2);
                                if (child1.exists()) {
                                    UserDetails.userType = "type1";
                                    UserDetails.chatRef = FirebaseDatabase.getInstance().getReference("/messages").child(type1);
                                    //chatRef2 = messageRef.child(type1);
                                } else if (child2.exists()) {
                                    UserDetails.userType = "type2";
                                    UserDetails.chatRef = FirebaseDatabase.getInstance().getReference("/messages").child(type2);

                                } else {
                                    UserDetails.userType = "type1";
                                    messageRef.child(type1).setValue("none");
                                    UserDetails.chatRef = FirebaseDatabase.getInstance().getReference("/messages").child(type1);
                                    //chatRef2 = messageRef.child(type2);
                                }
                                if (UserDetails.chatRef != null) {
                                    FirebaseRecyclerAdapter<ChatModel, ChatViewHolder> firebaseRecyclerAdapter =
                                            new FirebaseRecyclerAdapter<ChatModel, ChatViewHolder>(
                                                    ChatModel.class,
                                                    R.layout.chat_row,
                                                    ChatViewHolder.class,
                                                    UserDetails.chatRef) {
                                                @Override
                                                protected void populateViewHolder(ChatViewHolder viewHolder, ChatModel model, int position) {
//                                                    final String chatKey = getRef(position).getKey();
                                                    viewHolder.setChatMessage(model.getMessage());
                                                    type = Objects.equals(model.getUser(), UserDetails.userID);
                                                    viewHolder.setUserText(model.getUser(), type);

                                                }
                                            };
                                    chatRecView.setAdapter(firebaseRecyclerAdapter);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                                    Utils.intentWithClear(ChatActivity.this, ChatActivity.class);
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
