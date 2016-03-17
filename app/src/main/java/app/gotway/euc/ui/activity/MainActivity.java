package app.gotway.euc.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.KeyEvent;

import app.gotway.euc.R;
import app.gotway.euc.ble.profile.BleProfileActivity;
import app.gotway.euc.ble.profile.BleService;
import app.gotway.euc.data.Data0x00;
import app.gotway.euc.share.SharePreference;
import app.gotway.euc.ui.MainActivityMgr;
import app.gotway.euc.ui.fragment.ExitDialog;
import app.gotway.euc.util.DebugLogger;

public class MainActivity extends BleProfileActivity {
    public Data0x00 mData;
    private long mLastBackTime;
    private MainActivityMgr mgr;

    @SuppressLint({"InlinedApi"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VERSION.SDK_INT >= 19) {
            getWindow().addFlags(67108864);
            getWindow().addFlags(134217728);
        }
        setContentView(R.layout.activity_main);
        this.mgr = new MainActivityMgr(this);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        long time = System.currentTimeMillis();
        if (time - this.mLastBackTime < 2000) {
            judgeExit();
        } else {
            this.mLastBackTime = time;
            toast(R.string.exit_by_muilt_click);
        }
        return true;
    }

    private void judgeExit() {
        int mode = mShare.getInt(SharePreference.EXIT_MODE, 0);
        DebugLogger.i("judgeExit", String.valueOf(mode));
        if ((mode & 1) == 0) {
            new ExitDialog().show(getFragmentManager(), null);
        } else {
            exit(mode);
        }
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

    public void onReceiveCurrentData(Data0x00 data) {
        super.onReceiveCurrentData(data);
        if (data != null) {
            if (this.mData != null) {
                data.totalDistance = this.mData.totalDistance;
            }
            this.mData = data;
        } else {
            this.mData = new Data0x00();
        }
        this.mgr.setData(this.mData);
    }

    public void onReceiveTotalData(float totalDistance) {
        super.onReceiveTotalData(totalDistance);
        if (this.mData == null) {
            this.mData = new Data0x00();
        }
        this.mData.totalDistance = totalDistance;
        this.mgr.setData(this.mData);
    }
}
