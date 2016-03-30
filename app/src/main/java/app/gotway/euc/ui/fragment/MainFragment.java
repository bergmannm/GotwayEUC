package app.gotway.euc.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import app.gotway.euc.BuildConfig;
import app.gotway.euc.R;
import app.gotway.euc.ble.cmd.CMDMgr;
import app.gotway.euc.ble.profile.BleCore;
import app.gotway.euc.ble.profile.BleProfileActivity;
import app.gotway.euc.ble.scanner.ScannerFragment;
import app.gotway.euc.data.Data0x00;
import app.gotway.euc.ui.AboutDialog;
import app.gotway.euc.ui.activity.MainActivity;
import app.gotway.euc.ui.view.BatteryView;
import app.gotway.euc.ui.view.DashboardView;
import app.gotway.euc.ui.view.TemperatureView;
import app.gotway.euc.util.DebugLogger;

public class MainFragment extends Fragment implements OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener, LocationListener {
    private BleProfileActivity act;
    private BatteryView batterView;
    private DashboardView dashBoardView;
    private long lastAnimTime;
    private View mRootView;
    private TemperatureView temperView;

    private TextView batteryValues;
    private TextView energyConsumptionValue;

    private PowerStats powerStats = new PowerStats();

    private static boolean aboutDialogShown = false;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootView != null) {
            ViewGroup parent = (ViewGroup) this.mRootView.getParent();
            if (parent != null) {
                parent.removeView(this.mRootView);
            }
        } else {
            this.mRootView = inflater.inflate(R.layout.fragment_main, container, false);
            initView();
        }

        if (!aboutDialogShown && !BuildConfig.DEBUG) {
            aboutDialogShown = true;
            AboutDialog about = new AboutDialog(this.mRootView.getContext());
            about.requestWindowFeature(Window.FEATURE_NO_TITLE);
            about.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            about.show();
        }

        return this.mRootView;
    }

    private void initView() {
        this.batteryValues = (TextView) this.mRootView.findViewById(R.id.batteryValues);
        this.energyConsumptionValue = (TextView) this.mRootView.findViewById(R.id.energyConsumptionValue);

        this.temperView = (TemperatureView) this.mRootView.findViewById(R.id.temper);
        this.batterView = (BatteryView) this.mRootView.findViewById(R.id.batter);
        this.dashBoardView = (DashboardView) this.mRootView.findViewById(R.id.dashBoard);
        this.mRootView.findViewById(R.id.scan).setOnClickListener(this);
        this.mRootView.findViewById(R.id.volume).setOnClickListener(this);
        this.mRootView.findViewById(R.id.reset).setOnClickListener(this);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.act = (BleProfileActivity) activity;
    }

    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        updatePrefValues(sharedPreferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        setData(((BleProfileActivity) getActivity()).mData);
//        if (GPS_SPEED_ENABLED) {
//            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
//        if (GPS_SPEED_ENABLED) {
//            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//            locationManager.removeUpdates(this);
//        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan:
                if (this.act.isConnected()) {
                    new DisconnDialog().show(getFragmentManager(), null);
                } else if (((MainActivity) getActivity()).isBLEEnabled()) {
                    ScannerFragment.getInstance(getActivity(), BleCore.SERVICE_UUID, true).show(getFragmentManager(), null);
                } else {
                    ((MainActivity) getActivity()).showBLEDialog();
                }
                break;
            case R.id.volume:
                this.act.writeData(CMDMgr.CALL);
            case R.id.reset:
                this.distanceZero = this.lastDistance;
            default:
        }
    }

    MovingAverage voltageAvg = new MovingAverage(), currentAvg = new MovingAverage(), powerAvg = new MovingAverage();

    static final float AVG_COEF = 1.0f/4;

    void updateAvg(MovingAverage avg, float value) {
        if (avg.getCoef() == 0.0) {
            avg.reset(AVG_COEF, value);
        } else {
            avg.add(value);
        }
    }

    Vibrator vibrator;

    private static final long[] VIBRATE_PATTERN1 = {0, 150};
    private static final long[] VIBRATE_PATTERN2 = {0, 150, 50, 150};
    private static final long[] VIBRATE_PATTERN3 = {0, 150, 50, 150, 50, 150};

    protected void vibrate(long[] pattern){
        if (vibrator == null) {
            vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (vibrator != null) {
            vibrator.vibrate(pattern, -1);
        }
    }

    long lastVibrate;

    protected void vibrateMaybe(long currentTime, float speed) {
        if ((currentTime - lastVibrate)>1000) {
            long[] vibPattern = null;
            if (ALARM_SPEED3>0 && speed>=ALARM_SPEED3) {
                vibPattern = VIBRATE_PATTERN3;
            } else if (ALARM_SPEED2>0 && speed>=ALARM_SPEED2) {
                vibPattern = VIBRATE_PATTERN2;
            } else if (ALARM_SPEED1>0 && speed>=ALARM_SPEED1) {
                vibPattern = VIBRATE_PATTERN1;
            }
            if (vibPattern != null) {
                lastVibrate = currentTime;
                vibrate(vibPattern);
            }
        }
    }

    private float CURRENT_DIVIDER = 100.0f;
    private float SPEED_DIVIDER = 1.0f;

    private boolean VIB_ALARM_ENABLED = false;
//    private boolean GPS_SPEED_ENABLED = false;

    private float ALARM_SPEED1 = -1.0f;
    private float ALARM_SPEED2 = -1.0f;
    private float ALARM_SPEED3 = -1.0f;

    int distanceZero = 0;
    int lastDistance;

    public void setData(Data0x00 data) {
        if (data != null) {
            try {
                if (distanceZero > data.distance) {
                    distanceZero = data.distance;
                }

                float speed = data.speed / SPEED_DIVIDER;
                float VOLTAGE_DIVIDER = 100.0f;
                float voltage = data.voltageInt / VOLTAGE_DIVIDER;
                float current = data.currentInt / CURRENT_DIVIDER;
                int distance = data.distance - distanceZero;

                long time = SystemClock.elapsedRealtime();
                long duration = Math.min(1000, time - this.lastAnimTime);
                this.lastAnimTime = time;
                this.lastDistance = data.distance;
                this.dashBoardView.setData(distance, data.totalDistance, speed, duration);
                this.batterView.startAnim(data.energe, duration);
                this.temperView.startAnim((int) data.temperature, duration);

                if (VIB_ALARM_ENABLED) {
                    vibrateMaybe(time, speed);
                }

                float power = Math.abs(current * voltage);

                updateAvg(voltageAvg, voltage);
                updateAvg(currentAvg, Math.abs(current));
                updateAvg(powerAvg, power);


                this.batteryValues.setText(String.format("%.2f", voltageAvg.get()) + "V  "
                        + String.format("%6.2f", currentAvg.get()) + "A  "
                        + String.format("%7.2f", powerAvg.get()) + "W");

                {
                    powerStats.add(power, data.distance);
                    StringBuilder sb = new StringBuilder();
                    float whPerKm = powerStats.getWhPerKm();
                    sb.append(whPerKm < 0 ? "-" : String.format("%6.2f", whPerKm));
                    sb.append(" Wh/km");
                    // sb.append(",   Sampling rate:");
                    // float samplesPerSec = powerStats.getSamplesPerSec();
                    // sb.append(samplesPerSec < 0 ? "-" : String.format("%.2f", samplesPerSec));
                    // sb.append(" /sec");
                    energyConsumptionValue.setText(sb.toString());
                }

            } catch (NullPointerException e) {
                DebugLogger.e("MainFragment", "setData() \u65f6fragment\u7684view\u8fd8\u672a\u521d\u59cb\u5316\u6216\u8005\u6536\u5230\u7684\u6570\u636e\u4e3anull");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefValues(sharedPreferences);
    }

    private void updatePrefValues(SharedPreferences sharedPreferences) {
        SPEED_DIVIDER =  getPrefFloat(sharedPreferences, "speed_divider", 1.0f);
        CURRENT_DIVIDER = getPrefFloat(sharedPreferences, "current_divider", 100.0f);
        ALARM_SPEED1 = getPrefFloat(sharedPreferences, "vib_alarm_speed1", -1.0f);
        ALARM_SPEED2 = getPrefFloat(sharedPreferences, "vib_alarm_speed2", -1.0f);
        ALARM_SPEED3 = getPrefFloat(sharedPreferences, "vib_alarm_speed3", -1.0f);

        VIB_ALARM_ENABLED = sharedPreferences.getBoolean("vib_alarm_enabled", false);
//        boolean old_gps_speed_enabled = GPS_SPEED_ENABLED;
//        GPS_SPEED_ENABLED = sharedPreferences.getBoolean("gps_speed_enabled", false);
//        if (old_gps_speed_enabled != GPS_SPEED_ENABLED) {
//            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//            if (GPS_SPEED_ENABLED) {
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
//                        0, this);
//            } else {
//                locationManager.removeUpdates(this);
//            }
//        }
    }

    float getPrefFloat(SharedPreferences sharedPreferences, String key, float def) {
        String value = sharedPreferences.getString(key, null);
        if (value == null || value.length() == 0) {
            return def;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            DebugLogger.e("MainFragment", e.toString(), e);
        }
        return def;
    }

    @Override
    public void onLocationChanged(Location location) {
//        dashBoardView.setGpsSpeed(location.getAccuracy()>50 || !location.hasSpeed() ? -1 : 3.6f * location.getSpeed());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
