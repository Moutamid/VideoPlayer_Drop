package com.moutamid.videoplayer;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.downloader.utils.Utils;
import com.fxn.stash.Stash;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

//    https://dl.dropboxusercontent.com/s/lu0ga5ix0m9zzy2/VIDEO_6e995d33-5cbe-4ebe-8c15-e2f62cbb20f0.mp4
//    https://www.dropbox.com/s/lu0ga5ix0m9zzy2/VIDEO_6e995d33-5cbe-4ebe-8c15-e2f62cbb20f0.mp4?dl=0

    String source;
    String downloadLink;
    File dir;
    VideoView videoView;
    ProgressDialog progressDialog;
    MaterialCardView fullscreen;
    ImageView btnImg;
    String stashSource;
    LinkModel linkModel;
    StartupOnBootUpReceiver bootUpReceiver = new StartupOnBootUpReceiver();
    String permission[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PRDownloader.initialize(getApplicationContext());

        videoView = findViewById(R.id.videoView);
        fullscreen = findViewById(R.id.fullscreen);
        btnImg = findViewById(R.id.btnImg);

        stashSource = Stash.getString("stashSource");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Video is Downloading...");
        progressDialog.setProgressStyle(progressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);

        ActivityCompat.requestPermissions(this, permission, 1);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            btnImg.setImageResource(R.drawable.ic_baseline_fullscreen);
        }else {
            btnImg.setImageResource(R.drawable.ic_baseline_fullscreen_exit);
        }

        // Enabling database for resume support even after the application is killed:
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder().setDatabaseEnabled(true).build();
        PRDownloader.initialize(getApplicationContext(), config);

        getVideo();

        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

//        dir = getRootDirPath(this);

        fullscreen.setOnClickListener(v -> {
            orientataion();
        });
    }

    private void getVideo() {
        Constants.databaseReference().child("link").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                linkModel = snapshot.getValue(LinkModel.class);
                source = linkModel.getLink();
                source = source.replace("?dl=0", "");
                downloadLink = source.replace("www.dropbox.com", "dl.dropboxusercontent.com");
                if (stashSource.isEmpty() || stashSource == null){
                    Stash.put("stashSource", downloadLink);
                    downloadVideo(downloadLink);
                } else {
                    if (stashSource.equals(downloadLink)){
                        String filename = Stash.getString("filename");
                        String link = dir.getPath() + "/" + filename;
                        startVideo(link);
                    } else {
                        Stash.put("stashSource", downloadLink);
                        downloadVideo(downloadLink);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG", error.getDetails());
            }
        });
    }

    private void downloadVideo(String downloadLink) {
        String filename = URLUtil.guessFileName(downloadLink, null, null);
        Stash.put("filename", filename);
        PRDownloader.download(downloadLink, dir.getPath(), filename)
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
                        progressDialog.show();
                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        double progressPer = (100.00 * progress.currentBytes) / progress.totalBytes;
                        progressDialog.setProgress((int) progressPer);
                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        progressDialog.dismiss();
                        String link = dir.getPath() + "/" + filename;
                        startVideo(link);
                    }

                    @Override
                    public void onError(Error error) {
                        progressDialog.dismiss();
                        if (error.isServerError()){
                            Toast.makeText(MainActivity.this, error.getServerErrorMessage(), Toast.LENGTH_SHORT).show();
                        } else if (error.isConnectionError()){
                            Toast.makeText(MainActivity.this, "" + error.getConnectionException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void orientataion(){
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public void startVideo(String link){
        Uri uri = Uri.parse(link);
        videoView.setVideoURI(uri);
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        registerReceiver(bootUpReceiver, filter);
    }
}