package cafe.adriel.androidaudiorecorder;

import com.cleveroad.audiovisualization.DbmHandler;

public class VisualizerHandler extends DbmHandler<Float> {

    @Override
    protected void onDataReceivedImpl(Float amplitude, int layersCount, float[] dBmArray, float[] ampsArray) {
        amplitude = amplitude / 100;
        if(amplitude <= 0.5){
            amplitude = 0.0f;
        } else if(amplitude > 0.5 && amplitude <= 0.6){
            amplitude = 0.2f;
        } else if(amplitude > 0.6 && amplitude <= 0.7){
            amplitude = 0.6f;
        } else if(amplitude > 0.7){
            amplitude = 1f;
        }
        dBmArray[0] = amplitude;
        ampsArray[0] = amplitude;
    }

    public void stop() {
        calmDownAndStopRendering();
    }

}