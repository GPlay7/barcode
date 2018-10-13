package app.solution.barcode;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import app.solution.barcode.database.DBHandler;
import app.solution.barcode.database.DbModelClass;

public class BarcodeResultActivity extends AppCompatActivity {

    private Intent intent;
    private String result;

    private ImageView imgSave, imgShare;
    private Button btnSearch;
    private TextView txtScanResult;

    private Toolbar toolbar;
    private String title;

    private AdView mAdView;

    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_result);
        toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.tool_bar_color), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Implemented by activity
            }
        });

        initUI();

        loadBannerAd();

        loadInterStitialAd();

        intent = getIntent();
        result = intent.getStringExtra("result");

        txtScanResult.setText(result);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Animation myAnimSearch = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
                btnSearch.startAnimation(myAnimSearch);

                if (isOnline(getApplicationContext())){
                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY, result);
                    startActivity(intent);
                }else {
                    Toast.makeText(getApplicationContext(), "Please check your internet connection!",Toast.LENGTH_LONG).show();
                }
            }
        });

        imgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        result);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Animation myAnimSave = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
                imgSave.startAnimation(myAnimSave);
                showTitleDialog();
            }
        });

    }

    private void loadInterStitialAd() {

        mInterstitialAd = new InterstitialAd(this);
        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));

        AdRequest adRequests = new AdRequest.Builder()
                .build();

        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequests);

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }
        });

    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private void loadBannerAd() {
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);
    }

    private void showTitleDialog() {
        final String currentDateandTime = new SimpleDateFormat("EEE, d MMM yyyy HH:mma").format(new Date());
        Log.d("CurrentTime", currentDateandTime);
        final Dialog dialogConfirm = new Dialog(BarcodeResultActivity.this,
                R.style.MyDialog);
        dialogConfirm.setContentView(R.layout.custom_dialog);
        dialogConfirm.setCancelable(true);
        final Button btnOk = (Button) dialogConfirm.findViewById(R.id.btnOk);
        final Button btnCancel = (Button) dialogConfirm.findViewById(R.id.btnCancel);
        final EditText etTitle = (EditText) dialogConfirm.findViewById(R.id.etTitle);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogConfirm.dismiss();
                title = etTitle.getText().toString();
                if (!title.equals("")) {
                    DBHandler db = new DBHandler(BarcodeResultActivity.this);
                    DbModelClass dbModelClass = new DbModelClass();
                    dbModelClass.setTitle(title);
                    dbModelClass.setScanQuery(result);
                    dbModelClass.setDateTime(currentDateandTime);
                    db.addItem(dbModelClass);
                    Toast.makeText(BarcodeResultActivity.this, "Data Saved Successfully!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(BarcodeResultActivity.this, "Please give a title", Toast.LENGTH_LONG).show();
                }
                title = "";
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogConfirm.dismiss();
            }
        });

        dialogConfirm.show();
    }

    private void initUI() {
        txtScanResult = findViewById(R.id.txtScanResult);
        imgSave = findViewById(R.id.imgSave);
        imgShare = findViewById(R.id.imgShare);
        btnSearch = findViewById(R.id.btnSearch);

    }


    public static boolean isOnline(final Context ctx) {
        try {
            final ConnectivityManager cm = (ConnectivityManager) ctx
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null) {
                return ni.isConnectedOrConnecting();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
        Log.d("LifeCycle", "OnDestroy");
//        cameraSource.release();
//        barcodeDetector.release();
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
}
