package com.dev.salim.ChatApp.Ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.dev.salim.ChatApp.R;
import com.dev.salim.ChatApp.Ui.fragments.ChatsFragment;
import com.dev.salim.ChatApp.Ui.fragments.UsersFragment;
import com.dev.salim.ChatApp.Ui.modules.ChatMessages;
import com.dev.salim.ChatApp.Ui.modules.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView navView;

    String status;
    int fragmentPosition = 0;
    private int[] tabIcons = {R.drawable.ic_chats, R.drawable.ic_people};

    CircleImageView mProfile;
    TextView tvName;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        mAuth = FirebaseAuth.getInstance();

        mProfile = findViewById(R.id.ivProfile);
        tvName = findViewById(R.id.tvName);
        navView = findViewById(R.id.navView);

        Intent i = getIntent();
        if (i.getExtras() != null) {
            fragmentPosition = i.getIntExtra("fragmentPosition", 0);
            status = i.getStringExtra("status");
            Log.e("fragmentPosition", "  " + fragmentPosition);
        } else {
            fragmentPosition = 0;
            status = "online";
        }

        switch (fragmentPosition) {
            case 0:
                transIntent(new ChatsFragment());
                openFragmentByPosition();
                break;

            case 1:
                transIntent(new UsersFragment());
                openFragmentByPosition();
                break;
        }

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                tvName.setText(user.getName());
                if (user.getImageUrl().equals("default")) {
                    mProfile.setImageResource(R.drawable.ic_image);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageUrl()).into(mProfile);
                }

                Log.e("MainActivity", "onDataChange: User >> " + snapshot.getValue().toString());
                if (user.getStatus().equals("offline")) {
                    status = "offline";
                } else {
                    status = "online";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unread = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    ChatMessages messages = dataSnapshot.getValue(ChatMessages.class);
                    if (messages.getReceiver() != null)
                        if (messages.getReceiver().equals(firebaseUser.getUid())) {
                            unread++;
                        }
                }
                if (unread == 0) {
                    navView.getMenu().getItem(0).getTitle();
                } else {
                    navView.getMenu().getItem(0).setTitle("(" + unread + ") Chats");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        init();

    }

    private void init() {
        navView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.chats:
                    fragmentPosition = 0;
                    transIntent(new ChatsFragment());
                    break;
                case R.id.users:
                    fragmentPosition = 1;
                    transIntent(new UsersFragment());
                    break;
            }
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuLogout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
            case R.id.menuSettings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (fragmentPosition == 0) {
            super.onBackPressed();
        } else {
            fragmentPosition = 0;
            openFragmentByPosition();
        }
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status(status);
    }

    @Override
    protected void onPause() {
        super.onPause();
        status(status);
    }


    private void openFragmentByPosition() {
        switch (fragmentPosition) {
            case 0:
                navView.setSelectedItemId(R.id.chats);
                break;
            case 1:
                navView.setSelectedItemId(R.id.users);
                break;
        }
    }

    private void transIntent(Fragment fragment) {
        getSupportFragmentManager().popBackStackImmediate();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment).commit();
    }
}
