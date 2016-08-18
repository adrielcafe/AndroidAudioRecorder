package cafe.adriel.androidaudiorecorder;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import omrecorder.AudioChunk;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;

public class AudioRecorderActivity extends AppCompatActivity
        implements PullTransport.OnAudioChunkPulledListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private MediaPlayer player;
    private Recorder recorder;
    private VisualizerHandler visualizerHandler;

    private String filePath;
    private int color;

    private Timer timer;
    private MenuItem saveMenuItem;
    private int secondsElapsed;
    private boolean isRecording;

    private RelativeLayout contentLayout;
    private GLAudioVisualizationView visualizerView;
    private TextView statusView;
    private TextView timerView;
    private ImageButton restartView;
    private ImageButton recordView;
    private ImageButton playView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);

        if(savedInstanceState != null) {
            filePath = savedInstanceState.getString(AndroidAudioRecorder.EXTRA_FILE_PATH);
            color = savedInstanceState.getInt(AndroidAudioRecorder.EXTRA_COLOR);
        } else {
            filePath = getIntent().getStringExtra(AndroidAudioRecorder.EXTRA_FILE_PATH);
            color = getIntent().getIntExtra(AndroidAudioRecorder.EXTRA_COLOR, Color.BLACK);
        }

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

        visualizerView = new GLAudioVisualizationView.Builder(this)
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
        statusView = (TextView) findViewById(R.id.status);
        timerView = (TextView) findViewById(R.id.timer);
        restartView = (ImageButton) findViewById(R.id.restart);
        recordView = (ImageButton) findViewById(R.id.record);
        playView = (ImageButton) findViewById(R.id.play);

        contentLayout.setBackgroundColor(Util.getDarkerColor(color));
        contentLayout.addView(visualizerView, 0);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);

        if(Util.isBrightColor(color)) {
            getResources().getDrawable(R.drawable.ic_clear)
                    .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            getResources().getDrawable(R.drawable.ic_check)
                    .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            statusView.setTextColor(Color.BLACK);
            timerView.setTextColor(Color.BLACK);
            restartView.setColorFilter(Color.BLACK);
            recordView.setColorFilter(Color.BLACK);
            playView.setColorFilter(Color.BLACK);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            visualizerView.onResume();
        } catch (Exception e){ }
    }

    @Override
    protected void onPause() {
        stopRecording();
        try {
            visualizerView.onPause();
        } catch (Exception e){ }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_CANCELED);
        try {
            visualizerView.release();
        } catch (Exception e){ }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(AndroidAudioRecorder.EXTRA_FILE_PATH, filePath);
        outState.putInt(AndroidAudioRecorder.EXTRA_COLOR, color);
        super.onSaveInstanceState(outState);
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

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {
        if(mediaPlayer != null){

        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopPlaying();
    }

    private void selectAudio() {
        setResult(RESULT_OK);
        finish();
    }

    public void toggleRecording(View v) {
        stopPlaying();
        Util.wait(100, new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        });
    }

    public void togglePlaying(View v){
        stopRecording();
        Util.wait(100, new Runnable() {
            @Override
            public void run() {
                if(isPlaying()){
                    stopPlaying();
                } else {
                    startPlaying();
                }
            }
        });
    }

    public void restartRecording(View v){
        stopRecording();
        stopPlaying();
        saveMenuItem.setVisible(false);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.ic_rec);
        timerView.setText("00:00:00");
        secondsElapsed = 0;
    }

    private void startRecording() {
        isRecording = true;
        saveMenuItem.setVisible(false);
        timerView.setText("00:00:00");
        statusView.setText(R.string.recording);
        statusView.setVisibility(View.VISIBLE);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.ic_stop);
        playView.setImageResource(R.drawable.ic_play);

        visualizerHandler = new VisualizerHandler();
        visualizerView.linkTo(visualizerHandler);

        recorder = OmRecorder.wav(
                new PullTransport.Default(Util.getMic(), AudioRecorderActivity.this),
                new File(filePath));
        recorder.startRecording();

        startTimer();
    }

    private void stopRecording() {
        isRecording = false;
        if(!isFinishing()) {
            saveMenuItem.setVisible(true);
        }
        statusView.setText("");
        statusView.setVisibility(View.INVISIBLE);
        // TODO pause and restart recording before showing this button
//        restartView.setVisibility(View.VISIBLE);
        playView.setVisibility(View.VISIBLE);
        recordView.setImageResource(R.drawable.ic_rec);
        playView.setImageResource(R.drawable.ic_play);

        visualizerView.release();

        if(visualizerHandler != null) {
            visualizerHandler.stop();
        }

        if (recorder != null) {
            recorder.stopRecording();
            recorder = null;
        }

        stopTimer();
    }

    private void startPlaying(){
        try {
            player = new MediaPlayer();
            player.setDataSource(filePath);
            player.prepare();
            player.start();

            visualizerView.linkTo(DbmHandler.Factory.newVisualizerHandler(this, player));
            visualizerView.post(new Runnable() {
                @Override
                public void run() {
                    player.setOnCompletionListener(AudioRecorderActivity.this);
                }
            });

            timerView.setText("00:00:00");
            statusView.setText(R.string.playing);
            statusView.setVisibility(View.VISIBLE);
            playView.setImageResource(R.drawable.ic_pause);

            startTimer();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void stopPlaying(){
        statusView.setText("");
        statusView.setVisibility(View.INVISIBLE);
        playView.setImageResource(R.drawable.ic_play);

        if(player != null){
            try {
                player.stop();
                player.reset();
                player.release();
            } catch (Exception e){ }
        }

        stopTimer();
    }

    private boolean isPlaying(){
        try {
            return player != null && player.isPlaying();
        } catch (Exception e){
            return false;
        }
    }

    private void startTimer(){
        stopTimer();
        secondsElapsed = 0;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTimer();
            }
        }, 0, 1000);
    }

    private void stopTimer(){
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    private void updateTimer() {
        if(isRecording || isPlaying()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    secondsElapsed++;
                    timerView.setText(Util.formatSeconds(secondsElapsed));
                }
            });
        }
    }
}
