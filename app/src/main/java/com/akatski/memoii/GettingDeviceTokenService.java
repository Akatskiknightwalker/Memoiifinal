package com.akatski.memoii;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.mbms.StreamingServiceInfo;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class GettingDeviceTokenService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {

        String deviceToken  = FirebaseInstanceId.getInstance().getToken();
        Log.d("devicetoken",deviceToken);
    }

}
