package com.khacchung.glitchimage.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.khacchung.glitchimage.BuildConfig;
import com.khacchung.glitchimage.customs.CallBackEffect;
import com.khacchung.glitchimage.customs.CallBackPermission;
import com.khacchung.glitchimage.R;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity implements CallBackEffect {
    private ProgressDialog dialog;
    public static final String PER_CAMERA = Manifest.permission.CAMERA;
    public static final String PER_READ = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String PER_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String PER_AUDIO = Manifest.permission.RECORD_AUDIO;
    public static final int CODE_PERMISSION = 12;

    private CallBackPermission callBackPermission;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setCancelable(false);
    }

    protected void setMessageDialog(String text) {
        dialog.setMessage(text.isEmpty() ?
                getResources().getString(R.string.please_wait) :
                text);
    }

    protected void setFullScreen() {
        hidenStatusBar();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    protected void hidenStatusBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }

    protected void setTitleToolbar(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    protected void showLoading() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    protected void cancleLoading() {
        if (dialog != null && dialog.isShowing()) {
            dialog.cancel();
            dialog.dismiss();
        }
    }

    protected void enableBackButton() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private int getNavigationBarHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight)
            return realHeight - usableHeight;
        else
            return 0;
    }

    protected boolean checkPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public void checkPermission(String[] permission,
                                CallBackPermission callBackPermission) {
        this.callBackPermission = callBackPermission;
        boolean check = true;
        for (String per : permission) {
            if (!checkPermission(per)) {
                check = false;
            }
        }

        if (!check) {
            ActivityCompat.requestPermissions(this,
                    permission,
                    CODE_PERMISSION);
        } else {
            callBackPermission.grantedFullPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == CODE_PERMISSION && grantResults.length > 0) {
            boolean check = true;
            for (int gr : grantResults) {
                if (gr != PackageManager.PERMISSION_GRANTED) {
                    check = false;
                }
            }

            if (check) {
                if (callBackPermission != null) {
                    callBackPermission.grantedFullPermission();
                }
            } else {
                if (callBackPermission != null) {
                    callBackPermission.notFullPermission();
                }
            }
        }
    }

    protected void showSnackBar(View view, String message) {
        Snackbar snackbar;
        snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.colorError));
        TextView textView = snackBarView.findViewById(R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.colorWhite));
        snackbar.show();
    }

    @Override
    public void setEffects(int i) {

    }

    public void intentShareImage(String path) {
        MediaScannerConnection.scanFile(this, new String[]{path},

                null, (path1, uri) -> {
                    Intent shareIntent = new Intent(
                            Intent.ACTION_SEND);
                    shareIntent.setType("image/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    startActivity(Intent.createChooser(shareIntent,
                            getResources().getString(R.string.share_image)));

                });
    }

    public void intentShareVideo(String path) {
        MediaScannerConnection.scanFile(this, new String[]{path},

                null, (path1, uri) -> {
                    Intent shareIntent = new Intent(
                            Intent.ACTION_SEND);
                    shareIntent.setType("video/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    startActivity(Intent.createChooser(shareIntent,
                            getResources().getString(R.string.share_video)));

                });
    }

    protected void intentShareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String shareMessage = getString(R.string.app_name);
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id="
                    + BuildConfig.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Choose one"));
        } catch (Exception e) {
            Toast.makeText(this, getResources().getString(R.string.have_error), Toast.LENGTH_SHORT).show();
        }
    }
}
