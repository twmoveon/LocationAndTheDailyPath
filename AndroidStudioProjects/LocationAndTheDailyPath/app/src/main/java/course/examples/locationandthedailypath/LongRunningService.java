package course.examples.locationandthedailypath;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Date;



public class LongRunningService extends Service {
    Thread thread;
    boolean stopornot=false;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        thread= new Thread(new Runnable() {
            @Override
            public void run() {

                    Intent intent = new Intent();
                    intent.setAction("CHECK-IN");
                    intent.putExtra("KEY", 1);
                    sendBroadcast(intent);
                    Log.d("LongRunningService", "executed at " + new Date().
                            toString());}


        });
        thread.start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int fiveMinutes = 5 * 60 * 1000; // 5 minutes
        long triggerAtTime = SystemClock.elapsedRealtime() + fiveMinutes;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopornot=true;
    }
}
