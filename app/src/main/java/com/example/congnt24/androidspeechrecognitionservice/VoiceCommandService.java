package com.example.congnt24.androidspeechrecognitionservice;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class VoiceCommandService extends Service {

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;
    private static final String TAG = "SpeechRecognitionListener";
    private static final String LOG_TAG = "LOGTAG";
    private static final int FOREGROUND_FLAGS = 101;
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));
    protected AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected boolean mIsListening;
    protected volatile boolean mIsCountDownOn;
    private Notification notification;

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        setStreamMute(true);

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        notification = new NotificationCompat.Builder(this)
                .setTicker("Listening for you")
                .setContentTitle("Listening")
                .setContentText("Speech")
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
                .build();
    }

    public void setStreamMute(boolean isMute) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC)) {
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC
                        , isMute ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_MUTE, 0);
            }
        } else {
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, isMute);
        }
    }

    public void startListening() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // turn off beep sound
            mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }
        if (!mIsListening) {
            startForeground(101,
                    notification);
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            mIsListening = true;
            Log.d(TAG, "message start listening aa"); //$NON-NLS-1$
        }
    }

    public void stopListening() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH);
        } else {
            stopForeground(true);
        }
        mSpeechRecognizer.cancel();
        mIsListening = false;
        Log.d(TAG, "message canceled recognizer aaaaaaaaaaaaaa"); //$NON-NLS-1$
    }

    @Subscribe
    public void onEvent(Boolean startOrstop) {
        if (startOrstop) {
            startListening();
        } else {
            stopListening();
        }
    }
//
//    // Count down timer for Jelly Bean work around
//    protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000) {
//
//        @Override
//        public void onTick(long millisUntilFinished) {
//            // TODO Auto-generated method stub
//
//        }
//
//        @Override
//        public void onFinish() {
//            mIsCountDownOn = false;
//            Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
//            try {
//                mServerMessenger.send(message);
//                message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
//                mServerMessenger.send(message);
//            } catch (RemoteException e) {
//
//            }
//        }
//    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        setStreamMute(false);
//        if (mIsCountDownOn) {
//            mNoSpeechCountDown.cancel();
//        }
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");  //$NON-NLS-1$

        return mServerMessenger.getBinder();
    }

    protected static class IncomingHandler extends Handler {
        private WeakReference<VoiceCommandService> mtarget;

        IncomingHandler(VoiceCommandService target) {
            mtarget = new WeakReference<>(target);
        }


        @Override
        public void handleMessage(Message msg) {
            final VoiceCommandService target = mtarget.get();

            switch (msg.what) {
                case MSG_RECOGNIZER_START_LISTENING:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        // turn off beep sound
                        target.mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
                    }
                    if (!target.mIsListening) {
                        target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                        target.mIsListening = true;
                        Log.d(TAG, "message start listening"); //$NON-NLS-1$
                    }
                    break;

                case MSG_RECOGNIZER_CANCEL:
                    target.mSpeechRecognizer.cancel();
                    target.mIsListening = false;
                    Log.d(TAG, "message canceled recognizer"); //$NON-NLS-1$
                    break;
            }
        }
    }

    protected class SpeechRecognitionListener implements RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {
            Log.i(LOG_TAG, "onBeginningOfSpeech");
            // speech input will be processed, so there is no need for count down anymore
            if (mIsCountDownOn) {
                mIsCountDownOn = false;
//                mNoSpeechCountDown.cancel();
            }
            //Log.d(TAG, "onBeginingOfSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.i(LOG_TAG, "onBufferReceived: " + buffer);
            EventBus.getDefault().post(buffer);
        }

        @Override
        public void onEndOfSpeech() {
            Log.i(LOG_TAG, "onEndOfSpeech");
        }

        @Override
        public void onError(int error) {
            String errorMessage = getErrorText(error);
            Log.d(LOG_TAG, "FAILED " + errorMessage);
            if (mIsCountDownOn) {
                mIsCountDownOn = false;
//                mNoSpeechCountDown.cancel();
            }
            mIsListening = false;
            Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
            try {
                mServerMessenger.send(message);
            } catch (RemoteException e) {

            }
            //Log.d(TAG, "error = " + error); //$NON-NLS-1$
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.i(LOG_TAG, "onEvent");
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.i(LOG_TAG, "onPartialResults");
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mIsCountDownOn = true;
//                mNoSpeechCountDown.start();
                mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
            }
            Log.d(TAG, "onReadyForSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onResults(Bundle bundle) {

            ArrayList<String> matches = bundle
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String text = "";
            for (String result : matches)
                text += result + "\n";
            EventBus.getDefault().post("" + text);
            mSpeechRecognizer.cancel();
            mIsListening = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                // turn off beep sound
                mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
            }
            if (!mIsListening) {
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                mIsListening = true;
                Log.d(TAG, "message start listening"); //$NON-NLS-1$
            }
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            if (mIsListening) {
                Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
                EventBus.getDefault().post(rmsdB+3);
            }
        }

    }
}