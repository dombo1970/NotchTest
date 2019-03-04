package zanini.andrea.notchtest;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.List;


public class NotchAccessibilityService extends AccessibilityService {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Accessibility","CREATED");
        return START_STICKY;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.d("ACCESSIBILITY", String.valueOf(accessibilityEvent.getAction()));
        Log.d(String.valueOf(accessibilityEvent.getEventType()),String.valueOf(accessibilityEvent.isFullScreen()));
        AccessibilityNodeInfo mSource = accessibilityEvent.getSource();
        int id=accessibilityEvent.getWindowId();
        Log.d("ID", String.valueOf(id));
        accessibilityEvent.getWindowChanges();
try {
    int child = mSource.getChildCount();
    // iterate through all child of parent view
    for (int i = 0; i < child; i++) {
        AccessibilityNodeInfo childNodeView = mSource.getChild(i);
        Log.d("FINESTRA", childNodeView.toString());

        Log.d("FIGLI", String.valueOf(childNodeView.getChildCount()));
        if( childNodeView.getClassName().equals("android.widget.FrameLayout") && childNodeView.getPackageName().equals("com.android.systemui")){
            int deepcount=childNodeView.getChildCount();
            for(int d=0;d<deepcount;d++) {
                Log.d(childNodeView.getChild(d).toString(), String.valueOf(childNodeView.getChild(d).getChildCount()));
            }
        }
        // Do something with this window content
    }

}catch (Exception e){
    Log.d("ERRORE",e.getMessage());
}
    }

    @Override
    public void onInterrupt() {

    }
}
