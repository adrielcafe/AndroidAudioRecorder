package cafe.adriel.androidaudiorecorder;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cleveroad.audiovisualization.GLAudioVisualizationView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import omrecorder.AudioChunk;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;

public class AudioRecorderActivity extends AppCompatActivity implements PullTransport.OnAudioChunkPulledListener {

    private Recorder recorder;
    private VisualizerHandler visualizerHandler;

    private Timer timer;
    private MenuItem selectMenuItem;
    private String filePath;
    private int secondsRecorded;
    private int color;
    private boolean isRecording;

    private RelativeLayout contentLayout;
    private GLAudioVisualizationView audioVisualizationView;
    private TextView timerView;
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
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(Util.getDarkerColor(color)));
            getSupportActionBar().setHomeAsUpIndicator(
                    getResources().getDrawable(R.drawable.ic_clear));
        }

        audioVisualizationView = new GLAudioVisualizationView.Builder(this)
                .setLayersCount(1)
                .setWavesCount(5)
                .setWavesHeight(R.dimen.wave_height)
                .setWavesFooterHeight(R.dimen.footer_height)
                .setBubblesPerLayer(16)
                .setBubblesSize(R.dimen.bubble_size)
                .setBubblesRandomizeSize(true)
                .setBackgroundColor(Util.getDarkerColor(color))
                .setLayerColors(new int[]{color})
                .build();

        contentLayout = (RelativeLayout) findViewById(R.id.content);
        timerView = (TextView) findViewById(R.id.timer);
        recordView = (ImageButton) findViewById(R.id.record);

        contentLayout.setBackgroundColor(Util.getDarkerColor(color));
        contentLayout.addView(audioVisualizationView, 0);

        visualizerHandler = new VisualizerHandler();
        audioVisualizationView.linkTo(visualizerHandler);

        if(Util.isBrightColor(color)) {
            recordView.setColorFilter(Color.BLACK);
            timerView.setTextColor(Color.BLACK);
            getResources().getDrawable(R.drawable.ic_clear)
                    .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            getResources().getDrawable(R.drawable.ic_check)
                    .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        audioVisualizationView.onResume();
    }

    @Override
    protected void onPause() {
        stopRecoding();
        audioVisualizationView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_CANCELED);
        audioVisualizationView.release();
        super.onDestroy();
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

    @Override
    public void onAudioChunkPulled(AudioChunk audioChunk) {
        float amplitude = isRecording ? (float) audioChunk.maxAmplitude() : 0f;
        visualizerHandler.onDataReceived(amplitude);
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
                new PullTransport.Default(Util.getMic(), this),
                new File(filePath));
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
        if(!isFinishing()) {
            selectMenuItem.setVisible(true);
        }
        recordView.setImageResource(R.drawable.ic_play);

        if(visualizerHandler != null) {
            visualizerHandler.stop();
        }

        if (recorder != null) {
            recorder.stopRecording();
            recorder = null;
        }

        if (timer != null) {
            timer.cancel();
        }
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
