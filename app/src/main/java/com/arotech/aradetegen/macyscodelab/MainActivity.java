package com.arotech.aradetegen.macyscodelab;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_READ_SD = 0;
    private static final String DIRECTORY_SUMMERY = "DirectorySummery";
    private static final int NOTIFICATION_ID = 100;
    private View mLayout;

    private Subscriber<FileInfo> subscription;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.biggest_file_list)
    TextView biggestFileList;

    @BindView(R.id.frequent_file_list)
    TextView frequentFileList;

    @BindView(R.id.average_file_size)
    TextView averageFileSize;

    @BindView(R.id.message)
    TextView ScanMessage;

    @BindView(R.id.summery)
    View summeryView;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private Menu mMenu;
    private boolean scanningInProgress;

    DirectorySummery mDirectorySummery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mLayout = findViewById(R.id.main_layout);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unSubscribe();
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(DIRECTORY_SUMMERY, mDirectorySummery);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mDirectorySummery = (DirectorySummery) savedInstanceState.getSerializable(DIRECTORY_SUMMERY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            share(mDirectorySummery);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void share(DirectorySummery mDirectorySummery) {
        if (mDirectorySummery != null) {
            String shareBody = String.format(getString(R.string.share_body),
                    mDirectorySummery.getTopBiggestFilesString(),
                    mDirectorySummery.getTopFrequentFileExtensionsString(),
                    mDirectorySummery.getAverageFileSizeString());
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
        } else {
            Toast.makeText(this, R.string.nothing_to_share, Toast.LENGTH_LONG).show();
        }

    }

    @OnClick(R.id.fab)
    void onFabButtonClick() {
        if (scanningInProgress) {
            unSubscribe();
            setInProgress(false);
        } else {
            checkPermissionAndScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_SD) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(mLayout, R.string.permission_granted,
                        Snackbar.LENGTH_SHORT).show();
                scan();
            } else {
                Snackbar.make(mLayout, R.string.permission_not_granted,
                        Snackbar.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * This method is called when ever we want to update the UI. It will check if we have DirectorySummery and update the screen accordingly.
     * <p/>
     * NOTE: The better way to present the Data is using a Recycler view with Sectional header. But since I have limited time I am going to go with an easy way out :).
     */
    private void updateUI() {
        if (mDirectorySummery != null) {
            Toast.makeText(this, R.string.updated, Toast.LENGTH_LONG).show();
            summeryView.setVisibility(View.VISIBLE);
            ScanMessage.setVisibility(View.GONE);
            biggestFileList.setText(mDirectorySummery.getTopBiggestFilesString());
            frequentFileList.setText(mDirectorySummery.getTopFrequentFileExtensionsString());
            averageFileSize.setText(String.format(getString(R.string.average_size), mDirectorySummery.getAverageFileSize()));
            enableMenu(true);
        } else {
            summeryView.setVisibility(View.GONE);
            ScanMessage.setVisibility(View.VISIBLE);
            enableMenu(false);
        }
    }

    private void enableMenu(boolean enable) {
        if (mMenu != null)
            mMenu.findItem(R.id.action_share).setEnabled(enable);
    }

    /**
     * I am using runtime permission so I should check if User give proper permission before Scanning. If yes I will request for permission else I will start scanning.
     */
    public void checkPermissionAndScan() {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestReadPermission();
        } else {
            scan();
        }
    }

    /**
     * This method will start the scanning the drive on background tread. I used RxJava for the implementation.
     */
    private void scan() {
        setInProgress(true);
        GetFileList getFileList = new GetFileList() {
            @Override
            public void onSuccessfullyCompleted(DirectorySummery directorySummery) {
                setInProgress(false);
                mDirectorySummery = directorySummery;
                updateUI();
            }

            @Override
            public void onCompletedWithError(String error) {
                setInProgress(false);
                Toast.makeText(getBaseContext(),
                        (TextUtils.isEmpty(error) ? getString(R.string.cannot_scan_error_msg) : error),
                        Toast.LENGTH_LONG).show();
            }
        };

        subscription = getFileList.getSubscriber();
        getFileList.getObservable()
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread()) // Create a new Thread
                .subscribe(subscription);
    }

    /**
     * Here we request required permission to read file from directory.
     */
    private void requestReadPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Snackbar.make(mLayout, R.string.read_permission_needed,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_READ_SD);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_SD);
        }
    }

    /**
     * we update the UI for scan in progress state
     */
    public void setInProgress(boolean isInProgress) {
        if (isInProgress) {
            scanningInProgress = true;
            fab.setImageResource(R.mipmap.btn_close);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            scanningInProgress = false;
            fab.setImageResource(R.mipmap.ic_scan);
            progressBar.setVisibility(View.GONE);
        }
        showNotification(isInProgress);
    }

    /**
     * If a user decide to cancel the scanning this method will unSubscribe the  user.l
     */
    private void unSubscribe() {
        if (subscription != null)
            subscription.unsubscribe();
    }

    /**
     * Show/Hide notification when scanning is in progress
     */
    private void showNotification(boolean show) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (show) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.notification_title))
                    .setSmallIcon(R.mipmap.ic_scan)
                    .setDefaults(Notification.DEFAULT_ALL);
            mNotificationManager.notify(NOTIFICATION_ID, builder.build());
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }
}


