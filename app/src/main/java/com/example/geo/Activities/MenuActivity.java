package com.example.geo.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.example.geo.R;
import com.example.geo.Models.User;

public class MenuActivity extends AppCompatActivity {

    User currentUser;
    String caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = (User) getIntent().getSerializableExtra("userobj");
        caller = getIntent().getStringExtra("caller");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        //this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        //User curruser=UserDetails.user;
        Bundle bundle = new Bundle();
        bundle.putSerializable("userobj",currentUser);

        switch (item.getItemId()){
            case R.id.menu_updateprofile:
                Intent intent = new Intent(this, Updateprofile.class);
                intent.putExtras(bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;

            case R.id.menu_users:
                Intent intent1 = new Intent(this, UserListActivity.class);
                intent1.putExtras(bundle);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
                return true;

            case R.id.menu_maps:
                Intent intent3 = new Intent(this, MapsActivity.class);
                intent3.putExtras(bundle);
                intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent3);

                return true;

            case R.id.menu_signout:
                Intent intent2 = new Intent(this, SignInActivity.class);
                intent2.putExtras(bundle);
                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent2);

                return true;
            default:
                /*if("UserListActivity".equals(caller)) {
                    intent = new Intent(this, UserListActivity.class);
                    intent.putExtras(bundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                else if("Maps".equals(caller)) {
                    intent = new Intent(this, MapsActivity.class);
                    intent.putExtras(bundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                else*/
                    return super.onOptionsItemSelected(item);
        }

        return true;
    }

}
