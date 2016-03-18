package app.gotway.euc.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

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

public class MainFragment extends Fragment implements OnClickListener {
    private BleProfileActivity act;
    private BatteryView batterView;
    private DashboardView dashBoardView;
    private long lastAnimTime;
    private View mRootView;
    private TemperatureView temperView;

    private TextView batteryValues;
    private TextView energyConsumptionValue;

    private PowerStats powerStats = new PowerStats();

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

        AboutDialog about = new AboutDialog(this.mRootView.getContext());
        about.requestWindowFeature(Window.FEATURE_NO_TITLE);
        about.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        about.show();

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
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.act = (BleProfileActivity) activity;
    }

    public void onResume() {
        super.onResume();
        setData(((MainActivity) getActivity()).mData);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan /*2131361799*/:
                if (this.act.isConnected()) {
                    new DisconnDialog().show(getFragmentManager(), null);
                } else if (((MainActivity) getActivity()).isBLEEnabled()) {
                    ScannerFragment.getInstance(getActivity(), BleCore.SERVICE_UUID, true).show(getFragmentManager(), null);
                } else {
                    ((MainActivity) getActivity()).showBLEDialog();
                }
                break;
            case R.id.volume /*2131361800*/:
                this.act.writeData(CMDMgr.CALL);
            default:
        }
    }

    public void setData(Data0x00 data) {
        try {
            long time = System.currentTimeMillis();
            long duration = Math.min(1000, time - this.lastAnimTime);
            this.lastAnimTime = time;
            this.dashBoardView.setData(data, duration);
            this.batterView.startAnim(data.energe, duration);
            this.temperView.startAnim((int) data.temperature, duration);

            float power = data.current * data.voltage;

            this.batteryValues.setText(String.format("%.1f", data.voltage) + " V,  "
                    + String.format(data.current >= 10 ? "%.1f" : "%.2f", data.current) + " A,  "
                    + String.format(power >= 100 ? "%.0f" : power >= 10 ? "%.1f" : "%.2f", power) + " W");

            {
                powerStats.add(power, data.distance);
                StringBuilder sb = new StringBuilder();
                float whPerKm = powerStats.getWhPerKm();
                sb.append(whPerKm < 0 ? "-" : String.format("%.1f", whPerKm));
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
