package app.gotway.euc.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import app.gotway.euc.R;
import app.gotway.euc.ble.DataParser;
import app.gotway.euc.data.Data0x00;
import app.gotway.euc.data.RecordStats;
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

    File logsDir;

    void scanLogDir() {
        if (logsDir.exists()) {
            final List<File> csvFiles = new ArrayList<>();
            File[] files = logsDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    String fName = f.getName();
                    if (fName.toLowerCase().endsWith(".csv")) {
                        File fx = new File(f.getParentFile(), fName.substring(0,fName.length() - 4) + ".bin");
                        if (!fx.exists() || true) {
                            csvFiles.add(f);
                        }
                    }
                }
            }

            if (csvFiles.size()>0) {
                final Activity activity = getActivity();
                final ProgressDialog progress = new ProgressDialog(getActivity());
                progress.setTitle("Coverting CSV to binary format");
                progress.setMessage("");
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.setMax(csvFiles.size());
                progress.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for(int i = 0;i<csvFiles.size();i++) {
                                File f = csvFiles.get(i);
                                final String fName = f.getName();
                                if (fName.toLowerCase().endsWith(".csv")) {
                                    try {
                                        final int progressValue = i;
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progress.setMessage(fName);
                                                progress.setProgress(progressValue);
                                            }
                                        });
                                        List<Data0x00> data0x00s = DataParser.loadData(f);
                                        {
                                            long start = SystemClock.elapsedRealtime();
                                            RecordStats recordStats = RecordStats.create(data0x00s);
                                            File sf = new File(f.getParentFile(), fName.substring(0,fName.length() - 4) + ".stats.xml");
                                            if (recordStats != null) {
                                                OutputStream ous = new BufferedOutputStream(new FileOutputStream(sf));
                                                recordStats.serialize(ous);
                                                ous.close();
                                            }
                                            long elapsed = SystemClock.elapsedRealtime() - start;
                                            DebugLogger.i(DataParser.class.getSimpleName(), "Creating status " + sf.getName() + "  elapsed time=" + elapsed + " ms");

                                        }
                                        File fx = new File(f.getParentFile(), fName.substring(0,fName.length() - 4) + ".bin");
                                        {
                                            long start = SystemClock.elapsedRealtime();
                                            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fx)));
                                            dos.writeInt(data0x00s.size());
                                            for(Data0x00 d:data0x00s) {
                                                d.writeExternal(dos);
                                            }
                                            dos.close();
                                            long elapsed = SystemClock.elapsedRealtime() - start;
                                            DebugLogger.i(DataParser.class.getSimpleName(), "Binary serialization " + fx.getName() + "  elapsed time=" + elapsed + " ms");
                                        }
                                        data0x00s.clear();
                                        {
                                            long start = SystemClock.elapsedRealtime();
                                            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fx)));
                                            int count = dis.readInt();
                                            for(int j= 0;j<count;j++) {
                                                Data0x00 d = new Data0x00();
                                                d.readExternal(dis);
                                            }
                                            dis.close();
                                            long elapsed = SystemClock.elapsedRealtime() - start;
                                            DebugLogger.i(DataParser.class.getSimpleName(), "Binary deserialization " + fx.getName() + "  elapsed time=" + elapsed + " ms");
                                        }
                                    } catch (IOException e) {
                                        DebugLogger.e(this.getClass().getSimpleName(), e.toString(), e);
                                    }
                                }
                            }
                            DebugLogger.i(DataParser.class.getSimpleName(), "Finished log dir scanning.");
                        } finally {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progress.dismiss();
                                }
                            });

                        }
                    }
                }).start();
            }
        }
    }

    void initLogsDir() {
        logsDir = new File(Environment.getExternalStorageDirectory(), "GotwayLogs");
        if (!logsDir.exists()) {
            boolean created = logsDir.mkdirs();
            if (!created) {
                toast("Failed to create logs dir");
                return;
            }
        }
    }

    private void initView() {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density  = getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth  = outMetrics.widthPixels / density;

        initLogsDir();
        scanLogDir();
        ((Button) mRootView.findViewById(R.id.btnStartRecording)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
