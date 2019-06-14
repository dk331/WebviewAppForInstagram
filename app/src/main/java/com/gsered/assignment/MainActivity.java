package com.gsered.assignment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 9000;
    private final static int FCR = 1;
    private String mCM;
    private ValueCallback<Uri[]> mUMA;
    private String instaURL = "https://www.instagram.com";
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {

            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                webSettings.setSafeBrowsingEnabled(true);
            }
            webSettings.setAllowFileAccess(true);
            webSettings.setLoadsImagesAutomatically(true);
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setSupportZoom(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
            webSettings.setAppCacheEnabled(true);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    view.loadUrl(instaURL);
                    return true;
                }
            });
            webView.setWebChromeClient(new MyWebChromeClient());

            webView.loadUrl(instaURL);
        }

        final SwipeRefreshLayout swipeView = findViewById(R.id.swipe);
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.loadUrl("javascript:window.location.reload(true)");
                swipeView.setRefreshing(false);
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onDestroy() {
        if (webView != null)
            webView.destroy();
        super.onDestroy();
    }

    protected void showDialogOK(Context context, String message, DialogInterface.OnClickListener okListener) {
        if (!isFinishing()) {
            new AlertDialog.Builder(context)
                    .setMessage(message)
                    .setPositiveButton("OK", okListener)
                    .setNegativeButton("Cancel", okListener)
                    .create()
                    .show();
        }
    }

    protected void explain(String msg, Context context) {
        if (!isFinishing()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(msg)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse(getPackageName())));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            paramDialogInterface.dismiss();
                        }
                    });
            dialog.show();
        }
    }

    private boolean checkAndRequestPermissions() {
        int camerapermission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        int writepermission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readpermission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (camerapermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (readpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            Map<String, Integer> perms = new HashMap<>();
            perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                if ((perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                        && (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                        && (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {

                    return;

                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)
                            || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        showDialogOK(MainActivity.this, "Service Permissions are required for this app",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                checkAndRequestPermissions();
                                                break;
                                            case DialogInterface.BUTTON_NEGATIVE:
                                                dialog.dismiss();
                                                break;
                                        }
                                    }
                                });
                    } else {
                        explain(getString(R.string.go_to_settings_for_permissions), MainActivity.this);
                    }
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Uri[] results = null;
        //Check if response is positive
        if (resultCode == RESULT_OK) {
            if (requestCode == FCR) {
                if (null == mUMA) {
                    return;
                }
                if (data == null) {
                    //Capture Photo if no image available
                    if (mCM != null) {
                        results = new Uri[]{Uri.parse(mCM)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
        }
        mUMA.onReceiveValue(results);
        mUMA = null;
    }

    // Create an image file
    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!storageDir.mkdir()) storageDir.mkdirs();
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public class MyWebChromeClient extends WebChromeClient {

        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

            if (checkAndRequestPermissions()) {
                if (!isFinishing()) {
                    mUMA = filePathCallback;
                    final String[] options = {"Take photo", "Choose from gallery"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle("Select option");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            if (options[item].equals("Take photo")) {
                                dialog.dismiss();

                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                                    File photoFile = null;
                                    try {
                                        photoFile = createImageFile();
                                        takePictureIntent.putExtra("PhotoPath", mCM);
                                    } catch (IOException ex) {
                                        Log.e(TAG, "Image file creation failed", ex);
                                    }
                                    if (photoFile != null) {
                                        mCM = "file:" + photoFile.getAbsolutePath();
                                        if (Build.VERSION.SDK_INT >= 24) {
                                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                                    FileProvider.getUriForFile(MainActivity.this,
                                                            BuildConfig.APPLICATION_ID + ".provider",
                                                            photoFile));
                                        } else {
                                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                                        }
                                    } else {
                                        takePictureIntent = null;
                                    }
                                    startActivityForResult(takePictureIntent, FCR);
                                }
                            } else if (options[item].equals("Choose from gallery")) {
                                dialog.dismiss();
                                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                                contentSelectionIntent.setType("*/*");

                                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                                startActivityForResult(chooserIntent, FCR);
                            }
                        }
                    });
                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                            if (mUMA != null) {
                                mUMA.onReceiveValue(null);
                            }
                        }
                    });
                    builder.show();

                    return true;
                }
            } else {
                filePathCallback.onReceiveValue(null);
                return true;
            }
            return true;
        }
    }
}
