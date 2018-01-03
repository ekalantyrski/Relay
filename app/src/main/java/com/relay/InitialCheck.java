package com.relay;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;


public class InitialCheck extends AppCompatActivity {
    private Receiver r;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_uuidcheck);
        requestPermissions();




        SmsManager manager = SmsManager.getDefault(); // used to sendInfo SMS


        Network n = new Network(this, new SmsSender(manager));
        Thread networkThread = new Thread(n);
        networkThread.start();
        r = new Receiver(n);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(r, filter);

    }



//    public void loadData()
//    {
//
//        try
//        {
//            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
//            Bundle bundle = ai.metaData;
//            String key = bundle.getString("my_test_data");
//            System.out.println(key);
//        }
//
//        catch(Exception e)
//        {
//            Log.e("Bad", "Something");
//        }
//
//
//
//    }

    //This method requests necessary permissions for the app to work.
    public boolean requestPermissions()
    {
        String[] permissions = null;
        try { // this code gets all the permissions that were requested from the manifest
            permissions = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS)
                    .requestedPermissions;
        }catch(PackageManager.NameNotFoundException e)
        {
            Log.e("Permissions", "Pkg name not found");
            return false;
        }

        for(int i = 0; i < permissions.length; i++) //for each permission, checks if already granted, if not it will ask for that permission
        {
            String permission = permissions[i];
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if(permissionCheck == PackageManager.PERMISSION_DENIED)
            {
                String[] list = new String[1];
                list[0] = permissions[i];
                ActivityCompat.requestPermissions(this, list , 1);
            }
        }

        return true;
    }


    @Override
    //This code unregisters receiver once program is done
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(r);
    }
}
