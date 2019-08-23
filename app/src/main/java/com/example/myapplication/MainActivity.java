package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.browser.customtabs.TrustedWebUtils;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "Debug TrustedWeb";
    private final int SESSION_ID = 278391278;
    private final String mHost = "https://com-amptest.firebaseapp.com";
    private final String mUrl = "/twa_test/";
    private String startUrl = mHost+mUrl;

    @Nullable
    private TwaCustomTabsServiceConnection mServiceConnection;
    @Nullable
    private TwaCustomTabsServiceConnectionOpenNow mServiceConnectionNow;
    @Nullable
    CustomTabsIntent mCustomTabsIntent;

    /*
    SUPPORTED_CHROME_PACKAGES is missing in TrustedWebUtils
     */
    private static final String CHROME_LOCAL_BUILD_PACKAGE = "com.google.android.apps.chrome";
    private static final String CHROMIUM_LOCAL_BUILD_PACKAGE = "org.chromium.chrome";
    private static final String CHROME_CANARY_PACKAGE = "com.chrome.canary";
    private static final String CHROME_DEV_PACKAGE = "com.chrome.dev";
    private static final String CHROME_STABLE_PACKAGE = "com.android.chrome";
    private static final String CHROME_BETA_PACKAGE = "com.chrome.beta";
    private boolean runflg = true;
    public static final List<String> SUPPORTED_CHROME_PACKAGES = Arrays.asList(
            CHROME_LOCAL_BUILD_PACKAGE,
            CHROMIUM_LOCAL_BUILD_PACKAGE,
            CHROME_CANARY_PACKAGE,
            CHROME_DEV_PACKAGE,
            CHROME_BETA_PACKAGE,
            CHROME_STABLE_PACKAGE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTwaPage(mHost+mUrl);
            }
        });

        String customTabsProviderPackage = CustomTabsClient.getPackageName(this, SUPPORTED_CHROME_PACKAGES, false);

        if( customTabsProviderPackage == null ){
            //no supported browser found for TWA
            //Show a webview instead
            // Implement later...
        }

        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        if( Intent.ACTION_VIEW.equals(appLinkAction) && appLinkAction != null ){
            // This code is for the app link's intent to open a url in intent immediately
            String query = appLinkData.getQuery();
            startUrl = mHost+appLinkData.getPath()+((query!=null)?("?"+appLinkData.getQuery()):"");
            mServiceConnectionNow = new TwaCustomTabsServiceConnectionOpenNow();
            CustomTabsClient.bindCustomTabsService(this, customTabsProviderPackage, mServiceConnectionNow);
        }else{
            // The code is for normal startup to warm up custom tab session.
            mServiceConnection = new TwaCustomTabsServiceConnection();
            CustomTabsClient.bindCustomTabsService(this, customTabsProviderPackage, mServiceConnection);
        }
    }

    private void openTwaPage(String uri){
        Log.d(TAG, "target URI:"+uri);
        Log.d(TAG, "mCustomTabsIntent value:"+((mCustomTabsIntent==null)?"null":mCustomTabsIntent.toString()));
        if (mCustomTabsIntent != null) {
            TrustedWebUtils.launchAsTrustedWebActivity(MainActivity.this, mCustomTabsIntent, Uri.parse(uri));
            runflg = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( mServiceConnection != null ){
            unbindService(mServiceConnection);
        }
    }

    //This one is for warming up the TWA session.
    private class TwaCustomTabsServiceConnection extends CustomTabsServiceConnection {
        @Override
        public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
            CustomTabsSession session = client.newSession(null);
            mCustomTabsIntent = new CustomTabsIntent.Builder(session).build();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "Twa CustomTab Service Disconnected");
        }
    }

    //This one is for opening TWA page immediately
    private class TwaCustomTabsServiceConnectionOpenNow extends CustomTabsServiceConnection {
        @Override
        public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
            CustomTabsSession session = client.newSession(null);
            mCustomTabsIntent = new CustomTabsIntent.Builder(session).build();

            // And this part is the only diff from above one.
            openTwaPage(startUrl);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "Twa CustomTab Service Disconnected");
        }
    }
}
