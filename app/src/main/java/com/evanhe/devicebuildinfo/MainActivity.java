package com.evanhe.devicebuildinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Context;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity {
    private String device_name, android_OS, android_device, android_model, android_brand, android_product, unique_device_id, build_id, display_id, locale, manufacturer, network, abi, tags, android_id, address, city, htmlText;
    private String imei = "Not Supported";
    public static String proxy_string, device_details_string = "", network_location_string = "", location_string = "", location_latitude_string = "", location_longitutde_string = "", ip_string = "", ipv6_string = "", ip_city = "", local_ip = "";
    private boolean googlePlayServicesAvailable;
    private int sdk_version;
    String gid = "";
    public static WebView browser;
    public static int REQUEST_CODE_CHECK_SETTINGS = 101;
    public static int REQUEST_CODE_READ_PHONE = 100;
    FusedLocationProviderClient mFusedLocationClient;
    Geocoder geocoder;
    List<Address> addresses;
    public static EditText proxy;
    public static Button set_proxy;
    public static Handler handler;
    public static Runnable runnable;
    public static boolean server_status;
    public static boolean location_status;
    public static boolean network_location_status;
    public static boolean ip_status;
    public static boolean ipv6_status;

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.location_status = false;
        MainActivity.network_location_status = false;
        MainActivity.ip_status = false;
        MainActivity.ipv6_status = false;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        this.sdk_version = Build.VERSION.SDK_INT;
        this.android_OS = Build.VERSION.RELEASE;
        this.android_device = Build.DEVICE;
        this.android_model = Build.MODEL;
        this.android_brand = Build.BRAND;
        this.android_product = Build.PRODUCT;
        this.unique_device_id = getUniqueID();
        this.build_id = Build.ID;
        this.display_id = Build.DISPLAY;
        this.manufacturer = Build.MANUFACTURER;
        this.abi = Build.CPU_ABI;
        this.tags = Build.TAGS;
        this.device_name = Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME);
        this.locale = Locale.getDefault().getDisplayCountry();
        this.googlePlayServicesAvailable = isGooglePlayServicesAvailable(MainActivity.this);
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        this.network = telephonyManager.getNetworkOperatorName();
        this.android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), "android_id");
        this.local_ip = displayInterfaceInformation();

        proxy = findViewById(R.id.proxy_string);
        proxy_string = Settings.Global.getString(getContentResolver(), "http_proxy");
        proxy.setText(proxy_string);
        set_proxy = findViewById(R.id.set_proxy);
        set_proxy.setEnabled(false);

        if (!isMockLocationEnabled()) {
            try {
                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
            }
            catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Please enable developer options", Toast.LENGTH_SHORT).show();
            }
        }

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                if (MainActivity.location_status && MainActivity.ip_status && MainActivity.ipv6_status && MainActivity.network_location_status) {
                    RequestBody requestBody = new FormBody.Builder()
                            .add("android_id", android_id)
                            .add("ipv4", ip_string)
                            .add("ipv6", ipv6_string)
                            .add("latitude", location_latitude_string)
                            .add("longitude", location_longitutde_string)
                            .add("device_details", device_details_string)
                            .build();
                    postAPI("https://citysourcing.in/api/saveData.php", requestBody);

                    RequestBody checkIpRequestBody = new FormBody.Builder()
                            .add("android_id", android_id)
                            .add("ipv4", ip_string)
                            .add("ipv6", ipv6_string)
                            .build();
                    checkIP("https://citysourcing.in/api/checkIpManual.php", checkIpRequestBody);

                    MainActivity.location_status = false;
                    MainActivity.network_location_status = false;
                    MainActivity.ip_status = false;
                    MainActivity.ipv6_status = false;
                    handler.removeCallbacks(runnable);
                }
                else {
                    handler.postDelayed(runnable, 1000);
                }
            }
        };
        handler.post(runnable);

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
        browser.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                try {
                    set_proxy.setEnabled(true);
                    new GetPublicIP().execute();
                    new GetPublicIPv6().execute();

                    if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_CHECK_SETTINGS);
                    }
                    else {
                        getLastLocation();
                        getLocation();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        DisplayMetrics screenDisplay = getScreenDisplay();
        device_details_string = "" +
                "device_id is " + android_id + ",\n" +
                "imei is" + imei + ",\n" +
                "device_name is " + device_name + ",\n" +
                "sdk_version is " + sdk_version + ",\n" +
                "release is " + android_OS + ",\n" +
                "device is " + android_device + ",\n" +
                "model is " + android_model + ",\n" +
                "brand is " + android_brand + ",\n" +
                "manufacturer is " + manufacturer + ",\n" +
                "product is " + android_product + ",\n" +
                "network is " + network + ",\n" +
                "network_location is " + network_location_string + ",\n" +
                "local_ip is " + local_ip + ",\n" +
                "abi is " + abi + ",\n" +
                "tags is " + tags + ",\n" +
                "build_id is " + build_id + ",\n" +
                "display_id is " + display_id + ",\n" +
                "locale is " + locale + ",\n" +
                "screen_dpi is " + screenDisplay.densityDpi + ",\n" +
                "screen_height is " + screenDisplay.heightPixels + ",\n" +
                "screen_width is " + screenDisplay.widthPixels + ",\n" +
                "user_agent is " + System.getProperty( "http.agent" ) + "" +
                "";
        browser.setWebChromeClient(new WebChromeClient());
        browser.getSettings().setJavaScriptEnabled(true);
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
                "</style></head><body><b>Device ID:</b> <i>" + this.android_id + "</i><span id='imei'><br><br><b>IMEI:</b> <i>" + this.imei + "</i></span><br><br><b>Device Name:</b> <i>" + device_name + "</i><br><br><b>SDK Version:</b> <i>" + sdk_version + "</i><br><br><b>Release:</b> <i>" + android_OS + "</i><br><br><b>Device:</b> <i>" + android_device + "</i><br><br><b>Model:</b> <i>" + android_model + "</i><br><br><b>Brand:</b> <i>" + android_brand + "</i><br><br><b>Manufacturer:</b> <i>" + manufacturer + "</i><br><br><b>Product:</b> <i>" + android_product + "</i><br><br><b>Network:</b> <i>" + network + "</i><br><br><b>Local IP:</b> <i>" + local_ip + "</i><span id='pip'><br><br><b>IP Address:</b> Searching...</span><span id='pipv6'><br><br><b>IPv6 Address:</b> Searching...</span><span id='ipregion'><br><br><b>IP Region:</b> Searching...</span><span id='ipcity'><br><br><b>IP City:</b> Searching...</span><span id='network_location'><br><br><b>Network Location:</b> Searching...</span><span id='location'><br><br><b>Location:</b> Searching...</span><span id='address'><br><br><b>Address:</b> Searching...</span><span id='city'><br><br><b>Location City:</b> Searching...</span><span id='gadid'><br><br><b>AD id:</b> Searching...</span>" + "<br><br><b>ABI:</b> <i>" + abi + "</i><br><br><b>Tags:</b> <i>" + tags + "</i><br><br><b>Build ID:</b> <i>" + build_id + "</i><br><br><b>Display ID:</b> <i>" + display_id + "</i><br><br><b>Locale:</b> <i>" + locale + "</i><br><br><b>Google Play Services:</b> <i>" + googlePlayServicesAvailable + "</i><br><br><b>Device DRM ID:</b> <i>" + unique_device_id + "</i>" +
                "<script type=\"text/javascript\">" +
                "function updateGadid(gid) {" +
                "document.getElementById('gadid').innerHTML = \"<br><br><b>AD id:</b> <i>\" + gid + \"</i>\";" +
                "}" +
                "function updateNetworkLocation(lat, long) {" +
                "document.getElementById('network_location').innerHTML = \"<br><br><b>Network Location:</b> <i>\" + lat + \", \" + long + \"</i>\";" +
                "}" +
                "function updateLocation(lat, long) {" +
                "document.getElementById('location').innerHTML = \"<br><br><b>Location:</b> <i>\" + lat + \", \" + long + \"</i>\";" +
                "}" +
                "function updateAddress(address) {" +
                "document.getElementById('address').innerHTML = \"<br><br><b>Address:</b> <i>\" + address + \"</i>\";" +
                "}" +
                "function updateCity(city) {" +
                "document.getElementById('city').innerHTML = \"<br><br><b>Location City:</b> <i>\" + city + \"</i>\";" +
                "}" +
                "function updateIP(ip) {" +
                "document.getElementById('pip').innerHTML = \"<br><br><b>IP Address:</b> <i>\" + ip + \"</i>\";" +
                "}" +
                "function updateIPv6(ip) {" +
                "document.getElementById('pipv6').innerHTML = \"<br><br><b>IPv6 Address:</b> <i>\" + ip + \"</i>\";" +
                "}" +
                "function updateIpRegion(region) {" +
                "document.getElementById('ipregion').innerHTML = \"<br><br><b>IP Region:</b> <i>\" + region + \"</i>\";" +
                "}" +
                "function updateIpCity(city) {" +
                "document.getElementById('ipcity').innerHTML = \"<br><br><b>IP City:</b> <i>\" + city + \"</i>\";" +
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
    }

    @SuppressLint({"MissingPermission", "WrongConstant"})
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_CODE_CHECK_SETTINGS) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                    getLocation();
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

    private boolean isMockLocationEnabled()
    {
        boolean isMockLocation;
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager opsManager = (AppOpsManager) getApplicationContext().getSystemService(Context.APP_OPS_SERVICE);
                isMockLocation = (Objects.requireNonNull(opsManager).checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID)== AppOpsManager.MODE_ALLOWED);
            } else {
                isMockLocation = !android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), "mock_location").equals("0");
            }
        } catch (Exception e) {
            return false;
        }
        return isMockLocation;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setMock(String provider, double latitude, double longitude) {
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.addTestProvider (provider, false, false, false, false, false, true, true, 0, 5);

        Location newLocation = new Location(provider);

        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);
        newLocation.setAltitude(3F);
        newLocation.setTime(System.currentTimeMillis());
        newLocation.setSpeed(0.01F);
        newLocation.setBearing(1F);
        newLocation.setAccuracy(3F);
        newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            newLocation.setBearingAccuracyDegrees(0.1F);
            newLocation.setVerticalAccuracyMeters(0.1F);
            newLocation.setSpeedAccuracyMetersPerSecond(0.01F);
        }
        mLocationManager.setTestProviderEnabled(provider, true);

        mLocationManager.setTestProviderLocation(provider, newLocation);
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
        } catch (Exception unused) {
            return null;
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (isLocationEnabled()) {
            mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    requestNewLocationData();
                    if (location == null) {
                        requestNewLocationData();
                    } else {
                        try {
                            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            address = addresses.get(0).getAddressLine(0);
                            city = addresses.get(0).getLocality();
                            MainActivity.browser.loadUrl("javascript:(updateLocation(\"" + location.getLatitude() + "\", \"" + location.getLongitude() + "\"))");
                            MainActivity.browser.loadUrl("javascript:(updateAddress(\"" + address + "\"))");
                            MainActivity.browser.loadUrl("javascript:(updateCity(\"" + city + "\"))");
                            MainActivity.location_string = location.getLatitude() + "," + location.getLongitude();
                            MainActivity.location_latitude_string = location.getLatitude() + "";
                            MainActivity.location_longitutde_string = location.getLongitude() + "";
                            MainActivity.location_status = true;
                            if (isMockLocationEnabled()) {
                                setMock(LocationManager.GPS_PROVIDER, location.getLatitude(), location.getLongitude());
                                setMock(LocationManager.NETWORK_PROVIDER, location.getLatitude(), location.getLongitude());
                            }
                        } catch (IOException e) {
                            MainActivity.browser.post(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.browser.loadUrl("javascript:(updateLocation(\"No Internet\", \"No Internet\"))");
                                    MainActivity.browser.loadUrl("javascript:(updateAddress(\"No Internet\"))");
                                    MainActivity.browser.loadUrl("javascript:(updateCity(\"No Internet\"))");
                                    MainActivity.handler.removeCallbacks(MainActivity.runnable);
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else {
            Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(100);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            try {
                addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                address = addresses.get(0).getAddressLine(0);
                city = addresses.get(0).getLocality();
                MainActivity.browser.loadUrl("javascript:(updateLocation(\"" + mLastLocation.getLatitude() + "\", \"" + mLastLocation.getLongitude() + "\"))");
                MainActivity.browser.loadUrl("javascript:(updateAddress(\"" + address + "\"))");
                MainActivity.browser.loadUrl("javascript:(updateCity(\"" + city + "\"))");
                MainActivity.location_string = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
                MainActivity.location_latitude_string = mLastLocation.getLatitude() + "";
                MainActivity.location_longitutde_string = mLastLocation.getLongitude() + "";
                MainActivity.location_status = true;
                if (isMockLocationEnabled()) {
                    setMock(LocationManager.GPS_PROVIDER, mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    setMock(LocationManager.NETWORK_PROVIDER, mLastLocation.getLatitude(), mLastLocation.getLongitude());
                }
            } catch (IOException e) {
                MainActivity.browser.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.browser.loadUrl("javascript:(updateLocation(\"No Internet\", \"No Internet\"))");
                        MainActivity.browser.loadUrl("javascript:(updateAddress(\"No Internet\"))");
                        MainActivity.browser.loadUrl("javascript:(updateCity(\"No Internet\"))");
                        MainActivity.handler.removeCallbacks(MainActivity.runnable);
                    }
                });
                e.printStackTrace();
            }
        }
    };

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setProxy(View view) {
        try {
            proxy = findViewById(R.id.proxy_string);
            proxy_string = proxy.getText().toString();
            if (MainActivity.proxy_string == null || proxy_string.trim().equals("")) {
                proxy_string = ":0";
                Settings.Global.putString(
                        getContentResolver(),
                        Settings.Global.HTTP_PROXY,
                        proxy_string
                );
                Toast.makeText(MainActivity.this, "Proxy removed", Toast.LENGTH_LONG).show();
            } else {
                Settings.Global.putString(
                        getContentResolver(),
                        Settings.Global.HTTP_PROXY,
                        proxy_string
                );
                Toast.makeText(MainActivity.this, "Proxy set to " + proxy_string, Toast.LENGTH_LONG).show();
            }
            MainActivity.browser.loadUrl("javascript:(updateIpRegion(\"Searching...\"))");
            MainActivity.browser.loadUrl("javascript:(updateIpCity(\"Searching...\"))");
            MainActivity.browser.loadUrl("javascript:(updateIP(\"Searching...\"))");
            handler.post(runnable);
            new GetPublicIP().execute();
            new GetPublicIPv6().execute();
            getLastLocation();
            getLocation();
            requestNewLocationData();
        }
        catch (SecurityException e) {
            Toast.makeText(MainActivity.this, "Permission denied to WRITE_SECURE_SETTINGS", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public static void updateIPData(Context context, String proxy) {
        proxy_string = proxy;
        MainActivity.proxy.setText(proxy_string);
        MainActivity.set_proxy.setEnabled(true);
        MainActivity.set_proxy.performClick();
    }

    public static void postAPI(String postUrl, RequestBody postBody) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                try {
                    JSONObject obj = new JSONObject(res);
                    if (obj.getInt("status") == 200) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.browser.loadUrl("javascript:(showToast(\"" + "Saved" + "\"))");
                            }
                        });
                    }
                    else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.browser.loadUrl("javascript:(showToast(\"" + "Error" + "\"))");
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void checkIP(String postUrl, RequestBody postBody) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                try {
                    JSONObject obj = new JSONObject(res);
                    if (obj.getBoolean("code")) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.browser.loadUrl("javascript:(updateIP(\"" + MainActivity.ip_string + " <span style='color: green;'>&#10004;</span>\"))");
                                MainActivity.browser.loadUrl("javascript:(updateIPv6(\"" + MainActivity.ipv6_string + " <span style='color: green;'>&#10004;</span>\"))");
                                MainActivity.browser.loadUrl("javascript:(showToast(\"" + "Valid IP" + "\"))");
                            }
                        });
                    }
                    else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.browser.loadUrl("javascript:(updateIP(\"<del>" + MainActivity.ip_string + "</del>\"))");
                                MainActivity.browser.loadUrl("javascript:(updateIPv6(\"<del>" + MainActivity.ipv6_string + "</del>\"))");
                                MainActivity.browser.loadUrl("javascript:(showToast(\"" + "Invalid IP" + "\"))");
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    String displayInterfaceInformation() {
        String str = "";
        try {
            Iterator<NetworkInterface> it = Collections.list(NetworkInterface.getNetworkInterfaces()).iterator();
            while (it.hasNext()) {
                Iterator<InetAddress> it2 = Collections.list(((NetworkInterface) it.next()).getInetAddresses()).iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    InetAddress inetAddress = (InetAddress) it2.next();
                    if (!inetAddress.isLoopbackAddress()) {
                        String hostAddress = inetAddress.getHostAddress();
                        if (hostAddress.indexOf(58) < 0) {
                            str = hostAddress;
                            break;
                        }
                    }
                }
            }
        } catch (Throwable th) {
        }
        return str;
    }

    public void getLocation() {
        Location lastKnownLocation;
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != -1) {
            @SuppressLint("WrongConstant") LocationManager locationManager2 = (LocationManager) getApplicationContext().getSystemService("location");
            List<String> providers = locationManager2.getProviders(true);
            System.out.println("*******Provider*******");
            for (String provider : providers) {
                System.out.println(provider);
            }
            System.out.println("*******Provider*******");
            System.out.println(locationManager2.getLastKnownLocation("network"));
            if (locationManager2.getLastKnownLocation("network") == null)
                MainActivity.network_location_status = true;
            locationManager2.requestLocationUpdates("network", 1000, 0.0f, new LocationListener() {
                public void onLocationChanged(Location location) {
                    MainActivity.browser.loadUrl("javascript:(updateNetworkLocation(\"" + location.getLatitude() + "\", \"" + location.getLongitude() + "\"))");
                    MainActivity.network_location_string = location.getLatitude() + "," + location.getLongitude();
                    MainActivity.network_location_status = true;
                }

                public void onProviderDisabled(String str) {
                }

                public void onProviderEnabled(String str) {
                }

                public void onStatusChanged(String str, int i, Bundle bundle) {
                }
            });
            if (locationManager2 != null && (lastKnownLocation = locationManager2.getLastKnownLocation("network")) != null) {
                final double latitude = lastKnownLocation.getLatitude();
                final double longitude = lastKnownLocation.getLongitude();
                MainActivity.browser.loadUrl("javascript:(updateNetworkLocation(\"" + latitude + "\", \"" + longitude + "\"))");
                MainActivity.network_location_string = latitude + "," + longitude;
                MainActivity.network_location_status = true;
            }
        }
    }

    @SuppressLint("WrongConstant")
    DisplayMetrics getScreenDisplay() {
        WindowManager windowManager;
        Display defaultDisplay;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (!(getApplicationContext() == null || (windowManager = (WindowManager) getSystemService("window")) == null || (defaultDisplay = windowManager.getDefaultDisplay()) == null)) {
            defaultDisplay.getMetrics(displayMetrics);
        }
        return displayMetrics;
    }


}

class GetPublicIP extends AsyncTask<String, String, String> {
    URLConnection socket;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected String doInBackground(String... strings) {
        String publicIP = "";

        try  {
            if (MainActivity.proxy_string == null || MainActivity.proxy_string.trim().equals("") || MainActivity.proxy_string.trim().equals(":0")) {
                socket = new URL("http://ip-api.com/json").openConnection();
            }
            else {
                String[] arrayString = MainActivity.proxy_string.split(":");
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(arrayString[0], Integer.parseInt(arrayString[1])));
                socket = new URL("http://ip-api.com/json").openConnection(proxy);
            }

            socket.setUseCaches( false );
            socket.setDefaultUseCaches( false );
            HttpURLConnection conn = ( HttpURLConnection )socket;
            conn.setUseCaches( false );
            conn.setDefaultUseCaches( false );
            conn.setRequestProperty( "Cache-Control",  "no-cache" );
            conn.addRequestProperty("Cache-Control", "max-age=0");
            conn.setRequestProperty( "Pragma",  "no-cache" );
            conn.setRequestProperty( "Expires",  "0" );
            conn.setRequestMethod( "GET" );
            conn.connect();
            java.util.Scanner s = new java.util.Scanner(conn.getInputStream(), "UTF-8").useDelimiter("\\A");
            publicIP = s.next();
            conn.disconnect();
        } catch (IOException e) {
            MainActivity.browser.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.browser.loadUrl("javascript:(updateIpRegion(\"No Internet\"))");
                    MainActivity.browser.loadUrl("javascript:(updateIpCity(\"No Internet\"))");
                    MainActivity.browser.loadUrl("javascript:(updateIP(\"No Internet\"))");
                    MainActivity.handler.removeCallbacks(MainActivity.runnable);
                }
            });
            e.printStackTrace();
        }
        return publicIP;
    }

    @Override
    protected void onPostExecute(String publicIp) {
        super.onPostExecute(publicIp);
        try {
            JSONObject obj = new JSONObject(publicIp);
            MainActivity.browser.loadUrl("javascript:(updateIpRegion(\"" + obj.get("regionName") + "\"))");
            MainActivity.browser.loadUrl("javascript:(updateIpCity(\"" + obj.get("city") + "\"))");
            MainActivity.browser.loadUrl("javascript:(updateIP(\"" + obj.get("query") + "\"))");
            MainActivity.ip_string = obj.getString("query");
            MainActivity.ip_city = obj.getString("city");
            MainActivity.ip_status = true;
        } catch (JSONException e) {
            e.printStackTrace();
            MainActivity.handler.removeCallbacks(MainActivity.runnable);
        }
    }
}

class GetPublicIPv6 extends AsyncTask<String, String, String> {
    URLConnection socket;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected String doInBackground(String... strings) {
        String publicIP = "";

        try  {
            if (MainActivity.proxy_string == null || MainActivity.proxy_string.trim().equals("") || MainActivity.proxy_string.trim().equals(":0")) {
                socket = new URL("https://api64.ipify.org").openConnection();
            }
            else {
                String[] arrayString = MainActivity.proxy_string.split(":");
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(arrayString[0], Integer.parseInt(arrayString[1])));
                socket = new URL("https://api64.ipify.org").openConnection(proxy);
            }

            socket.setUseCaches( false );
            socket.setDefaultUseCaches( false );
            HttpURLConnection conn = ( HttpURLConnection )socket;
            conn.setUseCaches( false );
            conn.setDefaultUseCaches( false );
            conn.setRequestProperty( "Cache-Control",  "no-cache" );
            conn.addRequestProperty("Cache-Control", "max-age=0");
            conn.setRequestProperty( "Pragma",  "no-cache" );
            conn.setRequestProperty( "Expires",  "0" );
            conn.setRequestMethod( "GET" );
            conn.connect();
            java.util.Scanner s = new java.util.Scanner(conn.getInputStream(), "UTF-8").useDelimiter("\\A");
            publicIP = s.next();
            conn.disconnect();
        } catch (IOException e) {
            MainActivity.browser.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.browser.loadUrl("javascript:(updateIPv6(\"No Internet\"))");
                    MainActivity.handler.removeCallbacks(MainActivity.runnable);
                }
            });
            e.printStackTrace();
        }
        return publicIP;
    }

    @Override
    protected void onPostExecute(String publicIp) {
        super.onPostExecute(publicIp);
        MainActivity.browser.loadUrl("javascript:(updateIPv6(\"" + publicIp + "\"))");
        MainActivity.ipv6_string = publicIp;
        MainActivity.ipv6_status = true;
    }
}
