package com.evanhe.devicebuildinfo;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaDrm;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private String screen_text;
    private String device_name;
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
    private String android_id;
    private String imei = "Not Supported";
    private boolean googlePlayServicesAvailable, isNetworkEnabled;
    private int sdk_version;
    private TextView tx, tx2;
    private LocationManager locationManager;
    private LocationListener locationListener;
    String gid = "";
    String htmlText;
    public static WebView browser;
    public static int REQUEST_CODE_CHECK_SETTINGS = 101;
    public static int REQUEST_CODE_READ_PHONE = 100;

    @SuppressLint("WrongConstant")
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
        this.device_name = Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME);
        this.locale = Locale.getDefault().getDisplayCountry();
        this.googlePlayServicesAvailable = isGooglePlayServicesAvailable(MainActivity.this);
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        this.network = telephonyManager.getNetworkOperatorName();
        this.android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), "android_id");

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_READ_PHONE);
            }
            else {
                this.imei = ((TelephonyManager) getApplicationContext().getSystemService("phone")).getDeviceId();
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener(getApplicationContext());
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_CHECK_SETTINGS);
            }
            else {
                if (isNetworkEnabled)
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                else
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        } catch (Exception e){
            e.printStackTrace();
        }


        browser = (WebView) findViewById(R.id.webview);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.setWebChromeClient(new WebChromeClient());
        htmlText = "<!DOCTYPE html><html><head><style type=\"text/css\">\n" +
                "#toast {" +
                "position: fixed;" +
                "display: block;" +
                "bottom: 2em;" +
                "height: 2em;" +
                "width: 10em;" +
                "left: calc(50% - 5em);" +
                "animation: toast-fade-in 1s 2 alternate;" +
                "background-color: black;" +
                "border-radius: 2em;" +
                "color: white;" +
                "text-align: center;" +
                "padding: 1em;" +
                "line-height: 2em;" +
                "opacity: 0;" +
                "}" +
                "@keyframes toast-fade-in {" +
                "from {" +
                "opacity: 0;" +
                "}" +
                "to {" +
                "opacity: 1;" +
                "}" +
                "}" +
                "</style></head><body><b>Device ID:</b> <i>" + this.android_id + "</i><span id='imei'><br><br><b>IMEI:</b> <i>" + this.imei + "</i></span><br><br><b>Device Name:</b> <i>" + device_name + "</i><br><br><b>SDK Version:</b> <i>" + sdk_version + "</i><br><br><b>Release:</b> <i>" + android_OS + "</i><br><br><b>Device:</b> <i>" + android_device + "</i><br><br><b>Model:</b> <i>" + android_model + "</i><br><br><b>Brand:</b> <i>" + android_brand + "</i><br><br><b>Manufacturer:</b> <i>" + manufaturer + "</i><br><br><b>Product:</b> <i>" + android_product + "</i><br><br><b>Network:</b> <i>" + network + "</i><span id='pip'><br><br><b>IP Address:</b> Searching...</span><span id='location'><br><br><b>Location:</b> Searching...</span><span id='city'><br><br><b>City:</b> Searching...</span><span id='gadid'><br><br><b>AD id:</b> Searching...</span>" + "<br><br><b>ABI:</b> <i>" + abi + "</i><br><br><b>Tags:</b> <i>" + tags + "</i><br><br><b>Build ID:</b> <i>" + build_id + "</i><br><br><b>Display ID:</b> <i>" + display_id + "</i><br><br><b>Locale:</b> <i>" + locale + "</i><br><br><b>Google Play Services:</b> <i>" + googlePlayServicesAvailable + "</i><br><br><b>Device DRM ID:</b> <i>" + unique_device_id + "</i>" +
                "<script type=\"text/javascript\">" +
                "function updateGadid(gid) {" +
                "document.getElementById('gadid').innerHTML = \"<br><br><b>AD id:</b> <i>\" + gid + \"</i>\";" +
                "}" +
                "function updateLocation(lat, long) {" +
                "document.getElementById('location').innerHTML = \"<br><br><b>Location:</b> <i>\" + lat + \", \" + long + \"</i>\";" +
                "}" +
                "function updateCity(city) {" +
                "document.getElementById('city').innerHTML = \"<br><br><b>City:</b> <i>\" + city + \"</i>\";" +
                "}" +
                "function updateIP(ip) {" +
                "document.getElementById('pip').innerHTML = \"<br><br><b>IP Address:</b> <i>\" + ip + \"</i>\";" +
                "}" +
                "function updateIMEI(imei) {" +
                "document.getElementById('imei').innerHTML = \"<br><br><b>IMEI:</b> <i>\" + imei + \"</i>\";" +
                "}" +
                "</script>" +
                "<script type=\"text/javascript\">" +
                "function copyTextToClipboard(text) {" +
                "  var textArea = document.createElement(\"textarea\");" +
                "  textArea.style.position = 'fixed';" +
                "  textArea.style.top = 0;" +
                "  textArea.style.left = 0;" +
                "  textArea.style.width = '2em';" +
                "  textArea.style.height = '2em';" +
                "  textArea.style.padding = 0;" +
                "  textArea.style.border = 'none';" +
                "  textArea.style.outline = 'none';" +
                "  textArea.style.boxShadow = 'none';" +
                "  textArea.style.background = 'transparent';" +
                "  textArea.value = text;" +
                "  document.body.appendChild(textArea);" +
                "  textArea.focus();" +
                "  textArea.select();" +
                "  try {" +
                "    var successful = document.execCommand('copy');" +
                "    showToast(\"Copied!!\");" +
                "  } catch (err) {" +
                "    showToast(\"Failed to copy!!\");" +
                "  }" +
                "  document.body.removeChild(textArea);" +
                "}" +
                "function showToast(text) {" +
                "  span = document.createElement('span');" +
                "  text = document.createTextNode(text);" +
                "  span.append(text);" +
                "  span.setAttribute(\"id\", \"toast\");" +
                "  document.body.appendChild(span);" +
                "  setTimeout(function(){" +
                "    document.getElementById('toast').remove();" +
                "  },2000);" +
                "}; " +
                "function copyText(e) {" +
                "  if (e.target.tagName == \"I\") {" +
                "    elementData = e.target.innerText;" +
                "    copyTextToClipboard(elementData);" +
                "  }" +
                "}" +
                "document.addEventListener(\"click\", copyText);" +
                "</script>" +
                "</body>" +
                "</html>";
        browser.loadDataWithBaseURL("file:///android_asset/www/", htmlText, "text/html", "UTF-8", null);
        new GetPublicIP().execute();

    }

    @SuppressLint({"MissingPermission", "WrongConstant"})
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_CODE_CHECK_SETTINGS) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isNetworkEnabled)
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    else
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to get your Location", Toast.LENGTH_SHORT).show();
                }
                return;
        }

        if (requestCode == REQUEST_CODE_READ_PHONE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {

                        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_CHECK_SETTINGS);
                        }

                        this.imei = ((TelephonyManager) getApplicationContext().getSystemService("phone")).getDeviceId();
                        MainActivity.browser.loadUrl("javascript:(updateIMEI(\"" + this.imei + "\"))");
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to read your Phone", Toast.LENGTH_SHORT).show();
                }
                return;
        }
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
                    browser.loadUrl("javascript:(updateGadid(\"" + gid + "\"))");
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

/*---------- Listener class to get coordinates ------------- */
class MyLocationListener implements LocationListener {

    Context context;
    MyLocationListener(Context context) { this.context = context; }
    @Override
    public void onLocationChanged(Location loc) {
        String latitude = loc.getLatitude() + "";
        String longitude = loc.getLongitude() + "";

        MainActivity.browser.loadUrl("javascript:(updateLocation(\"" + latitude + "\", \"" + longitude + "\"))");

        /*------- To get city name from coordinates -------- */
        String cityName = null;
        Geocoder gcd = new Geocoder(this.context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
                MainActivity.browser.loadUrl("javascript:(updateCity(\"" + cityName + "\"))");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}

class GetPublicIP extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... strings) {
        String publicIP = "";
        try  {
            java.util.Scanner s = new java.util.Scanner(
                    new java.net.URL(
                            "https://api.ipify.org")
                            .openStream(), "UTF-8")
                    .useDelimiter("\\A");
            publicIP = s.next();
            System.out.println("My current IP address is " + publicIP);
        } catch (java.io.IOException e) {
            MainActivity.browser.loadUrl("javascript:(updateIP(\"No Internet\"))");
            e.printStackTrace();
        }

        return publicIP;
    }

    @Override
    protected void onPostExecute(String publicIp) {
        super.onPostExecute(publicIp);
        MainActivity.browser.loadUrl("javascript:(updateIP(\"" + publicIp + "\"))");
    }
}