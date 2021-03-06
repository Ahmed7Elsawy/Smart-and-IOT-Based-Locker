package com.IOT.fingerprint.data.database;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class DeviceFirebaseInsertUpdateDB {

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final FirebaseUser userData = mAuth.getCurrentUser();
    private static final DatabaseReference REF =
            FirebaseDatabase.getInstance().getReference();

    public static void updateDeviceState(final Context mContext, final String key, final String userID, final String state) {
        REF.child("Devices").child(key).child("state").setValue(state);
        HistoryFirebaseInsertDB.InsertNewHistory(mContext, key,userID, state);
    }

    public static void insertNewDevice(String myUsername, String deviceName, String userID, String deviceType, String devicePhone, boolean isNewDevice, String existDeviceKey) {

        String key;
        if (!isNewDevice) {
            key = existDeviceKey;
        } else {
            key = REF.child("Devices").push().getKey();
        }

        Map<String, String> deviceInfo = new HashMap<>();
        deviceInfo.put("type", deviceType);
        deviceInfo.put("phoneNumber", devicePhone);
        deviceInfo.put("key", key);
        deviceInfo.put("state", "OFF");

        Map<String, String> userDevice = new HashMap<>();
        userDevice.put("deviceName", deviceName);
        userDevice.put("userID", userID);
        REF.child("userDevices").child(userData.getUid()).child(key).setValue(userDevice);

        REF.child("deviceUserId").child(key).child(userID).setValue(myUsername);

        if (isNewDevice)
            REF.child("Devices").child(key).setValue(deviceInfo);

    }

}