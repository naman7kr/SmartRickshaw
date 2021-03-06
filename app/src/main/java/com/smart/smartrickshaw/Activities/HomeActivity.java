package com.smart.smartrickshaw.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.smart.smartrickshaw.Fragment.FindRidesFragment;
import com.smart.smartrickshaw.Fragment.ProfileFragment;
import com.smart.smartrickshaw.Fragment.WalletFragment;
import com.smart.smartrickshaw.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    //vars
    private FragmentTransaction ft;
    private GoogleSignInAccount account;

    //widgets
    private DrawerLayout drawer;
    private FrameLayout fl;
    private CircleImageView nav_img;
    private TextView nav_name;
    private NavigationView navigationView;
    private Toolbar toolbar;

    //constants
    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();

        setSupportActionBar(toolbar);

        setNavDrawer();
    }
    private void init(){
        toolbar =  findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView =  findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        nav_img = header.findViewById(R.id.nav_image);
        nav_name = header.findViewById(R.id.nav_user_name);
        account = GoogleSignIn.getLastSignedInAccount(this);
        fl = findViewById(R.id.home_container);
        drawer =  findViewById(R.id.drawer_layout);
    }
    private void setNavDrawer(){
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.home_container,new FindRidesFragment()).commit();
        navigationView.setCheckedItem(R.id.nav_find_rides);

        Picasso.with(this)
                .load(account.getPhotoUrl())
                .placeholder(android.R.drawable.sym_def_app_icon)
                .error(android.R.drawable.sym_def_app_icon)
                .into(nav_img);
        nav_name.setText(account.getGivenName());
        nav_img.setOnClickListener(this);
        nav_name.setOnClickListener(this);
    }
    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.isChecked()) item.setChecked(false);
        else item.setChecked(true);

        int id = item.getItemId();
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.home_container);
        switch (id){
            case R.id.nav_find_rides:
                if(!(f instanceof FindRidesFragment)) {
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.home_container, new FindRidesFragment()).commit();
                }
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_wallet:
                if(!(f instanceof WalletFragment)) {
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.home_container, new WalletFragment()).commit();
                }
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this,SettingsActivity.class));
                item.setChecked(false);
                break;
            case R.id.nav_feedback:
                item.setChecked(false);
                break;
        }
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.nav_image||v.getId()==R.id.nav_user_name){
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.home_container);
            if(!(f instanceof ProfileFragment)) {
                ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.home_container, new ProfileFragment()).commit();
            }
            Menu menu = navigationView.getMenu();
            for(int i=0;i<menu.size();i++){
                menu.getItem(i).setChecked(false);
            }
            drawer.closeDrawer(GravityCompat.START);
        }
    }
}
