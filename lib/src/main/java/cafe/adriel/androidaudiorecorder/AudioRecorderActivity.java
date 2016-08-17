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
    private MenuItem saveMenuItem;
    private String filePath;
    private int color;
    private int secondsRecorded;
    private boolean isRecording;

    private RelativeLayout contentLayout;
    private GLAudioVisualizationView audioVisualizationView;
    private TextView timerView;
    private ImageButton restartView;
    private ImageButton recordView;
    private ImageButton playView;

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
                .setWavesCount(6)
                .setWavesHeight(R.dimen.wave_height)
                .setWavesFooterHeight(R.dimen.footer_height)
                .setBubblesPerLayer(20)
                .setBubblesSize(R.dimen.bubble_size)
                .setBubblesRandomizeSize(true)
                .setBackgroundColor(Util.getDarkerColor(color))
                .setLayerColors(new int[]{color})
                .build();

        contentLayout = (RelativeLayout) findViewById(R.id.content);
        timerView = (TextView) findViewById(R.id.timer);
        restartView = (ImageButton) findViewById(R.id.restart);
        recordView = (ImageButton) findViewById(R.id.record);
        playView = (ImageButton) findViewById(R.id.play);

        contentLayout.setBackgroundColor(Util.getDarkerColor(color));
        contentLayout.addView(audioVisualizationView, 0);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);

        visualizerHandler = new VisualizerHandler();
        audioVisualizationView.linkTo(visualizerHandler);

        if(Util.isBrightColor(color)) {
            restartView.setColorFilter(Color.BLACK);
            recordView.setColorFilter(Color.BLACK);
            playView.setColorFilter(Color.BLACK);
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
        stopRecording();
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
        saveMenuItem = menu.findItem(R.id.action_save);
        saveMenuItem.setIcon(getResources().getDrawable(R.drawable.ic_check));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
        } else if (i == R.id.action_save) {
            selectAudio();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAudioChunkPulled(AudioChunk audioChunk) {
        float amplitude = isRecording ? (float) audioChunk.maxAmplitude() : 0f;
        visualizerHandler.onDataReceived(amplitude);
    }

    public void toggleRecording(View v) {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    public void restartRecording(View v){
        stopRecording();
        saveMenuItem.setVisible(false);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.ic_rec);
        timerView.setText("00:00:00");
        secondsRecorded = 0;
    }

    public void playRecording(View v){
        // TODO play recorded audio
    }

    private void selectAudio() {
        setResult(RESULT_OK);
        finish();
    }

    private void startRecording() {
        isRecording = true;
        saveMenuItem.setVisible(false);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
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

    private void stopRecording() {
        isRecording = false;
        if(!isFinishing()) {
            saveMenuItem.setVisible(true);
        }
//        restartView.setVisibility(View.VISIBLE);
//        playView.setVisibility(View.VISIBLE);
        recordView.setImageResource(R.drawable.ic_rec);

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
