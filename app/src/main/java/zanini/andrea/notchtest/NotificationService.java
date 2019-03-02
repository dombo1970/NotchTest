package zanini.andrea.notchtest;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Icon;
import android.os.Environment;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class NotificationService extends NotificationListenerService {

    View oldOverlay;
    List<String> packages= new ArrayList<>();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Override
    public void onCreate() {
       if(checkNotificationListenerPermission()){
                Log.i("Enabled","Permission");
        }else{
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        if(Settings.canDrawOverlays(getBaseContext())){
            Log.i("Enabled","Permission");
        }else{
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        super.onCreate();
    }

    @Override
    public void onListenerConnected(){
        Log.d("LISTENER","CONNECTED");
        if(checkNotificationListenerPermission() & getActiveNotifications().length>0)
        initOverlay();
    }


    private boolean checkNotificationListenerPermission() {
        ComponentName cn = new ComponentName(this, NotificationService.class);
        String flat = Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(cn.flattenToString());
    }

    @Override
    public void onNotificationPosted (StatusBarNotification sbn) {
        try {
            if (sbn != null && sbn.getNotification() != null) {
                if(sbn.getPackageName().equals("com.fb.fluid") || sbn.getPackageName().equals("android") || sbn.getPackageName().equals("com.xiaomi.joyose")){
                    return;
                }
                if(packages.size()<=7 && !packages.contains(sbn.getPackageName())) {
                    packages.add(sbn.getPackageName());
                    writeLog("Ora: "+sbn.getPostTime()+ " - AGGIUNTO: "+ sbn.getPackageName());
                    addToOverlay(sbn);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onNotificationRemoved (StatusBarNotification sbn) {
        Log.i("onNotificationRemoved","removed:"+sbn.getPackageName());
        packages.remove(sbn.getPackageName());
        if (sbn.getNotification() != null) {
            if (!sbn.getPackageName().equals("android") && !sbn.getPackageName().equals("com.xiaomi.joyose") && !sbn.getPackageName().equals("com.fb.fluid")) {
                writeLog("Ora: "+sbn.getPostTime()+ " - RIMOSSO: "+ sbn.getPackageName());
                reDrawOverlay();
            }
        }

    }
    /*
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("DESTROYING","CALLING RECEIVER");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, ServiceStarter.class);
        this.sendBroadcast(broadcastIntent);
    }*/


    void addToOverlay(StatusBarNotification sbn){
        Log.d("ADDING","Normal Icon");
        ImageView imageView=createNotificationImage(sbn.getNotification().getSmallIcon());
        LinearLayout overlay=(LinearLayout)oldOverlay;
        overlay.addView(imageView);
        drawOverlay(overlay);

    }

    void reDrawOverlay(){
        Log.d("ADDING","Normal Icon");
        LinearLayout overlay=(LinearLayout)oldOverlay;
        packages.clear();
        overlay.removeAllViewsInLayout();
        for(StatusBarNotification n :getActiveNotifications()){
            Log.d("ANALYZING",n.getPackageName()+" - "+ n.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString());
            if (n.getPackageName().equals("com.xiaomi.joyose") ) {
                Log.d("SKIPPING","UnUseful notification");
                continue;
            }
            if(packages.contains(n.getPackageName())){
                Log.d("SKIPPING","Icon already in notch");
                continue;
            }else{
                packages.add(n.getPackageName());
            }
            ImageView image;
            if(packages.size()>7){
                Log.d("ADDING","More notification icon");
                image=createNotificationImage(Icon.createWithResource(getBaseContext(),R.drawable.ic_stat_name));
            }else {
                Log.d("ADDING","Normal Icon");
                image=createNotificationImage(n.getNotification().getSmallIcon());
            }
            try {
                ((LinearLayout) overlay).addView(image);
            } catch (Exception e) {
                Log.d("FANCULO", e.getMessage());
            }
            if(packages.size()>7){
                break;
            }
        }
        drawOverlay(overlay);
    }



    void initOverlay(){
        Log.d("CALLED","INIT");
        //DEFINING OVERLAY LAYOUT
        View overlay = new LinearLayout(this);
        LinearLayout.LayoutParams overlayParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        overlayParams.setMargins(10,-30,0,0);
        overlayParams.width=500;
        overlay.setLayoutParams(overlayParams);
        ((LinearLayout) overlay).setOrientation(LinearLayout.HORIZONTAL);

        //ADDING ACTIVE NOTIFICATIONS
        for(StatusBarNotification n :getActiveNotifications()){
            Log.d("ANALYZING",n.getPackageName()+" - "+ n.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString());
            if (n.getPackageName().equals("com.xiaomi.joyose") ) {
                Log.d("SKIPPING","UnUseful notification");
                continue;
            }
            if(packages.contains(n.getPackageName())){
                Log.d("SKIPPING","Icon already in notch");
                continue;
            }else{
                packages.add(n.getPackageName());
            }
            ImageView image;
            if(packages.size()>7){
                Log.d("ADDING","More notification icon");
                image=createNotificationImage(Icon.createWithResource(getBaseContext(),R.drawable.ic_stat_name));
            }else {
                Log.d("ADDING","Normal Icon");
                image=createNotificationImage(n.getNotification().getSmallIcon());
            }
            try {
                ((LinearLayout) overlay).addView(image);
            } catch (Exception e) {
                Log.d("FANCULO", e.getMessage());
            }
            if(packages.size()>7){
                break;
            }
        }
        drawOverlay(overlay);

    }

    ImageView createNotificationImage(Icon i){
        ImageView image = new ImageView(this);
        if (i != null)
            i.setTint(Color.WHITE);
        image.setLayoutParams(new LinearLayout.LayoutParams(32, 32));
        image.setPadding(2,5,2,2);
        image.setImageIcon(i);
        return image;
    }


    void drawOverlay(View overlay){
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE ,
                PixelFormat.TRANSLUCENT);
        params.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        params.gravity= Gravity.TOP | Gravity.LEFT;
        params.x=20;
        params.y=60;
        if (wm != null) {
            if(oldOverlay!=null){
                wm.removeViewImmediate(oldOverlay);
            }
            wm.addView(overlay , params);
            oldOverlay=overlay;
        }
    }


    private static FileOutputStream getfos(){
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File myDir = new File(root + "/zaniniLog");
        if (myDir.mkdir()) {
            Log.d("Cartella creata", "zaniniLog");
        } else {
            Log.d("Cartella non creata", myDir.toString());
        }

        File f = new File(myDir + "/notchTestLog.txt");
        if (!f.exists()) {
            Log.d(f.toString(), "Non esiste");
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            return new FileOutputStream(f, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void writeLog(String log){
        FileOutputStream fos=getfos();
        try {
            fos.write(log.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}
