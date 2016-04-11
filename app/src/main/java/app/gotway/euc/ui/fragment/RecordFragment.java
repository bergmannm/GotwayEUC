package app.gotway.euc.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import app.gotway.euc.R;
import app.gotway.euc.ble.DataParser;
import app.gotway.euc.util.DebugLogger;

public class RecordFragment extends Fragment {

    public RecordFragment(){
    }

    public static final String RECORD_ACTION = "record";
    private View mRootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        DebugLogger.w("RecordFragment", "onCreateView");
        if (this.mRootView != null) {
            ViewGroup parent = (ViewGroup) this.mRootView.getParent();
            if (parent != null) {
                parent.removeView(this.mRootView);
            }
        } else {
            this.mRootView = inflater.inflate(R.layout.fragment_record, container, false);
            initView();
        }

        return this.mRootView;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private static final int NOTIF_ID = 123456;

    private void initView() {
        ((Button) mRootView.findViewById(R.id.btnStartRecording)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File logsDir = new File(Environment.getExternalStorageDirectory(), "GotwayLogs");
                if (!logsDir.exists()) {
                    boolean created = logsDir.mkdirs();
                    if (!created) {
                        toast("Failed to create logs dir");
                        return;
                    }
                }
                File logFile = new File(logsDir, String.format("data-%s.csv", new SimpleDateFormat("yyMMdd-HHmmss").format(new Date())));
                DataParser.setLogFile(logFile);
                toast("Started recording to " + logFile);
                updateBtnVisibility();

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getActivity())
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Gotway")
                                .setContentText("Recording your trip ...");

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
                stackBuilder.addParentStack(getActivity().getClass());
                stackBuilder.addNextIntent(new Intent(getActivity(), getActivity().getClass()).setAction(RECORD_ACTION));
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setOngoing(true);
                getNotificationManager().notify(NOTIF_ID, mBuilder.build());
            }
        });
        ((Button) mRootView.findViewById(R.id.btnStopRecording)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataParser.setLogFile(null);
                toast("Recording stopped");
                updateBtnVisibility();

                getNotificationManager().cancel(NOTIF_ID);
            }
        });
        updateBtnVisibility();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    void updateBtnVisibility() {
        boolean recording = DataParser.isRecording();
        ((Button) mRootView.findViewById(R.id.btnStartRecording)).setVisibility(recording ? View.INVISIBLE : View.VISIBLE);
        ((Button) mRootView.findViewById(R.id.btnStopRecording)).setVisibility(recording ? View.VISIBLE : View.INVISIBLE);
    }

    Toast mToast;

    protected void toast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG);
        mToast.show();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            Activity a = getActivity();
            if(a != null) {
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            }
        }
    }

}
