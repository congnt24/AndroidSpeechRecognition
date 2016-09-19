package com.example.congnt24.androidspeechrecognitionservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {
    TextView tvResult;
    private int mBindFlag;
    private Messenger mServiceMessenger;
    private VisualizerView visualizerView;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        public static final boolean DEBUG = true;
        public static final String TAG = "ZXXX";

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (DEBUG) {
                Log.d(TAG, "onServiceConnected");
            } //$NON-NLS-1$

            mServiceMessenger = new Messenger(service);
//            Message msg = new Message();
//            msg.what = VoiceCommandService.MSG_RECOGNIZER_START_LISTENING;
//
//            try {
//                mServiceMessenger.send(msg);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG) {
                Log.d(TAG, "onServiceDisconnected");
            } //$NON-NLS-1$
            mServiceMessenger = null;
        }

    }; // mServiceConnection

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent2(String str){
        Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(byte[] bytes){
        Toast.makeText(MainActivity.this, "AAAAAAAAAAAAAA", Toast.LENGTH_SHORT).show();
        visualizerView.updateVisualizer(bytes);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_main);
        visualizerView = (VisualizerView) findViewById(R.id.visualizerview);
        tvResult = (TextView) findViewById(R.id.tv_result);
        Intent service = new Intent(this, VoiceCommandService.class);
        startService(service);
        mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;
        findViewById(R.id.btn01).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                onStartSpeech();
                EventBus.getDefault().post(true);
            }
        });
        findViewById(R.id.btn02).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                onStopSpeech();
                EventBus.getDefault().post(false);
            }
        });
    }

    private void onStartSpeech() {
        Message msg = new Message();
        msg.what = VoiceCommandService.MSG_RECOGNIZER_START_LISTENING;
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void onStopSpeech() {
        Message msg = new Message();
        msg.what = VoiceCommandService.MSG_RECOGNIZER_CANCEL;
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        bindService(new Intent(this, VoiceCommandService.class), mServiceConnection, mBindFlag);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();

        if (mServiceMessenger != null) {
            unbindService(mServiceConnection);
            mServiceMessenger = null;
        }
    }
}
