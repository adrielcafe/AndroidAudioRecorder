package cafe.adriel.androidaudiorecorder;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import omrecorder.AudioChunk;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;

public class AudioRecorderActivity extends AppCompatActivity {

    private Recorder recorder;
    private Timer timer;
    private MenuItem selectMenuItem;
    private int secondsRecorded;
    private boolean isRecording;
    private String filePath;
    private int color;
    private RelativeLayout contentLayout;
    private TextView timerView;
    private ImageView micView;
    private ImageButton recordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);

        filePath = getIntent().getStringExtra(AndroidAudioRecorder.EXTRA_FILE_PATH);
        color = getIntent().getIntExtra(AndroidAudioRecorder.EXTRA_COLOR, Color.BLACK);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(Util.getDarkerColor(color)));
            getSupportActionBar().setHomeAsUpIndicator(
                    getResources().getDrawable(R.drawable.ic_clear));
        }

        contentLayout = (RelativeLayout) findViewById(R.id.content);
        timerView = (TextView) findViewById(R.id.timer);
        micView = (ImageView) findViewById(R.id.mic);
        recordView = (ImageButton) findViewById(R.id.record);

        // to get drawable resources of check icon and clear icon
        Drawable clear = getResources().getDrawable(R.drawable.ic_clear);
        Drawable check = getResources().getDrawable(R.drawable.ic_check);

        contentLayout.setBackgroundColor(color);

        // check to set tint of images
        if(Util.isBrightColor(color)) {
            micView.setColorFilter(Color.BLACK);
            recordView.setColorFilter(Color.BLACK);
            timerView.setTextColor(Color.BLACK);
            clear.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            check.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    protected void onPause() {
        stopRecoding();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_CANCELED);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        stopRecoding();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.audio_recorder, menu);
        selectMenuItem = menu.findItem(R.id.action_select);
        selectMenuItem.setIcon(getResources().getDrawable(R.drawable.ic_check));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
        } else if (i == R.id.action_select) {
            selectAudio();
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleRecord(View v) {
        if (isRecording) {
            stopRecoding();
        } else {
            startRecoding();
        }
    }

    private void selectAudio() {
        setResult(RESULT_OK);
        finish();
    }

    private void startRecoding() {
        isRecording = true;
        selectMenuItem.setVisible(false);
        recordView.setImageResource(R.drawable.ic_stop);
        timerView.setText("00:00:00");

        recorder = OmRecorder.wav(
                new PullTransport.Default(Util.getMic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override
                    public void onAudioChunkPulled(AudioChunk audioChunk) {
                        animateMic(audioChunk.maxAmplitude());
                    }
                }), new File(filePath));
        recorder.startRecording();

        secondsRecorded = 0;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTimer();
            }
        }, 0, 1000);
    }

    private void stopRecoding() {
        isRecording = false;
        selectMenuItem.setVisible(true);
        recordView.setImageResource(R.drawable.ic_play);

        if (recorder != null) {
            recorder.stopRecording();
            recorder = null;
        }

        if (timer != null) {
            timer.cancel();
        }

        animateMic(0);
    }

    private void animateMic(double amplitude) {
        float peak = (float) (amplitude / 150);
        micView.animate()
                .scaleX(1 + peak)
                .scaleY(1 + peak)
                .setDuration(5)
                .start();
    }

    private void updateTimer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                secondsRecorded++;
                timerView.setText(Util.formatSeconds(secondsRecorded));
            }
        });
    }
}
