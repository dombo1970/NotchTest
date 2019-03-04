package zanini.andrea.notchtest;

import android.app.Notification;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Icon;
import android.os.Environment;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
    int color=Color.WHITE;
    boolean fullscreen=false;

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
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        LinearLayout overlay=(LinearLayout) getOverlay(wm.getDefaultDisplay().getRotation());
        packages.clear();
        try {
            overlay.removeAllViewsInLayout();
        }catch(Exception e){
            Log.d("SKIPPING","No Views to remove");
        }
        for(StatusBarNotification n :getActiveNotifications()) {
            try {
                Log.d("ANALYZING", n.getPackageName() + " - " + n.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString());
                if (n.getPackageName().equals("com.xiaomi.joyose")) {
                    Log.d("SKIPPING", "UnUseful notification");
                    continue;
                }
                if (packages.contains(n.getPackageName())) {
                    Log.d("SKIPPING", "Icon already in notch");
                    continue;
                } else {
                    packages.add(n.getPackageName());
                }
                ImageView image;
                if (packages.size() > 7) {
                    Log.d("ADDING", "More notification icon");
                    image = createNotificationImage(Icon.createWithResource(getBaseContext(), R.drawable.ic_stat_name));
                } else {
                    Log.d("ADDING", "Normal Icon");
                    image = createNotificationImage(n.getNotification().getSmallIcon());
                }
                try {
                    overlay.addView(image);
                } catch (Exception e) {
                    Log.d("FANCULO", e.getMessage());
                }
                if (packages.size() > 7) {
                    break;
                }

            } catch (Exception e) {
                writeLog("ERROR caused by: " + e.getMessage());
            }
        }
        drawOverlay(overlay);
    }


    View getOverlay(int rotation){
        View overlay = new LinearLayout(this);
        LinearLayout.LayoutParams overlayParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        overlay.setLayoutParams(overlayParams);
        ((LinearLayout) overlay).setOrientation((rotation==Surface.ROTATION_0 || rotation==Surface.ROTATION_180 )?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);
        return overlay;
    }



    void initOverlay(){
        Log.d("CALLED","INIT");
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        //DEFINING OVERLAY LAYOUT
        View overlay= null;
        if (wm != null) {
            overlay = getOverlay(wm.getDefaultDisplay().getRotation());
        }
        //ADDING ACTIVE NOTIFICATIONS

            for (StatusBarNotification n : getActiveNotifications()) {
                try {
                Log.d("ANALYZING", n.getPackageName() + " - " + n.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString());
                if (n.getPackageName().equals("com.xiaomi.joyose")) {
                    Log.d("SKIPPING", "UnUseful notification");
                    continue;
                }
                if (packages.contains(n.getPackageName())) {
                    Log.d("SKIPPING", "Icon already in notch");
                    continue;
                } else {
                    packages.add(n.getPackageName());
                }
                ImageView image;
                if (packages.size() > 7) {
                    Log.d("ADDING", "More notification icon");
                    image = createNotificationImage(Icon.createWithResource(getBaseContext(), R.drawable.ic_stat_name));
                } else {
                    Log.d("ADDING", "Normal Icon");
                    image = createNotificationImage(n.getNotification().getSmallIcon());
                }
                try {
                    ((LinearLayout) overlay).addView(image);
                } catch (Exception e) {
                    Log.d("FANCULO", e.getMessage());
                }
                if (packages.size() > 7) {
                    break;
                }
                }catch (Exception e){
                    writeLog("ERROR caused by: "+ e.getMessage());
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
/*

    public class UpdateConditionsReceiver extends BroadcastReceiver {
        public String ROTATION_UPDATE="zanini.andrea.ROTATION_UPDATE";
        public String FULLSCREEN_UPDATE="zanini.andrea.FULLSCREEN_UPDATE";
        public String COLOR_CHANGE="zanini.andrea.COLOR_CHANGE";

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ROTATION_UPDATE)) {
                reDrawOverlay();

            }else if(intent.getAction().equals(FULLSCREEN_UPDATE)){
                return;
            }else if(intent.getAction().equals(COLOR_CHANGE)){
                color=(color==Color.WHITE)?Color.BLACK:Color.WHITE;
            }

        }
    }
*/


}
