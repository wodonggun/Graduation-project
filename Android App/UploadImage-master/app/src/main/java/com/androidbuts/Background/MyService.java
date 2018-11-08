package com.androidbuts.Background;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.FileObserver;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class MyService extends Service {


    private FileObserver observer;
    static final String downpath = "storage/emulated/0/DCIM/MiDrone";


    @Nullable

    @Override
    public IBinder onBind(Intent intent) {
        // Service 객체와 (화면단 Activity 사이에서)
        // 통신(데이터를 주고받을) 때 사용하는 메서드
        // 데이터를 전달할 필요가 없으면 return null;
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // 서비스에서 가장 먼저 호출됨(최초에 한번만)
        Log.d("test", "서비스의 onCreate");
       // mp = MediaPlayer.create(this, R.raw.shapeofyou);
       // mp.setLooping(false); // 반복재생





    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        observer = new FileObserver(downpath) {
            @Override
            public void onEvent(int event, String path) {
                Log.e("event발생","num: "+event);
                event &= FileObserver.ALL_EVENTS;
                if(event == FileObserver.CREATE){
                    Log.e("sibal","File Changed --> Path = " + downpath + "/" + path);
                    Log.e("sibal","File Changed --> Event = " + event);
                }
            }
        };
        observer.startWatching();


        // 서비스가 호출될 때마다 실행
        Log.d("test", "서비스의 onStartCommand");
     //   mp.start(); // 노래 시작
        observer.startWatching();


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 실행
      //  mp.stop(); // 음악 종료
        Log.d("test", "서비스의 onDestroy");
        observer.stopWatching();
    }

}
