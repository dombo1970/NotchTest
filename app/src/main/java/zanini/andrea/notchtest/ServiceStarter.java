package zanini.andrea.notchtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;


public class ServiceStarter extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("RECEIVER","CALLED AND STARTING");

        if(Objects.equals(intent.getAction(), "android.intent.action.BOOT_COMPLETED") || Objects.equals(intent.getAction(),"restartservice")) {
            //Intent accessibility = new Intent(context, NotchAccessibilityService.class);
            //context.startService(accessibility);
            Intent notification = new Intent(context, NotificationService.class);
            context.startService(notification);
        }
    }
}
