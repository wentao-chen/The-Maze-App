package com.gmail.wentaochen97.themazeapp;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Wentao-Admin on 2016-01-07.
 */
class MainThread {

    private SurfaceHolder surfaceHolder;
    private DrawPanel gamePanel;
    private final AtomicBoolean RUNNING = new AtomicBoolean(false);

    public MainThread(SurfaceHolder surfaceHolder, DrawPanel gamePanel) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }

    public void start() {

        setRunning(true);

        long time = System.nanoTime();
        while (RUNNING.get()) {
            long timeElapsed = System.nanoTime() - time;
            time = System.nanoTime();
            Canvas canvas = null;
            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (this.surfaceHolder) {
                    this.gamePanel.update(timeElapsed * 0.000000001);
                    this.gamePanel.draw(canvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (canvas != null) {
                try {
                    this.surfaceHolder.unlockCanvasAndPost(canvas);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isRunning() {
        return RUNNING.get();
    }

    public void setRunning(boolean running) {
        RUNNING.set(running);
    }

}
