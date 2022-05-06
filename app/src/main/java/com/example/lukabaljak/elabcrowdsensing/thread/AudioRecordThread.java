package com.example.lukabaljak.elabcrowdsensing.thread;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.HashMap;
import java.util.Timer;

/**
 * Created by maricm on 6/11/2017.
 */

public class AudioRecordThread extends Thread {
    private OnAudioRecordingInterface mInterface;

    private AudioRecord recorder = null;
    private boolean isRecording = false;

    private Timer timer = null;

    private int totalSeconds = 0;

    private HashMap<String, Integer>[] sampleValues = null;

    public AudioRecordThread(OnAudioRecordingInterface onAudioRecordingInterface) {
        this.mInterface = onAudioRecordingInterface;
    }

    int bufferSize = 0;

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        timer = new Timer();
        totalSeconds = 0;
        recorder = findAudioRecord();

        if (recorder != null) {
            startRecording();
        }
    }

    private void startRecording() {
        if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            recorder.startRecording();
            isRecording = true;

            int rate = (recorder.getSampleRate() / 8000);

            short[] audioData = new short[256 * rate];
            double[] fftAudio = new double[audioData.length];
            DoubleFFT_1D fft = new DoubleFFT_1D(audioData.length);

            int bufferReadResult;
            while (isRecording) {
                bufferReadResult = recorder.read(audioData, 0, audioData.length);

                for (int i = 0; i < audioData.length && i < bufferReadResult; i++) {
                    fftAudio[i] = (double) audioData[i] /        32768.0;
                }

                Log.d("SIZE OF RECORD", bufferReadResult+"");

                fft.realForward(fftAudio);

                mInterface.readAudioDataArray(fftAudio, rate);
            }

            recorder.release();
        } else {
            Log.d("recorder", String.valueOf(recorder.getState()));
        }
    }

    public void stopRecording() {
        isRecording = false;

        if (recorder != null) {
            recorder.release();
        }

        timer.cancel();

        interrupt();
    }

    private static int[] mSampleRates = new int[]{44100, 22050, 11025, 8000};

    private AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[]{AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT}) {
                for (short channelConfig : new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO}) {
                    try {
                        Log.d("audioData", "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
                                + channelConfig);
                        bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        Log.e("audioData", rate + "Exception, keep trying.", e);
                    }
                }
            }
        }
        return null;
    }

    public interface OnAudioRecordingInterface {
        void readAudioDataArray(double[] data, int rate);
    }



}
