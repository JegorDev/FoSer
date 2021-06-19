package pollub.ism.foser;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.Timer;
import java.util.TimerTask;

public class MyForegroundService extends Service {

    public static final String CHANNEL_ID = "MyForegroundServiceChannel";
    public static final String CHANNEL_NAME = "FoSer service channel";

    public static final String MESSAGE = "message";
    public static final String TIME = "time";
    public static final String WORK = "work";
    public static final String WORK_DOUBLE = "work_double";

    private String message;
    private Boolean showTime;
    private Boolean doWork;
    private Boolean doubleSpeed;
    private final long period = 2000;

    private Context ctx;
    private Intent notificationIntent;
    private PendingIntent pendingIntent;

    private int counter;
    private Timer timer;
    private TimerTask timerTask;
    final Handler handler = new Handler();

    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Notification notification = new Notification.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_my_icon)
                    .setContentTitle(getString(R.string.ser_title))
                    .setShowWhen(showTime)
                    .setContentText(message + " " + String.valueOf(counter))
                    .setLargeIcon(BitmapFactory.decodeResource (getResources() , R.drawable.circle ))
                    .setContentIntent(pendingIntent)
                    .build();

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.notify(1,notification);
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();

        ctx = this;
        notificationIntent = new Intent(ctx, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);

        counter = 0;

        timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                counter++;
                handler.post(runnable);
            }
        };
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        timer.cancel();
        timer.purge();
        timer = null;
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        message = intent.getStringExtra(MESSAGE);
        showTime = intent.getBooleanExtra(TIME,false);
        doWork = intent.getBooleanExtra(WORK,false);
        doubleSpeed = intent.getBooleanExtra(WORK_DOUBLE,false);

        createNotificationChannel();

        Notification notification = new Notification.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_my_icon)
                .setContentTitle(getString(R.string.ser_title))
                .setShowWhen(showTime)
                .setContentText(message)
                .setLargeIcon(BitmapFactory.decodeResource (getResources() , R.drawable.circle ))
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1,notification);

        doWork();

        return START_NOT_STICKY;
    }

    private void doWork() {
//        try {
//            Thread.sleep(5000);
//
//        } catch (Exception e) {
//
//        }
        //FIXME
        //Eksperyment: Następuje blokowanie MainActivity bo sleep blokuje wątek.Dotyczy to jednak tylko tej konkretnej aplikacji,nie blokuje więc rozwijania paska

        if(doWork) {
            timer.schedule(timerTask, 0L, doubleSpeed ? period / 2L : period);
        }

        String info = "Start working..."
                +"\n showTime=" + showTime.toString()
                +"\n doWork=" + doWork.toString()
                +"\n doubleSpeed=" + doubleSpeed.toString();

        Toast.makeText(this, info ,Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }
}