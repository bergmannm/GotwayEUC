package app.gotway.euc;

import android.app.Application;
import android.content.Intent;

import app.gotway.euc.ble.profile.BleService;
import app.gotway.euc.util.DebugLogger;

public class App extends Application {
    public void onCreate() {
        super.onCreate();
        DebugLogger.i("APP", "\u542f\u52a8\u670d\u52a1");
        startService(new Intent(this, BleService.class));
    }
}
