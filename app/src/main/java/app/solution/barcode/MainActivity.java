package app.solution.barcode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.vision.CameraSource;

import java.lang.reflect.Field;

import app.solution.barcode.utils.PrefRewardedAd;
import app.solution.barcode.utils.RuntimePermissionClass;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, RewardedVideoAdListener {

    private static final String TAG = "Rewarded";
    Vibrator vibrator;
    private Fragment fragment;
    DrawerLayout drawer;
    String title;
    private ImageView imgSave, imgReset, imgSearch, imgFrame;
    private Button btnStartScan;
    private AdView mAdView;
    private RewardedVideoAd mAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        initUI();

        loadBannerAd();

        initRewardedAd();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        btnStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (new RuntimePermissionClass(getApplicationContext()).checkPermission()){
                    final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
                    btnStartScan.startAnimation(myAnim);
                    startActivity(new Intent(MainActivity.this, ScannerActivity.class));
                }else {
                    requestPermission();
                }
            }
        });
    }

    private void initRewardedAd() {

        MobileAds.initialize(this,"ca-app-pub-9590663615277504~9755335900");
        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);

        mAd.loadAd("ca-app-pub-9590663615277504/1519942641",new AdRequest.Builder().build());

    }


    private void loadBannerAd() {
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);
    }


    private void initUI() {

        btnStartScan = findViewById(R.id.btnStartScan);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_history) {
            drawer.closeDrawers();
            fragment = new HistoryFragment();
            openFragment(fragment);
            // Handle the camera action
        } else if (id == R.id.nav_home) {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            }

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Hey check out my app at: https://play.google.com/store/apps/details?id=");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (id == R.id.nav_send) {
            launchMarket();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private static Camera getCamera(@NonNull CameraSource cameraSource) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        return camera;
                    }
                    return null;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
        Log.d("LifeCycle", "OnDestroy");
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
        Log.d("LifeCycle", "onPause");
       // cameraSource.stop();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        if (mAdView != null) {
            mAdView.resume();
        }
        super.onResume();
        Log.d("LifeCycle", "onResume");
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

        }
    }


    public void openFragment(final Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.frameLayout, fragment);
        transaction.addToBackStack("tag");
        transaction.commit();

    }

    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
        }
    }


    public void requestPermission(){

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},RuntimePermissionClass.CAMERA_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case RuntimePermissionClass.CAMERA_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
                    btnStartScan.startAnimation(myAnim);
                    startActivity(new Intent(MainActivity.this, ScannerActivity.class));
                }else {
                    Toast.makeText(getApplicationContext(),"Permission denied",Toast.LENGTH_LONG).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                            showMessageOkCancel("You need to allow access permission", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestPermission();
                                }
                            });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOkCancel(String s, DialogInterface.OnClickListener onClickListener) {

        new AlertDialog.Builder(MainActivity.this)
                .setMessage(s)
                .setPositiveButton("Ok",onClickListener)
                .setNegativeButton("Cancel",null)
                .create()
                .show();

    }

    @Override
    public void onRewardedVideoAdLoaded() {
        Log.d(TAG,"Rewarded ad loaded");
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Log.d(TAG,"Rewarded ad opened");
        new PrefRewardedAd(this).setCount(1);
    }

    @Override
    public void onRewardedVideoStarted() {
        Log.d(TAG,"Rewarded ad started");
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Log.d(TAG,"Rewarded ad closed");
        finish();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        Log.d(TAG,"Rewarded ad "+rewardItem.getAmount()+"\n"+rewardItem.getType());
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Log.d(TAG,"Rewarded ad left application");
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        Log.d(TAG,"Rewarded ad failed to load");
    }


    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            if (mAd.isLoaded()){
                mAd.show();
            }
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 3000);
    }
}
