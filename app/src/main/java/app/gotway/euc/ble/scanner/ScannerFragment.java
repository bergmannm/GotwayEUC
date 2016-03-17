package app.gotway.euc.ble.scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import java.util.UUID;

import app.gotway.euc.R;
import app.gotway.euc.util.DebugLogger;

public class ScannerFragment extends DialogFragment {
    private static final String CUSTOM_UUID = "custom_uuid";
    private static final boolean DEVICE_IS_BONDED = true;
    private static final boolean DEVICE_NOT_BONDED = false;
    private static final int NO_RSSI = -1000;
    private static final String PARAM_UUID = "param_uuid";
    private static final long SCAN_DURATION = 5000;
    private static final String TAG = "ScannerFragment";
    private DeviceListAdapter mAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private boolean mIsCustomUUID;
    private boolean mIsScanning;
    private LeScanCallback mLEScanCallback;
    private OnDeviceSelectedListener mListener;
    private Button mScanButton;
    private UUID mUuid;

    public interface OnDeviceSelectedListener {
        void onDeviceSelected(BluetoothDevice bluetoothDevice, String str);

        void onDialogCanceled();
    }

    public ScannerFragment() {
        this.mHandler = new Handler();
        this.mIsScanning = DEVICE_NOT_BONDED;
        this.mLEScanCallback = new LeScanCallback() {
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (device != null) {
                    ScannerFragment.this.updateScannedDevice(device, rssi);
                    if (ScannerFragment.this.mIsCustomUUID) {
                        try {
                            if (ScannerServiceParser.decodeDeviceAdvData(scanRecord, ScannerFragment.this.mUuid)) {
                                ScannerFragment.this.addScannedDevice(device, ScannerServiceParser.decodeDeviceName(scanRecord), rssi, ScannerFragment.DEVICE_NOT_BONDED);
                                return;
                            }
                            return;
                        } catch (Exception e) {
                            DebugLogger.e(ScannerFragment.TAG, "Invalid data in Advertisement packet " + e.toString());
                            return;
                        }
                    }
                    ScannerFragment.this.addScannedDevice(device, ScannerServiceParser.decodeDeviceName(scanRecord), rssi, ScannerFragment.DEVICE_NOT_BONDED);
                }
            }
        };
    }

    public static ScannerFragment getInstance(Context context, UUID uuid, boolean isCustomUUID) {
        ScannerFragment fragment = new ScannerFragment();
        Bundle args = new Bundle();
        args.putParcelable(PARAM_UUID, new ParcelUuid(uuid));
        args.putBoolean(CUSTOM_UUID, isCustomUUID);
        fragment.setArguments(args);
        return fragment;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnDeviceSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDeviceSelectedListener");
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args.containsKey(CUSTOM_UUID)) {
            this.mUuid = ((ParcelUuid) args.getParcelable(PARAM_UUID)).getUuid();
        }
        this.mIsCustomUUID = args.getBoolean(CUSTOM_UUID);
        this.mBluetoothAdapter = ((BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
    }

    public void onDestroyView() {
        stopScan();
        super.onDestroyView();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_device_selection, null);
        ListView listview = (ListView) dialogView.findViewById(android.R.id.list);
        listview.setEmptyView(dialogView.findViewById(android.R.id.empty));
        DeviceListAdapter deviceListAdapter = new DeviceListAdapter(getActivity());
        this.mAdapter = deviceListAdapter;
        listview.setAdapter(deviceListAdapter);
        builder.setTitle(R.string.scanner_title);
        AlertDialog dialog = builder.setView(dialogView).create();
        final AlertDialog alertDialog = dialog;
        listview.setOnItemClickListener(new OnItemClickListener() {
            private final /* synthetic */ AlertDialog val$dialog = alertDialog;

            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ScannerFragment.this.stopScan();
                this.val$dialog.dismiss();
                ExtendedBluetoothDevice d = (ExtendedBluetoothDevice) ScannerFragment.this.mAdapter.getItem(position);
                ScannerFragment.this.mListener.onDeviceSelected(d.device, d.name);
            }
        });
        this.mScanButton = (Button) dialogView.findViewById(R.id.action_cancel);
        final AlertDialog alertDialog1 = dialog;
        this.mScanButton.setOnClickListener(new OnClickListener() {
            private final /* synthetic */ AlertDialog val$dialog = alertDialog1;

            public void onClick(View v) {
                if (v.getId() != R.id.action_cancel) {
                    return;
                }
                if (ScannerFragment.this.mIsScanning) {
                    this.val$dialog.cancel();
                } else {
                    ScannerFragment.this.startScan();
                }
            }
        });
        if (savedInstanceState == null) {
            startScan();
        }
        return dialog;
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        this.mListener.onDialogCanceled();
    }

    private void startScan() {
        this.mAdapter.clearDevices();
        this.mScanButton.setText(R.string.scanner_action_cancel);
        this.mIsCustomUUID = DEVICE_IS_BONDED;
        if (this.mIsCustomUUID) {
            this.mBluetoothAdapter.startLeScan(this.mLEScanCallback);
        } else {
            this.mBluetoothAdapter.startLeScan(new UUID[]{this.mUuid}, this.mLEScanCallback);
        }
        this.mIsScanning = DEVICE_IS_BONDED;
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                if (ScannerFragment.this.mIsScanning) {
                    ScannerFragment.this.stopScan();
                }
            }
        }, SCAN_DURATION);
    }

    private void stopScan() {
        if (this.mIsScanning) {
            this.mScanButton.setText(R.string.scanner_action_scan);
            this.mBluetoothAdapter.stopLeScan(this.mLEScanCallback);
            this.mIsScanning = DEVICE_NOT_BONDED;
        }
    }

    private void addBondedDevices() {
        for (BluetoothDevice device : this.mBluetoothAdapter.getBondedDevices()) {
            this.mAdapter.addBondedDevice(new ExtendedBluetoothDevice(device, device.getName(), NO_RSSI, DEVICE_IS_BONDED));
        }
    }

    private void addScannedDevice(BluetoothDevice device, String name, int rssi, boolean isBonded) {
        final BluetoothDevice bluetoothDevice = device;
        final String str = name;
        final int i = rssi;
        final boolean z = isBonded;
        getActivity().runOnUiThread(new Runnable() {
            private final /* synthetic */ BluetoothDevice val$device = bluetoothDevice;
            private final /* synthetic */ boolean val$isBonded = z;
            private final /* synthetic */ String val$name = str;
            private final /* synthetic */ int val$rssi = i;

            public void run() {
                ScannerFragment.this.mAdapter.addOrUpdateDevice(new ExtendedBluetoothDevice(this.val$device, this.val$name, this.val$rssi, this.val$isBonded));
            }
        });
    }

    private void updateScannedDevice(BluetoothDevice device, int rssi) {
        final BluetoothDevice bluetoothDevice = device;
        final int i = rssi;
        getActivity().runOnUiThread(new Runnable() {
            private final /* synthetic */ BluetoothDevice val$device = bluetoothDevice;
            private final /* synthetic */ int val$rssi = i;

            public void run() {
                ScannerFragment.this.mAdapter.updateRssiOfBondedDevice(this.val$device.getAddress(), this.val$rssi);
            }
        });
    }
}
