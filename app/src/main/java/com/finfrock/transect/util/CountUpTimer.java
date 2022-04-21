package com.finfrock.transect.util;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public abstract class CountUpTimer {

    private final long interval;
    private long base;

    public CountUpTimer(long interval) {
        this.interval = interval;
    }

    public void start() {
        start(secondsSinceEpoch());
    }

    public void start(long startBase) {
        base = startBase;
        handler.sendMessage(handler.obtainMessage(MSG));
    }

    public void stop() {
        handler.removeMessages(MSG);
    }

    abstract public void onTick(long elapsedTime);

    private static final int MSG = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (CountUpTimer.this) {
                long elapsedTime = secondsSinceEpoch() - base;
                onTick(elapsedTime);
                sendMessageDelayed(obtainMessage(MSG), interval);
            }
        }
    };

    private Long secondsSinceEpoch(){
        return LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
    }
}

