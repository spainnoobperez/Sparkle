package com.sion.sparkle;

import android.view.SurfaceView;

import android.view.SurfaceHolder;
import android.view.Surface;

import android.view.WindowManager;
import android.view.Gravity;

// Check version
import android.os.Build;

import android.view.MotionEvent;
import android.view.KeyEvent;

// Check input source
import android.view.InputDevice;

import android.util.Log;

// Software keyboard
import android.view.inputmethod.InputMethodManager;

// Broadcast receiver
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent; // XXX Basic
import android.content.Context; // XXX Basic

// Orientation control
import android.util.DisplayMetrics;
import android.content.pm.ActivityInfo;


public class SparkleView extends SurfaceView implements SurfaceHolder.Callback
{
    SparkleView(SparkleService sparkle, long user)
    {
        super(sparkle);

        Log.i("Sparkle", "Constructing view...");

        this.sparkle_ = sparkle;
        this.user = user;

        int windowType;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        else
            windowType = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT; // WindowManager.LayoutParams.TYPE_PHONE;

        // WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        int flags = 0;
        flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL; // allow any pointer events outside of the window to be sent to the windows behind it
        flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        //flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, 0, 0,
                windowType,
                flags,
                0);
        params.gravity = Gravity.CENTER;
        params.setTitle("Sparkle");

        //setVisibility(INVISIBLE);
        setFocusableInTouchMode(true);
        getHolder().addCallback(this);


        params.x = 0;
        params.y = 0;
        params.width = 100;
        params.height = 100;


        setVisibility(VISIBLE);



        DisplayMetrics display_metrics = new DisplayMetrics();
        sparkle_.window_manager_.getDefaultDisplay().getMetrics(display_metrics);
        if (display_metrics.widthPixels > display_metrics.heightPixels)
            params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        else
            params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;


        enabled_ = false;
        rmb_ = false;


        receiver_ = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if (action.equals(sparkle_.ACTION_HIDE))
                {
                    Log.i("Sparkle", "Hide all (service)");
                    set_visible(false);
                }
                else if (action.equals(sparkle_.ACTION_SHOW))
                {
                    Log.i("Sparkle", "Show all (service)");
                    set_visible(true);
                }
            }
        };

    }

    public void set_enabled(boolean enabled)
    {
        if (enabled && !enabled_)
        {
            sparkle_.window_manager_.addView(this, this.params);

            IntentFilter filter = new IntentFilter();
            filter.addAction(sparkle_.ACTION_HIDE);
            filter.addAction(sparkle_.ACTION_SHOW);
            sparkle_.registerReceiver(receiver_, filter);

            enabled_ = true;
        }
        else if (!enabled && enabled_)
        {
            sparkle_.window_manager_.removeView(this);

            sparkle_.unregisterReceiver(receiver_);

            enabled_ = false;
        }
    }

    public void set_visible(boolean visible)
    {
        if (visible)
            setVisibility(VISIBLE);
        else
            setVisibility(INVISIBLE);
    }

    public void set_position(int x, int y)
    {
        params.x = x;
        params.y = y;
        if (enabled_)
            sparkle_.window_manager_.updateViewLayout(this, params);
    }

    public void set_size(int width, int height)
    {
        params.width = width;
        params.height = height;
        if (enabled_)
            sparkle_.window_manager_.updateViewLayout(this, params);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        Log.i("Sparkle", "Surface changed");
        if (user == 0) {return;}
        surface_changed(user, holder.getSurface());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.i("Sparkle", "Surface created");
        if (user == 0) {return;}
        surface_changed(user, holder.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.i("Sparkle", "Surface destroyed");
        if (user == 0) {return;}
        surface_changed(user, null);
    }

    public static boolean hasSource(int sources, int source)
    {
        return (sources & source) == source;
    }

    public boolean onMotionEvent(MotionEvent event)
    {
        if (user == 0) {return false;}

        int source = event.getSource();

        if (hasSource(source, InputDevice.SOURCE_TOUCHSCREEN))
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    if (!rmb_)
                        touch_down(user, 0, event.getX(), event.getY());
                    else
                    {
                        pointer_motion(user, event.getX(), event.getY());
                        pointer_button_down(user, 2);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!rmb_)
                        touch_up(user, 0, event.getX(), event.getY());
                    else
                    {
                        pointer_button_up(user, 2);
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    touch_motion(user, 0, event.getX(), event.getY());
                    return true;
            }
        }
        else if (hasSource(source, InputDevice.SOURCE_MOUSE))
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_BUTTON_PRESS:
                    pointer_button_down(user, event.getActionButton());
                    return true;
                case MotionEvent.ACTION_BUTTON_RELEASE:
                    pointer_button_up(user, event.getActionButton());
                    return true;
                case MotionEvent.ACTION_HOVER_MOVE:
                    pointer_motion(user, event.getX(), event.getY());
                    return true;
                case MotionEvent.ACTION_MOVE:
                    pointer_motion(user, event.getX(), event.getY());
                    return true;
            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (onMotionEvent(event))
            return true;
        else
            return super.onTouchEvent(event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event)
    {
        if (onMotionEvent(event))
            return true;
        else
            return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (user == 0) {return false;}

        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            set_visible(false); // XXX
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
        {
            InputMethodManager imm = (InputMethodManager)sparkle_.getSystemService(sparkle_.INPUT_METHOD_SERVICE);
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT); // XXX Move
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
        {
            rmb_ = true;
        }

        key_down(user, keyCode);

        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (user == 0) {return false;}

        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
        {
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
        {
            rmb_ = false;
        }

        key_up(user, keyCode);

        return true;
    }


    public native void surface_changed(long user, Surface surface);

    public native void key_down(long user, int code);
    public native void key_up(long user, int code);

    public native void pointer_button_down(long user, int button);
    public native void pointer_button_up(long user, int button);
    public native void pointer_motion(long user, float x, float y);
    public native void pointer_enter(long user);
    public native void pointer_leave(long user);

    public native void touch_down(long user, int id, float x, float y);
    public native void touch_up(long user, int id, float x, float y);
    public native void touch_motion(long user, int id, float x, float y);


    SparkleService sparkle_;
    long user = 0;
    WindowManager.LayoutParams params;
    boolean enabled_;
    boolean rmb_;
    BroadcastReceiver receiver_;
}
