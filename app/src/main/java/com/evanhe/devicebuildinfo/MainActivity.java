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
import android.webkit.WebView;
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
    private String imei = "";
    private boolean googlePlayServicesAvailable, isGPSEnabled;
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

        browser = (WebView) findViewById(R.id.webview);
        browser.getSettings().setJavaScriptEnabled(true);
        htmlText = "<b>Device ID:</b> <i>" + this.android_id + "</i><span id='imei'><br><br><b>IMEI:</b> <i>" + this.imei + "</i></span><br><br><b>Device Name:</b> <i>" + device_name + "</i><br><br><b>SDK Version:</b> <i>" + sdk_version + "</i><br><br><b>Release:</b> <i>" + android_OS + "</i><br><br><b>Device:</b> <i>" + android_device + "</i><br><br><b>Model:</b> <i>" + android_model + "</i><br><br><b>Brand:</b> <i>" + android_brand + "</i><br><br><b>Manufacturer:</b> <i>" + manufaturer + "</i><br><br><b>Product:</b> <i>" + android_product + "</i><br><br><b>Network:</b> <i>" + network + "</i><span id='pip'><br><br><b>IP Address:</b> Searching...</span><span id='location'></span><span id='city'></span><span id='gadid'></span>" + "<br><br><b>ABI:</b> <i>" + abi + "</i><br><br><b>Tags:</b> <i>" + tags + "</i><br><br><b>Build ID:</b> <i>" + build_id + "</i><br><br><b>Display ID:</b> <i>" + display_id + "</i><br><br><b>Locale:</b> <i>" + locale + "</i><br><br><b>Google Play Services:</b> <i>" + googlePlayServicesAvailable + "</i><br><br><b>Device DRM ID:</b> <i>" + unique_device_id + "</i>" +
                "<style type=\"text/css\">\n" +
                "#toast {\n" +
                "position: fixed;\n" +
                "display:block;\n" +
                "bottom: 2em;\n" +
                "height: 2em;\n" +
                "width: 10em;\n" +
                "left: calc(50% - 5em);\n" +
                "animation: toast-fade-in 1s 2 alternate;\n" +
                "background-color: black;\n" +
                "border-radius: 2em;\n" +
                "color: white;\n" +
                "text-align: center;\n" +
                "padding: 1em;\n" +
                "line-height: 2em;\n" +
                "opacity: 0;\n" +
                "}\n" +
                "@keyframes toast-fade-in {\n" +
                "from {\n" +
                "opacity: 0;\n" +
                "}\n" +
                "to {\n" +
                "opacity: 1;\n" +
                "}\n" +
                "}\n" +
                "</style>" +
                "" +
                "<script>" +
                "function updateGadid(gid) {\n" +
                    "\tdocument.getElementById('gadid').innerHTML = \"<br><br><b>AD id:</b> <i>\" + gid + \"</i>\"" +
                "}\n" +
                "function updateLocation(lat, long) {\n" +
                    "\tdocument.getElementById('location').innerHTML = \"<br><br><b>Location:</b> <i>\" + lat + \", \" + long + \"</i>\"" +
                "}\n" +
                "function updateCity(city) {\n" +
                    "\tdocument.getElementById('city').innerHTML = \"<br><br><b>City:</b> <i>\" + city + \"</i>\"" +
                "}" +
                "function updateIP(ip) {\n" +
                    "\tdocument.getElementById('pip').innerHTML = \"<br><br><b>IP Address:</b> <i>\" + ip + \"</i>\"" +
                "}" +
                "function updateIMEI(imei) {\n" +
                    "\tdocument.getElementById('imei').innerHTML = \"<br><br><b>IMEI:</b> <i>\" + imei + \"</i>\"" +
                "}" +
                "function copyTextToClipboard(text) {\n" +
                "  var textArea = document.createElement(\"textarea\");\n" +
                "  textArea.style.position = 'fixed';\n" +
                "  textArea.style.top = 0;\n" +
                "  textArea.style.left = 0;\n" +
                "  textArea.style.width = '2em';\n" +
                "  textArea.style.height = '2em';\n" +
                "  textArea.style.padding = 0;\n" +
                "  textArea.style.border = 'none';\n" +
                "  textArea.style.outline = 'none';\n" +
                "  textArea.style.boxShadow = 'none';\n" +
                "  textArea.style.background = 'transparent';\n" +
                "  textArea.value = text;\n" +
                "  document.body.appendChild(textArea);\n" +
                "  textArea.focus();\n" +
                "  textArea.select();\n" +
                "  try {\n" +
                "    var successful = document.execCommand('copy');\n" +
                "    showToast(\"Copied!!\");\n" +
                "  } catch (err) {\n" +
                "    showToast(\"Failed to copy!!\")\n" +
                "  }\n" +
                "  document.body.removeChild(textArea);\n" +
                "}\n" +
                "function showToast(text) {\n" +
                "  span = document.createElement('span');\n" +
                "  text = document.createTextNode(text)\n" +
                "  span.append(text)\n" +
                "  span.setAttribute(\"id\", \"toast\")\n" +
                "  document.body.appendChild(span)\n" +
                "  setTimeout(function(){\n" +
                "    document.getElementById('toast').remove()\n" +
                "  },2000);\n" +
                "}; \n" +
                "function copyText(e) {\n" +
                "  console.log(e.target.tagName)\n" +
                "  console.log(e.target.tagName == \"i\")\n" +
                "  if (e.target.tagName == \"I\") {\n" +
                "    elementData = e.target.innerText;\n" +
                "    copyTextToClipboard(elementData);\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "document.addEventListener(\"click\", copyText);" +
                "</script>";
        browser.loadData(htmlText, "text/html; charset=UTF-8", null);
        new GetPublicIP().execute();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener(getApplicationContext());

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_CHECK_SETTINGS);
            }
            else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint({"MissingPermission", "WrongConstant"})
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_CODE_CHECK_SETTINGS) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to get your Location", Toast.LENGTH_SHORT).show();
                }
                return;
        }

        if (requestCode == REQUEST_CODE_READ_PHONE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.imei = ((TelephonyManager) getApplicationContext().getSystemService("phone")).getDeviceId();
                    MainActivity.browser.loadUrl("javascript:(updateIMEI(\"" + this.imei + "\"))");
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

//        System.out.println("*************************** " + latitude + ", " + longitude);

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
        String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
                + cityName;
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