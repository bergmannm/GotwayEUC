package app.gotway.euc.ble.profile;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

import app.gotway.euc.data.Data0x00;
import app.gotway.euc.share.SharePreference;
import app.gotway.euc.util.DebugLogger;

public class BleService extends Service implements BleManagerCallbacks {
    private static final long AUTO_CONN_PORID = 180000;
    private String mAddress;
    private Timer mAutoConn;
    private BleCore mBleCore;
    private BleManagerCallbacks mCallbacks;
    private BroadcastReceiver toothOpenReciver;

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    public BleService() {
        this.toothOpenReciver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                    int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                    if (state == BluetoothAdapter.STATE_ON) {
                        BleService.this.mBleCore.connect(BleService.this.getApplicationContext(), BleService.this.mAddress);
                    } else if (state == BluetoothAdapter.STATE_OFF) {
                        BleService.this.mBleCore.closeBluetoothGatt();
                    }
                }
            }
        };
    }

    public void onCreate() {
        super.onCreate();
        this.mBleCore = new BleCore();
        this.mBleCore.setGattCallbacks(this);
        registerReceiver(this.toothOpenReciver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
    }

    public void onDestroy() {
        super.onDestroy();
        DebugLogger.e("BleService", "service destroy");
        if (isConnected()) {
            this.mBleCore.disconnect();
        }
        unregisterReceiver(this.toothOpenReciver);
    }

    public IBinder onBind(Intent intent) {
        return getBinder();
    }

    private LocalBinder getBinder() {
        return new LocalBinder();
    }

    public boolean onUnbind(Intent intent) {
        this.mCallbacks = null;
        return true;
    }

    public void setBleCallBack(BleManagerCallbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public boolean isConnected() {
        return this.mBleCore.isConnected();
    }

    public void onDeviceConnected() {
        if (this.mCallbacks != null) {
            this.mCallbacks.onDeviceConnected();
        }
        if (this.mAutoConn != null) {
            this.mAutoConn.cancel();
            this.mAutoConn = null;
        }
    }

    public void onDeviceDisconnected() {
        if (this.mCallbacks != null) {
            this.mCallbacks.onDeviceDisconnected();
        }
    }

    public void onServicesDiscovered() {
        if (this.mCallbacks != null) {
            this.mCallbacks.onServicesDiscovered();
        }
    }

    public void onDeviceNotSupported() {
        if (this.mCallbacks != null) {
            this.mCallbacks.onDeviceNotSupported();
        }
    }

    public void onNotifyEnable() {
        if (this.mCallbacks != null) {
            this.mCallbacks.onNotifyEnable();
        }
    }

    public void onLinkLossOccur() {
        if (this.mCallbacks != null) {
            this.mCallbacks.onLinkLossOccur();
        }
        autoConn();
    }

    private void autoConn() {
        this.mAutoConn = new Timer();
        this.mAutoConn.schedule(new TimerTask() {
            public void run() {
                if (!BleService.this.mBleCore.isConnected()) {
                    BleService.this.mBleCore.disconnect();
                    BleService.this.mBleCore.connect(BleService.this.getApplicationContext(), BleService.this.mAddress);
                } else if (BleService.this.mAutoConn != null) {
                    BleService.this.mAutoConn.cancel();
                    BleService.this.mAutoConn = null;
                }
            }
        }, 0, AUTO_CONN_PORID);
    }

    public boolean connect(Context context, BluetoothDevice device) {
        this.mAddress = device.getAddress();
        return this.mBleCore.connect(context, device);
    }

    public boolean connect(Context context, String address) {
        this.mAddress = address;
        return this.mBleCore.connect(context, address);
    }

    public void disconnect() {
        this.mAddress = null;
        if (this.mAutoConn != null) {
            this.mAutoConn.cancel();
            this.mAutoConn = null;
        }
        this.mBleCore.disconnect();
    }

    public void setGattCallbacks(BleManagerCallbacks callbacks) {
    }

    public void closeBluetoothGatt() {
        this.mBleCore.closeBluetoothGatt();
    }

    public boolean write(byte[] data) {
        return this.mBleCore.write(data);
    }

    public void onWriteSuccess(byte[] data) {
        if (this.mCallbacks != null) {
            this.mCallbacks.onWriteSuccess(data);
        }
    }

    public void onReceiveTotalData(float totalDistance) {
        if (this.mCallbacks != null) {
            this.mCallbacks.onReceiveTotalData(totalDistance);
        }
        getSharedPreferences(SharePreference.FILE_NMAE, 0).edit().putFloat(SharePreference.HISTORY_DISTANCE, totalDistance).commit();
    }

    public void onReceiveCurrentData(Data0x00 data) {
        if (this.mCallbacks != null) {
            this.mCallbacks.onReceiveCurrentData(data);
        }
    }
}
