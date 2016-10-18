package com.whut.danmudemo;

import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.VideoView;

import java.util.Random;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

public class MainActivity extends AppCompatActivity {

    private VideoView mVideoView;
    private final String mp4File = "/netease/cloudmusic/MV/梁静茹 - 问.mp4";

    private boolean showDanMu;
    private DanmakuView danmakuView;
    private DanmakuContext danmakuContext;

    // 编辑、发送弹幕控件
    private LinearLayout sendDanMu_layout;
    private Button sendDanMu;
    private EditText editDanMu;

    private BaseDanmakuParser parser = new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVideoView = (VideoView) findViewById(R.id.playVideo);
        mVideoView.setVideoPath(Environment.getExternalStorageDirectory() + mp4File);
        mVideoView.start();

        danmakuView = (DanmakuView) findViewById(R.id.danmuOfbilibili);
        danmakuView.enableDanmakuDrawingCache(true);
        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                showDanMu = true;
                danmakuView.start();
                generateSomeDanMu();
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });
        danmakuContext = DanmakuContext.create();
        danmakuView.prepare(parser, danmakuContext);

        sendDanMu_layout = (LinearLayout) findViewById(R.id.sendDanMu_layout);
        sendDanMu = (Button) findViewById(R.id.sendDanMu);
        editDanMu = (EditText) findViewById(R.id.edit_danmu);

        danmakuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendDanMu_layout.getVisibility() == View.GONE) {
                    sendDanMu_layout.setVisibility(View.VISIBLE);
                } else {
                    sendDanMu_layout.setVisibility(View.GONE);
                }
            }
        });

        sendDanMu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editDanMu.getText().toString();
                if (!TextUtils.isEmpty(content)) {
                    addDanMu(content, true);
                    editDanMu.setText("");
                }
            }
        });

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                            onWindowFocusChanged(true);
                        }
                    }
                }
        );
    }

    /**
     * 随机生成一些弹幕内容以供测试
     */
    private void generateSomeDanMu() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (showDanMu) {
                    int time = new Random().nextInt(300);
                    String content = "" + time + time;
                    addDanMu(content, false);
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 向弹幕View中添加一条弹幕
     *
     * @param content    弹幕的具体内容
     * @param withBorder 弹幕是否有边框
     */
    private void addDanMu(String content, boolean withBorder) {
        BaseDanmaku danmu = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmu.text = content;
        danmu.padding = 5;
        danmu.textSize = sp2px(20);
        danmu.textColor = Color.WHITE;
        danmu.setTime(danmakuView.getCurrentTime());
        if (withBorder) {
            danmu.borderColor = Color.RED;
        }
        danmakuView.addDanmaku(danmu);
    }

    /**
     * sp转px的方法
     *
     * @param spValue
     * @return
     */
    private int sp2px(int spValue) {
        float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (danmakuView != null && danmakuView.isPrepared()
                && danmakuView.isPaused()) {
            danmakuView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showDanMu = false;
        if (danmakuView != null) {
            danmakuView.release();
            danmakuView = null;
        }
    }
}
