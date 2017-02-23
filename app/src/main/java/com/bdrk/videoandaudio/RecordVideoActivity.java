package com.bdrk.videoandaudio;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 5u51_5 on 2016/12/14.
 */
public class RecordVideoActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "MainActivity";
    private SurfaceView mSurfaceview;
    private ImageView mBtnStartStop;
    private boolean mStartedFlg = false;
    private MediaRecorder mRecorder;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private String path = "";
    private int recordTime = 0;
    private LinearLayout llTime;
    private TextView tvTime;
    private TimerTask recordTask;
    private ProgressBar pbRecord;
    private int progress = 0;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (progress == 240) {
                    if (mRecorder != null) {
                        mRecorder.stop();
                        mRecorder.reset();
                    }
                    if (recordTask != null) {
                        recordTask.cancel();
                    }
                    Intent intent = new Intent();
                    intent.putExtra("path", path);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                pbRecord.setProgress(progress);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.video2);
        initView();
        mBtnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starRecordVideo();
            }
        });
    }

    /**
     * 初始化控件以及录制视频的分辨率
     */
    private void initView() {
        mSurfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        llTime = (LinearLayout) findViewById(R.id.ll_time);
        tvTime = (TextView) findViewById(R.id.tv_time);
        pbRecord = (ProgressBar) findViewById(R.id.progressBar);
        pbRecord.setMax(240);//设置录制最大时间为120s
        mSurfaceHolder = mSurfaceview.getHolder();
        //设置屏幕分辨率
        mSurfaceHolder.setFixedSize(640, 480);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        mBtnStartStop = (ImageView) findViewById(R.id.btnStartStop);
        SurfaceHolder holder = mSurfaceview.getHolder();// 取得holder
        holder.addCallback(this); // holder加入回调接口
        // setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    private void starRecordVideo() {
        if (!mStartedFlg) {
            // Start
            if (mRecorder == null) {
                mRecorder = new MediaRecorder(); // Create MediaRecorder
            }
            try {
                /**
                 * 解锁camera
                 * 设置输出格式为mpeg_4（mp4），此格式音频编码格式必须为AAC否则网页无法播放
                 */
                mCamera.unlock();
                mRecorder.setCamera(mCamera);
                mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                //音频编码格式对应应为AAC
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                //视频编码格式对应应为H264
                mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                mRecorder.setVideoSize(640, 480);
                mRecorder.setVideoEncodingBitRate(600 * 1024);
                mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

                /**
                 * 设置输出地址
                 */
                String sdPath = getSDPath();
                if (sdPath != null) {
                    File dir = new File(sdPath + "/VideoAndAudio");
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    path = dir + "/" + getDate() + ".mp4";

                    mRecorder.setOutputFile(path);
                    mRecorder.setOrientationHint(90);
                    mRecorder.prepare();
                    mRecorder.start();   // Recording is now started
                    llTime.setVisibility(View.VISIBLE);
                    pbRecord.setVisibility(View.VISIBLE);
                    starRecordTimer();
                    mStartedFlg = true;
                    updateProgress();
                    mBtnStartStop.setImageResource(R.mipmap.pause);
                }
            } catch (Exception e) {
                /**
                 * 当用户拒绝录音权限会执行这里
                 */
                Toast.makeText(RecordVideoActivity.this, "没有录音权限", Toast.LENGTH_SHORT).show();
                finish();
            }

        } else {
            if (mStartedFlg) {
                try {
                    mRecorder.stop();
                    if (recordTask != null) {
                        recordTask.cancel();
                    }
                    mRecorder.reset();
                    mStartedFlg = false;
                    Toast.makeText(RecordVideoActivity.this, "录制完成" + "视频地址:" + path, Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e) {
                    Toast.makeText(RecordVideoActivity.this, "录制失败", Toast.LENGTH_SHORT).show();
                }
            }
            mStartedFlg = false; // Set button status flag
        }
    }

    private void updateProgress() {
        /**
         * 进度条线程
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (progress < 240) {
                    if (mStartedFlg) {
                        progress++;
                        Log.e("ssd", progress + "");
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
            }
        }).start();
    }

    /**
     * 开启计时
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

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
        mSurfaceHolder = holder;
        startPreView(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        // surfaceDestroyed的时候同时对象设置为null
        mSurfaceview = null;
        mSurfaceHolder = null;
        if (mRecorder != null) {
            mRecorder.release(); // Now the object cannot be reused
            mRecorder = null;
        }
    }

    /**
     * 开启预览
     *
     * @param holder
     */
    private void startPreView(SurfaceHolder holder) {
        try {
            if (mCamera == null) {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
            if (mRecorder == null) {
                mRecorder = new MediaRecorder();
            }
            if (mRecorder != null) {
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(holder);
                Camera.Parameters parameters = mCamera.getParameters();
                /**
                 * Camera自动对焦
                 */
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes != null) {
                    for (String mode : focusModes) {
                        mode.contains("continuous-video");
                        parameters.setFocusMode("continuous-video");
                    }
                }
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            }
        } catch (Exception e) {
            /**
             * 用户拒绝录像权限
             */
            Toast.makeText(RecordVideoActivity.this, "用户拒绝了录像权限", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 获取系统时间
     *
     * @return
     */
    public static String getDate() {
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);           // 获取年份
        int month = ca.get(Calendar.MONTH);         // 获取月份
        int day = ca.get(Calendar.DATE);            // 获取日
        int minute = ca.get(Calendar.MINUTE);       // 分
        int hour = ca.get(Calendar.HOUR);           // 小时
        int second = ca.get(Calendar.SECOND);       // 秒
        String date = "" + year + (month + 1) + day + hour + minute + second;
        Log.d(TAG, "date:" + date);

        return date;
    }

    /**
     * 获取SD path
     *
     * @return
     */
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        }

        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        llTime.setVisibility(View.GONE);
        recordTime = 0;
        tvTime.setText("00.00");
        mStartedFlg = false;
        mBtnStartStop.setImageResource(R.mipmap.pause);
        if (recordTask != null) {
            recordTask.cancel();
        }
        // 如果正在使用MediaRecorder，首先需要释放它。
        releaseMediaRecorder();
        // 在暂停事件中立即释放摄像头
        releaseCamera();
    }


    private void releaseMediaRecorder() {
        if (mRecorder != null) {
            // 清除recorder配置
            mRecorder.reset();
            // 释放recorder对象
            mRecorder.release();
            mRecorder = null;
            // 为后续使用锁定摄像头
            mCamera.lock();
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            // 为其它应用释放摄像头
            mCamera.release();
            mCamera = null;
        }
    }


}
