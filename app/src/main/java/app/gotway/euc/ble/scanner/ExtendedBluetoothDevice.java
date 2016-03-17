package app.gotway.euc.ble.scanner;

import android.bluetooth.BluetoothDevice;

class ExtendedBluetoothDevice {
    public BluetoothDevice device;
    public boolean isBonded;
    public String name;
    public int rssi;

    public static class AddressComparator {
        public String address;

        public boolean equals(Object o) {
            if (!(o instanceof ExtendedBluetoothDevice)) {
                return super.equals(o);
            }
            return this.address.equals(((ExtendedBluetoothDevice) o).device.getAddress());
        }
    }

    public ExtendedBluetoothDevice(BluetoothDevice device, String name, int rssi, boolean isBonded) {
        this.device = device;
        this.name = name;
        this.rssi = rssi;
        this.isBonded = isBonded;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ExtendedBluetoothDevice)) {
            return super.equals(o);
        }
        return this.device.getAddress().equals(((ExtendedBluetoothDevice) o).device.getAddress());
    }
}
