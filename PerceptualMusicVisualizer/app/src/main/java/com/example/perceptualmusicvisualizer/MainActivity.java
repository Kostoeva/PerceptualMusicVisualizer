package com.example.perceptualmusicvisualizer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/*
 * needed in AndroidManifest.xml
 * android:minSdkVersion="9"
 * uses-permission of "android.permission.RECORD_AUDIO"
 *
 */

public class MainActivity extends AppCompatActivity {

    PerceptualVisualizer mVisualizerView;

    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVisualizerView = (PerceptualVisualizer) findViewById(R.id.perceptualVisualizerView);

        initAudio();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing() && mMediaPlayer != null) {
            mVisualizer.release();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void initAudio() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mMediaPlayer = MediaPlayer.create(this, R.raw.elvis);

        setupVisualizerFxAndUI();

        // Beginning of playback.
        mVisualizer.setEnabled(true);

        // On end of playback, turn off.
        mMediaPlayer
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mVisualizer.setEnabled(false);
                    }
                });

        mMediaPlayer.start();
    }

    private void setupVisualizerFxAndUI() {

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
        // Get length of audio to be visualized; must be power of 2 for FFT.
        int len_of_audio = Visualizer.getCaptureSizeRange()[1];
        // Pass the length of audio file to Visualizer.
        mVisualizer.setCaptureSize(len_of_audio);
        // Sets a listener.
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate) {
                    }

                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] bytes, int samplingRate) {
                        mVisualizerView.updateVisualizerFFT(bytes);
                    }
                }, Visualizer.getMaxCaptureRate() / 2, false, true);
    }

    List<String> permissions = new ArrayList<String>();

    private boolean askPermission() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int RECORD_AUDIO = checkSelfPermission(Manifest.permission.RECORD_AUDIO );

            if (RECORD_AUDIO != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }


            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 1);
            } else
                return false;
        } else
            return false;
        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {

            boolean result = true;
            for (int i = 0; i < permissions.length; i++) {
                result = result && grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
            if (!result) {

                Toast.makeText(this, "..", Toast.LENGTH_LONG).show();
                askPermission();
            } else {
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}