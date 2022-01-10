package com.li.connectlibrary.recordlibrary;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Recorder {
    static String TAG = "RecorderPlugin";
    static boolean isRecording;
    static String filePath;
    static String filename;
    static byte[] data ;

    static private AudioRecord audioRecord = null;  // 声明 AudioRecord 对象
    static private int recordBufSize = 0; // 声明recoordBufffer的大小字段
    static RecordThread mRecordThread;

    public static void Init()
    {
        Log.d(TAG, "Init");
        filePath = "/storage/emulated/0/NUWA/assets/Voice/";
        //CreateAudioRecord();
    }

    public static void StartRecording()
    {
        Log.d(TAG, "StartRecording");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HHmm-ss");
        String str = df.format( Calendar.getInstance().getTime());
        filename ="RecordTest_" + str;

        audioRecord.startRecording();
        mRecordThread = new RecordThread("RecordThread");
        mRecordThread.start();

        isRecording = true;
    }

    public static void CreateAudioRecord()
    {
        Log.d(TAG, "CreateAudioRecord");

        int frequency = MediaRecorder.AudioSource.DEFAULT;
        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        int encodingPcm16bit = AudioFormat.ENCODING_PCM_16BIT;

        //audioRecord能接受的最小的buffer大小
        recordBufSize = AudioRecord.getMinBufferSize(48000, AudioFormat.CHANNEL_IN_STEREO, encodingPcm16bit);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, encodingPcm16bit, recordBufSize);
        data = new byte[recordBufSize];
    }

    public static void CreateAudioRecord(int frequency, int channelConfiguration, int encodingBitRate)
    {
        Log.d(TAG, "CreateAudioRecord. frequency:" + frequency + ", channel" +channelConfiguration + ",format:" + encodingBitRate );
        recordBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, encodingBitRate);  //audioRecord能接受的最小的buffer大小

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, encodingBitRate, recordBufSize);
        data = new byte[recordBufSize];
    }

    public static void StopRecording()
    {
        Log.d(TAG, "StopRecording");

        isRecording = false;

        if (null != audioRecord) {
            audioRecord.stop();
            //audioRecord.release();
            mRecordThread = null;
        }
    }

    static class RecordThread extends  Thread{

        public RecordThread(String name){
            super(name);
        }

        @Override
        public void run(){
            Log.d(TAG, "RecordThread Start.");
            FileOutputStream os = null;

            try {
                os = new FileOutputStream(filePath + filename);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (null != os) {
                while (isRecording) {
                    int read = audioRecord.read(data, 0, recordBufSize);

                    // 如果读取音频数据没有出现错误，就将数据写入到文件
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            os.write(data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void PcmTransformToWAV()
    {
        PcmTransformToWAV(filename);
    }

    public static void PcmTransformToWAV(String fileName)
    {
        Log.d(TAG, "PcmTransformToWAV, fileName:" + fileName);
        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(48000,    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        String pcmPath = "/storage/emulated/0/NUWA/assets/Voice/" + fileName;
        String wavPath =  "/storage/emulated/0/NUWA/assets/WAV/" + fileName +".wav";
        pcmToWavUtil.pcmToWav(pcmPath, wavPath);
    }
}