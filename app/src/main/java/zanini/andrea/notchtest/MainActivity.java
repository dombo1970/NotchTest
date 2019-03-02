package zanini.andrea.notchtest;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity {
    private final int PERMISSION_REQUEST_CODE = 123;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*if(!isAccessibilityServiceEnabled(getBaseContext(),NotchAccessibilityService.class)){
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        startService(new Intent(getBaseContext(),NotchAccessibilityService.class));
*/
        startService(new Intent(getBaseContext(),NotificationService.class));


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNotificationChannel();
               Notification n=new NotificationCompat.Builder(getApplicationContext(),"TEST")
                       .setSmallIcon(R.drawable.ic_stat_name)
                       .setCategory(Notification.CATEGORY_EVENT)
                       .setContentTitle("TESTTESTTEST")
                       .setContentText("PROVAPROVAPROVA")
                       .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                       .build();
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

// notificationId is a unique int for each notification that you must define
                notificationManager.notify(123456789, n);
            }
        }
        );
        if (!checkPermission()){
            requestPermission();
        }
    }



    public  boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> service) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(context.getPackageName()) && enabledServiceInfo.name.equals(service.getName()))
                return true;
        }

        return false;
    }

private void createNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
        CharSequence name = "TEST";
        String description = "TEST";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("TEST", name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }

}

    @Override
    public void onDestroy() {
        Log.d("DESTROYING","CALLING RECEIVER");

        Intent broadcastIntent = new Intent(this,ServiceStarter.class);
        broadcastIntent.setAction("restartservice");
        super.onDestroy();

        this.sendBroadcast(broadcastIntent);

    }
    @TargetApi(23)
    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        return (result == PackageManager.PERMISSION_GRANTED);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    Toast.makeText(this,"Permesso all'archiviazione necessario per il corretto funzionamento.",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    @TargetApi(23)
    private void requestPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){

            Toast.makeText(getApplicationContext(),"Need to read external storage.",Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);

        } else {

            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
        }
    }
}