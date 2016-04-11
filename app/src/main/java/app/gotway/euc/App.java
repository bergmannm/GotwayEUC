package app.gotway.euc;

import android.app.Application;
import android.content.Intent;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.List;

import app.gotway.euc.ble.profile.BleService;
import app.gotway.euc.util.DebugLogger;

public class App extends Application {
    public void onCreate() {
        super.onCreate();
        final ACRAConfiguration config;
        try {
            List<ReportField> reportFields = new ArrayList<>();
            ;
            config = new ConfigurationBuilder(this)
                    .setMailTo("michal.bergmann@gmail.com")
                    .setReportingInteractionMode(ReportingInteractionMode.DIALOG)
                    .setResDialogText(R.string.resDialogText)
                    .setResToastText(R.string.resDialogText)
                    .setResDialogNegativeButtonText(R.string.resDialogNegativeButtonText)
                    .setResDialogPositiveButtonText(R.string.resDialogPositiveButtonText)
                    .setCustomReportContent(new ReportField[]{
                            ReportField.ANDROID_VERSION,
                            ReportField.APP_VERSION_CODE,
                            ReportField.APP_VERSION_NAME,
                            ReportField.APPLICATION_LOG,
                            ReportField.AVAILABLE_MEM_SIZE,
                            ReportField.BRAND,
                            ReportField.BUILD,
                            ReportField.BUILD_CONFIG,
                            ReportField.CRASH_CONFIGURATION,
                            ReportField.DEVICE_FEATURES,
                            ReportField.DISPLAY,
                            ReportField.ENVIRONMENT,
                            ReportField.FILE_PATH,
                            ReportField.INITIAL_CONFIGURATION,
                            ReportField.INSTALLATION_ID,
                            ReportField.LOGCAT,
                            ReportField.PACKAGE_NAME,
                            ReportField.PHONE_MODEL,
                            ReportField.PRODUCT,
                            ReportField.REPORT_ID,
                            ReportField.SETTINGS_GLOBAL,
                            ReportField.SETTINGS_GLOBAL,
                            ReportField.SETTINGS_SECURE,
                            ReportField.SHARED_PREFERENCES,
                            ReportField.STACK_TRACE,
                            ReportField.THREAD_DETAILS,
                            ReportField.TOTAL_MEM_SIZE,
                            ReportField.USER_APP_START_DATE,
                            ReportField.USER_CRASH_DATE
                    }).build();
            // Initialise ACRA
            ACRA.init(this, config);
        } catch (ACRAConfigurationException e) {
            DebugLogger.w(this.getClass().getSimpleName(), e.toString(), e);
        }


        DebugLogger.i("APP", "\u542f\u52a8\u670d\u52a1");
        startService(new Intent(this, BleService.class));
    }

    Object mState;

    public void setState(Object state) {
        this.mState = state;
    }

    public Object getState() {
        return mState;
    }
}
