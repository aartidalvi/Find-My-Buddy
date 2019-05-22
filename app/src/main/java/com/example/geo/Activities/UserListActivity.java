package com.example.geo.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.example.geo.Models.ChatModel;
import com.example.geo.Models.User;
import com.example.geo.Models.UserDetails;
import com.example.geo.R;
import com.example.geo.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserListActivity extends MenuActivity {
    ListView usersList;
    TextView noUsersText;
    ArrayList<String> chatListNicknames;
    ArrayList<User> chatListUsers;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    ProgressDialog pd;
    User user;
    List<String> message_history = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        Button signout = (Button) findViewById(R.id.sign_out);
        user = (User) getIntent().getSerializableExtra("userobj");
        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.show();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    pd.dismiss();
                    Utils.intentWithClear(UserListActivity.this, SignInActivity.class);
                }/* else {
                    UserDetails.userID = UserDetails.userID;
                    UserDetails.userEmail = user.getEmail();
                }*/
            }
        };

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("/Users");
        usersList = (ListView) findViewById(R.id.usersList);
        noUsersText = (TextView) findViewById(R.id.noUsersText);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                chatListNicknames = new ArrayList<>();
                chatListUsers = new ArrayList<>();

//                Iterable<DataSnapshot> imagesDir = dataSnapshot.getChildren();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    User current = child.getValue(User.class);
                    if (!current.getEmailID().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                    //if (!Objects.equals(child.getValue(), FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        chatListNicknames.add(current.getNickName());
                        chatListUsers.add(current);

                    }
                }
                getMessageHistories();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String checkChild = chatListNicknames.get(position);
                UserDetails.chatwithEmail = chatListUsers.get(position).getEmailID();
                UserDetails.chatwithID = chatListUsers.get(position).getNickName();
                UserDetails.userEmail = user.getEmailID();
                UserDetails.userID = user.getNickName();
                UserDetails.user =user;

                Intent intent = new Intent(UserListActivity.this, ChatActivity.class);
                intent.putExtra("user", user.getEmailID());
                intent.putExtra("buddy", chatListUsers.get(position).getEmailID());
                intent.putExtra("caller","UserListActivity");
                Bundle bundle = new Bundle();
                bundle.putSerializable("userobj",user);
                intent.putExtras(bundle);

                startActivity(intent);
//                startActivity(new Intent(UserListActivity.this, ChatActivity.class));
            }
        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserDetails.chatwithEmail = "";
                UserDetails.userType = "";
                UserDetails.userEmail = "";
                UserDetails.chatwithID = "";
                UserDetails.userID = "";
                UserDetails.chatRef = null;
                FirebaseAuth.getInstance().signOut();
            }
        });
    }
//    @Override
//    public boolean onSupportNavigateUp() {
//        finish();
//        return true;
//    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    private boolean notinhistory() {
        return false;
    }
    public void getMessageHistories() {
        final DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("/messages");
                        messageRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                ArrayList<String> chatListNicknamesnew = new ArrayList<>();
                                ArrayList<User> chatListUsersnew = new ArrayList<>();

                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    message_history.add(child.getKey());
                                }
                                for(int i=0;i<chatListNicknames.size();i++) {
                                    String type1 = chatListNicknames.get(i) + "_" + user.getNickName();
                                    String type2 = user.getNickName() + "_" + chatListNicknames.get(i);
                                    if(message_history.contains(type1) || message_history.contains(type2))
                                    {
                                        chatListNicknamesnew.add(chatListNicknames.get(i));
                                        chatListUsersnew.add(chatListUsers.get(i));
                                    }
                                }
                                chatListNicknames = chatListNicknamesnew;
                                chatListUsers = chatListUsersnew;

                                if (chatListNicknames.isEmpty()) {
                                    noUsersText.setVisibility(View.VISIBLE);
                                    usersList.setVisibility(View.GONE);
                                } else {
                                    noUsersText.setVisibility(View.GONE);
                                    usersList.setVisibility(View.VISIBLE);
                                    usersList.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.custom_list, chatListNicknames));
                                }
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
    }
}