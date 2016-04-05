package app.gotway.euc.ble.profile;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.Arrays;

import app.gotway.euc.BuildConfig;
import app.gotway.euc.R;
import app.gotway.euc.ble.cmd.CMDMgr;
import app.gotway.euc.ble.profile.BleService.LocalBinder;
import app.gotway.euc.ble.scanner.ScannerFragment.OnDeviceSelectedListener;
import app.gotway.euc.data.Data0x00;
import app.gotway.euc.share.SharePreference;
import app.gotway.euc.util.DebugLogger;

public class BleProfileActivity extends Activity implements OnDeviceSelectedListener, BleManagerCallbacks {
    public Data0x00 mData;

    private static final int REQUEST_ENABLE_BT = 2;
    protected static SharedPreferences mShare;
    private ServiceConnection conn;
    private Handler handler;
    private float mCurrentSpeed;
    private BleService mService;
    private Toast mToast;
    private Runnable notifyUINullData;
    private Runnable sendAlert;
    private Runnable stopCorrect;

    public BleProfileActivity() {
        this.handler = new Handler();
        this.conn = new ServiceConnection() {
            public void onServiceDisconnected(ComponentName name) {
                BleProfileActivity.this.mService = null;
            }

            public void onServiceConnected(ComponentName name, IBinder service) {
                BleProfileActivity.this.mService = ((LocalBinder) service).getService();
                BleProfileActivity.this.mService.setBleCallBack(BleProfileActivity.this);
                BleProfileActivity.this.mService.connect(BleProfileActivity.this.getApplicationContext(), BleProfileActivity.mShare.getString(SharePreference.DEVICE_ADDRESS, ""));
            }
        };
        this.stopCorrect = new Runnable() {
            public void run() {
                BleProfileActivity.this.writeData(CMDMgr.CORRECT_END);
            }
        };
        this.sendAlert = new Runnable() {
            public void run() {
                BleProfileActivity.this.writeData(CMDMgr.CALL);
            }
        };
        this.notifyUINullData = new Runnable() {
            public void run() {
                DebugLogger.i("ACT", "null Data");
                BleProfileActivity.this.mCurrentSpeed = 0.0f;
                BleProfileActivity.this.onReceiveCurrentData(null);
            }
        };
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkBle();
        Intent intent = new Intent(this, BleService.class);
        startService(intent);
        bindService(intent, this.conn, Context.BIND_AUTO_CREATE);
        mShare = getSharedPreferences(SharePreference.FILE_NMAE, 0);
    }

    public void onDeviceSelected(BluetoothDevice device, String name) {
        if (this.mService != null) {
            if (this.mService.isConnected() && !mShare.getString(SharePreference.DEVICE_ADDRESS, "").equals(device.getAddress())) {
                this.mService.disconnect();
            }
            this.mService.connect(getApplicationContext(), device);
            toast(R.string.device_conning);
            mShare.edit().putString(SharePreference.DEVICE_ADDRESS, device.getAddress()).apply();
        }
    }

    public void onDialogCanceled() {
    }

    public void onDeviceConnected() {
        checkListenerTime();
    }

    public void onDeviceDisconnected() {
        toast(R.string.device_disConn);
        this.handler.removeCallbacks(this.notifyUINullData);
        this.handler.post(this.notifyUINullData);
    }

    public void onServicesDiscovered() {
    }

    public void onNotifyEnable() {
        toast(R.string.device_conn);
    }

    public void onLinkLossOccur() {
        toast(R.string.device_loss_link);
        this.handler.removeCallbacks(this.notifyUINullData);
        this.handler.post(this.notifyUINullData);
    }

    public void onDeviceNotSupported() {
        toast(R.string.device_error);
    }

    public boolean writeData(byte[] data) {
        if (this.mService != null) {
            checkBle();
            if (!this.mService.isConnected()) {
                toast(R.string.device_disConn);
            } else if (this.mCurrentSpeed <= 0.3f) {
                return this.mService.write(data);
            } else {
                toast(R.string.cant_send_when_moving);
            }
        }
        return false;
    }

    protected void toast(final int id) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (BleProfileActivity.this.mToast != null) {
                    BleProfileActivity.this.mToast.cancel();
                }
                BleProfileActivity.this.mToast = Toast.makeText(BleProfileActivity.this, id, Toast.LENGTH_LONG);
                BleProfileActivity.this.mToast.show();
            }
        });
    }

    private void ensureBLESupported() {
        if (!getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            Toast.makeText(this, R.string.no_ble, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    boolean bt_auto_enabled = false;

    private void checkBle() {
        ensureBLESupported();
        if (!isBLEEnabled()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean bt_auto_on = prefs.getBoolean("turn_bluetooth_automatically", false);
            if (bt_auto_on) {
                getBluetoothAdapter().enable();
                bt_auto_enabled = true;
            } else {
                if (!BuildConfig.DEBUG) {
                    showBLEDialog();
                }
            }
        }
    }

    public boolean isBLEEnabled() {
        BluetoothAdapter adapter = getBluetoothAdapter();
        return adapter != null && (adapter.getState() == 12 || adapter.getState() == 11);
    }

    private BluetoothAdapter getBluetoothAdapter() {
        return ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
    }

    public void showBLEDialog() {
        startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), REQUEST_ENABLE_BT);
    }

    protected void onDestroy() {
        if (bt_auto_enabled || BuildConfig.DEBUG) {
            getBluetoothAdapter().disable();
        }
        this.handler.removeCallbacksAndMessages(null);
        super.onDestroy();
        if (this.mService != null) {
            unbindService(this.conn);
        }
    }

    public void onWriteSuccess(byte[] data) {
        DebugLogger.i("ACT", "onWriteSuccess");
        if (CMDMgr.isNeedAlert(data)) {
            runOnUiThread(new Runnable() {
                public void run() {
                    BleProfileActivity.this.handler.postDelayed(BleProfileActivity.this.sendAlert, 300);
                }
            });
        } else if (Arrays.equals(data, CMDMgr.CORRECT_START)) {
            runOnUiThread(new Runnable() {
                public void run() {
                    BleProfileActivity.this.handler.postDelayed(BleProfileActivity.this.stopCorrect, 300);
                }
            });
        }
    }

    public void disconnect() {
        if (isConnected()) {
            this.mService.disconnect();
        }
    }

    public boolean isConnected() {
        return this.mService != null && this.mService.isConnected();
    }

    public void onReceiveCurrentData(Data0x00 data) {
        if (data != null) {
            this.mCurrentSpeed = data.speed;
            checkListenerTime();
        }
    }

    public void onReceiveTotalData(float totalDistance) {
        checkListenerTime();
    }

    private void checkListenerTime() {
        // DebugLogger.i("ACT", "remove");
        this.handler.removeCallbacks(this.notifyUINullData);
        this.handler.postDelayed(this.notifyUINullData, 3000);
    }

    public void exit(int mode) {
        DebugLogger.i("exit", String.valueOf(mode));
        if (((mode >> 1) & 1) == 1) {
            DebugLogger.i("exit", "\u4fdd\u6301\u94fe\u63a5");
            finish();
            return;
        }
        DebugLogger.i("exit", "\u5b8c\u5168\u9000\u51fa");
        stopService(new Intent(getApplication(), BleService.class));
        finish();
    }

}
