package com.bdrk.videoandaudio;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

/**
 * Created by 5u51_5 on 2016/12/14.
 */
public class RecordAudioActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView ivMusic;
    TextView tvCancle;
    TextView tvFinish;
    TextView tvTime;
    ImageView tvStar;
    TextView tvTip;
    ProgressBar progressBar;


    private File file;
    private boolean isRecord = false;
    private MediaRecorder mRecorder;

    private TimerTask recordTask;
    private int recordTime;
    private int progress = 0;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (progress == 240) {
                tvStar.setVisibility(View.INVISIBLE);
                tvFinish.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                recordTime = 0;
                isRecord = false;
                stopRecording();
            }
            progressBar.setProgress(progress);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);
        initView();


    }

    private void initView() {
        ivMusic = (ImageView) findViewById(R.id.iv_music);
        tvCancle = (TextView) findViewById(R.id.tv_cancle);
        tvFinish = (TextView) findViewById(R.id.tv_finish);
        tvTime = (TextView) findViewById(R.id.tv_time);
        tvStar = (ImageView) findViewById(R.id.tv_star);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvTip = (TextView) findViewById(R.id.tv_tip);

        tvCancle.setOnClickListener(this);
        tvStar.setOnClickListener(this);
        tvFinish.setOnClickListener(this);

        progressBar.setMax(240);
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancle:
                stopRecording();
                finish();
                break;
            case R.id.tv_star:
                if (!isRecord) {
                    try {
                        tvTime.setText("00.00");
                        Date date = new Date();
                        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
                        final String path = Environment.getExternalStorageDirectory() + "/VideoAndAudio";
                        File filedir = new File(path);
                        if (!filedir.exists()) {
                            filedir.mkdir();
                        }
                        String time = f.format(date);
                        file = new File(path + "/" + time + ".amr");

                        mRecorder = new MediaRecorder();
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                        mRecorder.setAudioSamplingRate(11025);
                        mRecorder.setOutputFile(file.getAbsolutePath());
                        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        mRecorder.prepare();
                        mRecorder.start();

                        tvStar.setImageResource(R.mipmap.pause);
                        starRecordTimer();
                        Thread t = new Thread(mPollTask);
                        t.start();
                        isRecord = true;

                        progressBar.setVisibility(View.VISIBLE);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (progress < 240)
                                    if (isRecord) {
                                        progress++;
                                        try {
                                            Thread.sleep(500);
                                            handler.sendEmptyMessage(1);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        break;
                                    }
                            }
                        }).start();
                    } catch (Exception e) {
                        /**
                         * 录音权限被拒绝
                         */
                        Toast.makeText(RecordAudioActivity.this, "没有录音权限", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    if (recordTime < 2) {
                        Toast.makeText(RecordAudioActivity.this, "音频不能少于2秒", Toast.LENGTH_SHORT).show();
                        tvStar.setImageResource(R.mipmap.recording);
                        recordTime = 0;
                        progress = 0;
                    } else {
                        tvStar.setVisibility(View.INVISIBLE);
                    }
                    progressBar.setVisibility(View.GONE);
                    isRecord = false;
                    stopRecording();
                }
                break;
            case R.id.tv_finish:
                if (recordTime < 2) {
                    /**
                     * 时间少于2秒
                     */
                    Toast.makeText(RecordAudioActivity.this, "音频不能少于2秒", Toast.LENGTH_SHORT).show();
                    tvTime.setText("00.00");
                    tvStar.setImageResource(R.mipmap.recording);
                    recordTime = 0;
                    progress = 0;
                } else {
                    /**
                     * 录音成功
                     * 将amr格式转码成MP3
                     */
                    tvTip.setVisibility(View.VISIBLE);

                    tvStar.setVisibility(View.INVISIBLE);

                    /**
                     * 转码库,sdk版本不能小于16
                     */
                    IConvertCallback callback = new IConvertCallback() {
                        @Override
                        public void onSuccess(File convertedFile) {
                            tvTip.setVisibility(View.GONE);
                            if (convertedFile != null) {
                                Toast.makeText(RecordAudioActivity.this, "音频转码完成,地址为:" + convertedFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(RecordAudioActivity.this, "找不到音频文件", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Exception error) {
                            Log.e("sddd", error.toString());
                            Toast.makeText(RecordAudioActivity.this, "音频转码失败", Toast.LENGTH_SHORT).show();
                        }
                    };
                    AndroidAudioConverter.with(this)
                            //设置需转码音频路径
                            .setFile(file)
                            // 转码格式
                            .setFormat(AudioFormat.MP3)
                            // 接口回调
                            .setCallback(callback)
                            // 开始转码
                            .convert();
                }
                progressBar.setVisibility(View.GONE);
                isRecord = false;
                stopRecording();


                break;
        }
    }

    private void stopRecording() {
        mHandler.removeCallbacks(mPollTask);
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
        if (recordTask != null) {
            recordTask.cancel();
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopRecording();
    }

    /**
     * 开始录音计时
     */
    private void starRecordTimer() {
        recordTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recordTime++;
                        int m = recordTime / 60;
                        int s = recordTime % 60;
                        String strm = m + "";
                        String strs = s + "";
                        if (m < 10) {
                            strm = "0" + m;
                        }
                        if (s < 10) {
                            strs = "0" + s;
                        }
                        tvTime.setText(strm + ":" + strs);
                    }
                });
            }
        };
        Timer recordTimer = new Timer(true);
        recordTimer.schedule(recordTask, 0, 1000);
    }


    private Runnable mPollTask = new Runnable() {
        public void run() {
            final int mVolume = getVolume();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateVolume(mVolume);
                }
            });

            mHandler.postDelayed(mPollTask, 100);
        }
    };

    private void updateVolume(int volume) {
        switch (volume) {
            case 0:
                ivMusic.setImageResource(R.mipmap.mic_0);
                break;
            case 1:
                ivMusic.setImageResource(R.mipmap.mic_1);
                break;
            case 2:
                ivMusic.setImageResource(R.mipmap.mic_2);
                break;
            case 3:
                ivMusic.setImageResource(R.mipmap.mic_3);
                break;
            case 4:
                ivMusic.setImageResource(R.mipmap.mic_4);
                break;
            case 5:
                ivMusic.setImageResource(R.mipmap.mic_6);
                break;
            case 6:
                ivMusic.setImageResource(R.mipmap.mic_6);
                break;
            case 7:
                ivMusic.setImageResource(R.mipmap.mic_6);
                break;
            default:
                break;
        }
    }

    /**
     * 获取录音音量等级
     *
     * @return
     */
    public int getVolume() {
        int volume = 0;
        // 录音
        if (mRecorder != null) {
            volume = mRecorder.getMaxAmplitude() / 650;
            if (volume != 0)
                volume = (int) (10 * Math.log10(volume)) / 3;
        }
        return volume;
    }

    private final Handler mHandler = new Handler();


}
