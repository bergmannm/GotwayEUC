package app.gotway.euc.ble.profile;

import app.gotway.euc.data.Data0x00;

public interface BleManagerCallbacks {
    void onDeviceConnected();

    void onDeviceDisconnected();

    void onDeviceNotSupported();

    void onLinklossOccur();

    void onNotifyEnable();

    void onReciveCurrentData(Data0x00 data0x00);

    void onReviceTotalData(float f);

    void onServicesDiscovered();

    void onWriteSuccess(byte[] bArr);
}
