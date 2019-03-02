package zanini.andrea.notchtest;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
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

    }

    @Override
    public void onInterrupt() {

    }
}
