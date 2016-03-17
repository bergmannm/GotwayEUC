package app.gotway.euc.ble.profile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import java.util.Arrays;
import java.util.UUID;

import app.gotway.euc.ble.DataPraser;
import app.gotway.euc.ble.Util;
import app.gotway.euc.util.DebugLogger;

public class BleCore {
    public static final UUID CHARACTER_UUID;
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID;
    private static final String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
    private static final String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
    private static final String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
    public static final UUID SERVICE_UUID;
    private static final String TAG = "BleManager";
    private String address;
    private BluetoothGatt mBluetoothGatt;
    private BleManagerCallbacks mCallbacks;
    private BluetoothGattCharacteristic mCharacteristic;
    private Context mContext;
    private final BluetoothGattCallback mGattCallback;
    private boolean mIsConnected;
    private byte[] mLastWriteData;
    private boolean mUserDisConnect;

    public BleCore() {
        this.mGattCallback = new BluetoothGattCallback() {
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (status != 0) {
                    BleCore.this.onError(BleCore.ERROR_CONNECTION_STATE_CHANGE, status);
                } else if (newState == 2) {
                    DebugLogger.i(BleCore.TAG, "Device connected");
                    BleCore.this.mBluetoothGatt.discoverServices();
                    BleCore.this.mCallbacks.onDeviceConnected();
                } else if (newState == 0) {
                    DebugLogger.i(BleCore.TAG, "Device disconnected");
                    if (BleCore.this.mUserDisConnect) {
                        BleCore.this.mCallbacks.onDeviceDisconnected();
                    } else {
                        BleCore.this.mCallbacks.onLinklossOccur();
                        BleCore.this.mUserDisConnect = false;
                    }
                    BleCore.this.closeBluetoothGatt();
                }
            }

            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == 0) {
                    for (BluetoothGattService service : gatt.getServices()) {
                        if (service.getUuid().equals(BleCore.SERVICE_UUID)) {
                            BleCore.this.mCharacteristic = service.getCharacteristic(BleCore.CHARACTER_UUID);
                            DebugLogger.i(BleCore.TAG, "service is found------" + BleCore.this.mCharacteristic);
                        }
                    }
                    if (BleCore.this.mCharacteristic == null) {
                        BleCore.this.mCallbacks.onDeviceNotSupported();
                        gatt.disconnect();
                        return;
                    }
                    BleCore.this.mCallbacks.onServicesDiscovered();
                    BleCore.this.enableNotification(gatt);
                    return;
                }
                BleCore.this.onError(BleCore.ERROR_DISCOVERY_SERVICE, status);
            }

            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                DebugLogger.e(BleCore.TAG, "onCharacteristicRead---->");
            }

            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                DebugLogger.e(BleCore.TAG, "onDescriptorWrite---->" + Arrays.toString(descriptor.getValue()));
                if (status != 0) {
                    BleCore.this.onError(BleCore.ERROR_WRITE_DESCRIPTOR, status);
                } else if (descriptor.getUuid().equals(BleCore.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID) && descriptor.getValue()[0] == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE[0]) {
                    BleCore.this.mCallbacks.onNotifyEnable();
                    BleCore.this.mIsConnected = true;
                }
            }

            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                DebugLogger.d(BleCore.TAG, "onCharacteristicChanged---->");
                DataPraser.praser(BleCore.this.mCallbacks, characteristic.getValue());
            }

            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                DebugLogger.d(BleCore.TAG, "onCharacteristicWrite---->" + characteristic.getUuid().equals(BleCore.CHARACTER_UUID) + "******" + (BleCore.this.mCallbacks != null) + "*****" + characteristic.getUuid().equals(BleCore.CHARACTER_UUID));
                if (status == 0) {
                    byte[] value = characteristic.getValue();
                    String valueStr = Util.bytes2HexStr(value);
                    DebugLogger.e(BleCore.TAG, "write:" + valueStr);
                    if (characteristic.getUuid().equals(BleCore.CHARACTER_UUID) && Arrays.equals(value, BleCore.this.mLastWriteData) && BleCore.this.mCallbacks != null) {
                        DebugLogger.e(BleCore.TAG, "write:" + valueStr);
                        BleCore.this.mCallbacks.onWriteSuccess(BleCore.this.mLastWriteData);
                    }
                }
            }
        };
    }

    static {
        SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
        CHARACTER_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
        CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

    public boolean connect(Context context, BluetoothDevice device) {
        DebugLogger.i(TAG, "connect--" + device.getAddress());
        this.mContext = context;
        if (!isConnected()) {
            this.address = device.getAddress();
            if (this.mBluetoothGatt != null) {
                return this.mBluetoothGatt.connect();
            }
            this.mBluetoothGatt = device.connectGatt(this.mContext, false, this.mGattCallback);
            return true;
        } else if (device.getAddress().equals(this.address)) {
            DebugLogger.d(TAG, "\u662f\u540c\u4e00\u4e2a\u8bbe\u5907\uff0c\u5ffd\u7565\u6389");
            return true;
        } else {
            DebugLogger.d(TAG, "\u4e0d\u540c\u8bbe\u5907\uff0c\u5148\u65ad\u5f00");
            disConnect(false);
            return true;
        }
    }

    public boolean connect(Context context, String address) {
        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            BluetoothAdapter adapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
            if (adapter != null) {
                BluetoothDevice device = adapter.getRemoteDevice(address);
                if (device != null) {
                    return connect(context, device);
                }
            }
        }
        return false;
    }

    public void disconnect() {
        disConnect(true);
    }

    private void disConnect(boolean userDisconnect) {
        this.mUserDisConnect = userDisconnect;
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.disconnect();
        }
    }

    public void setGattCallbacks(BleManagerCallbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public boolean isConnected() {
        return this.mIsConnected;
    }

    public void closeBluetoothGatt() {
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.close();
            this.mBluetoothGatt = null;
        }
        this.mIsConnected = false;
    }

    private void enableNotification(BluetoothGatt gatt) {
        gatt.setCharacteristicNotification(this.mCharacteristic, true);
        BluetoothGattDescriptor descriptor = this.mCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    protected void onError(String errorWriteDescriptor, int status) {
        disConnect(false);
    }

    public boolean write(byte[] data) {
        if (this.mCharacteristic != null) {
            this.mCharacteristic.setValue(data);
            if (this.mBluetoothGatt.writeCharacteristic(this.mCharacteristic)) {
                this.mLastWriteData = data;
                return true;
            }
        }
        return false;
    }
}
