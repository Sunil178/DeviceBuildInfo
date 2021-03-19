package com.evanhe.devicebuildinfo;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import android.database.Cursor;
import android.media.MediaDrm;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private String screen_text;
    private String android_OS;
    private String android_device;
    private String android_model;
    private String android_brand;
    private String android_product;
    private String unique_device_id;
    private String build_id;
    private String display_id;
    private String gid_text;
    private String locale;
    private String manufaturer;
    private String network;
    private String abi;
    private String tags;
    private boolean googlePlayServicesAvailable;
    private int sdk_version;
    private TextView tx, tx2;
    String gid = "";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.sdk_version = Build.VERSION.SDK_INT;
        this.android_OS = Build.VERSION.RELEASE;
        this.android_device = Build.DEVICE;
        this.android_model = Build.MODEL;
        this.android_brand = Build.BRAND;
        this.android_product = Build.PRODUCT;
        this.unique_device_id = getUniqueID();
        this.build_id = Build.ID;
        this.display_id = Build.DISPLAY;
        this.manufaturer = Build.MANUFACTURER;
        this.abi = Build.CPU_ABI;
        this.tags = Build.TAGS;
        this.locale = Locale.getDefault().getDisplayCountry();
        this.googlePlayServicesAvailable = isGooglePlayServicesAvailable(MainActivity.this);
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        this.network = telephonyManager.getNetworkOperatorName();

        screen_text = "<b>SDK Version:</b> " + sdk_version + "<br><b>Release:</b> " + android_OS + "<br><b>Device:</b> " + android_device + "<br><b>Model:</b> " + android_model + "<br><b>Brand:</b> " + android_brand + "<br><b>Manufacturer:</b> " + manufaturer + "<br><b>Product:</b> " + android_product + "<br><b>ABI:</b> " + abi + "<br><b>Tags:</b> " +tags + "<br><b>Build ID:</b> " + build_id + "<br><b>Display ID:</b> " + display_id + "<br><b>Locale:</b> " + locale + "<br><b>Network:</b> " + network + "<br><b>Google Play Services:</b> " + googlePlayServicesAvailable + "<br><b>Device DRM ID:</b> " + unique_device_id;
        tx = findViewById(R.id.hello);
        tx.setText(Html.fromHtml(screen_text));

    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                if (gid != "") {
                    gid_text = "<b>AD id:</b> " + gid;
                    tx2 = findViewById(R.id.ad_id);
                    tx2.setText(Html.fromHtml(gid_text));
                    handler.removeCallbacksAndMessages(null);
                }
                else
                    handler.postDelayed(this, 2000);
            }
        }, 2000);


        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String gaid = AdvertisingIdClient.getAdvertisingIdInfo(
                            MainActivity.this.getApplicationContext()).getId();
                    if (gaid != null) {
                        gid = gaid;
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Nullable
    public String getUniqueID() {
        try {
            return Arrays.toString(new MediaDrm(new UUID(-1301668207276963122L, -6645017420763422227L)).getPropertyByteArray("deviceUniqueId")).replaceAll("\\[|]|, |", "");
//            return Arrays.toString(new MediaDrm(new UUID(-1301668207276963122L, -6645017420763422227L)).getPropertyByteArray("deviceUniqueId"));
        } catch (Exception unused) {
            return null;
        }
    }
}