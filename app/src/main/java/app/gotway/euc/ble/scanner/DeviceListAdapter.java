package app.gotway.euc.ble.scanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import app.gotway.euc.R;
import app.gotway.euc.ble.scanner.ExtendedBluetoothDevice.AddressComparator;

public class DeviceListAdapter extends BaseAdapter {
    private static final int TYPE_EMPTY = 2;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_TITLE = 0;
    private final AddressComparator comparator;
    private final Context mContext;
    private final ArrayList<ExtendedBluetoothDevice> mListBondedValues;
    private final ArrayList<ExtendedBluetoothDevice> mListValues;

    private class ViewHolder {
        private TextView address;
        private TextView name;
        private ImageView rssi;

        private ViewHolder() {
        }
    }

    public DeviceListAdapter(Context context) {
        this.mListBondedValues = new ArrayList();
        this.mListValues = new ArrayList();
        this.comparator = new AddressComparator();
        this.mContext = context;
    }

    public void addBondedDevice(ExtendedBluetoothDevice device) {
        this.mListBondedValues.add(device);
        notifyDataSetChanged();
    }

    public void updateRssiOfBondedDevice(String address, int rssi) {
        this.comparator.address = address;
        int indexInBonded = this.mListBondedValues.indexOf(this.comparator);
        if (indexInBonded >= 0) {
            ((ExtendedBluetoothDevice) this.mListBondedValues.get(indexInBonded)).rssi = rssi;
            notifyDataSetChanged();
        }
    }

    public void addOrUpdateDevice(ExtendedBluetoothDevice device) {
        if (!this.mListBondedValues.contains(device)) {
            int indexInNotBonded = this.mListValues.indexOf(device);
            if (indexInNotBonded >= 0) {
                ((ExtendedBluetoothDevice) this.mListValues.get(indexInNotBonded)).rssi = device.rssi;
                notifyDataSetChanged();
                return;
            }
            this.mListValues.add(device);
            notifyDataSetChanged();
        }
    }

    public void clearDevices() {
        this.mListValues.clear();
        notifyDataSetChanged();
    }

    public int getCount() {
        int bondedCount = this.mListBondedValues.size() + TYPE_ITEM;
        int availableCount = this.mListValues.isEmpty() ? TYPE_EMPTY : this.mListValues.size() + TYPE_ITEM;
        return bondedCount == TYPE_ITEM ? availableCount : availableCount + bondedCount;
    }

    public Object getItem(int position) {
        int bondedCount = this.mListBondedValues.size() + TYPE_ITEM;
        if (!this.mListBondedValues.isEmpty()) {
            return "";
        }
        if (position == 0) {
            return Integer.valueOf(R.string.scanner_subtitle__not_bonded);
        }
        return this.mListValues.get(position - 1);
    }

    public int getViewTypeCount() {
        return 3;
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_ITEM;
    }

    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }
        if (!this.mListBondedValues.isEmpty() && position == this.mListBondedValues.size() + TYPE_ITEM) {
            return 0;
        }
        if (position == getCount() - 1 && this.mListValues.isEmpty()) {
            return TYPE_EMPTY;
        }
        return TYPE_ITEM;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View oldView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.mContext);
        View view = oldView;
        switch (getItemViewType(position)) {
            case 0:
                if (view == null) {
                    view = inflater.inflate(R.layout.device_list_title, parent, false);
                }
                ((TextView) view).setText(((Integer) getItem(position)).intValue());
                return view;
            case TYPE_EMPTY /*2*/:
                if (view == null) {
                    return inflater.inflate(R.layout.device_list_empty, parent, false);
                }
                return view;
            default:
                ViewHolder holder;
                if (view == null) {
                    view = inflater.inflate(R.layout.device_list_row, parent, false);
                    holder = new ViewHolder();
                    holder.name = (TextView) view.findViewById(R.id.name);
                    holder.address = (TextView) view.findViewById(R.id.address);
                    holder.rssi = (ImageView) view.findViewById(R.id.rssi);
                    view.setTag(holder);
                }
                ExtendedBluetoothDevice device = (ExtendedBluetoothDevice) getItem(position);
                holder = (ViewHolder) view.getTag();
                String name = device.name;
                TextView access$4 = holder.name;
                if (name == null) {
                    name = this.mContext.getString(R.string.not_available);
                }
                access$4.setText(name);
                holder.address.setText(device.device.getAddress());
                if (device.isBonded && device.rssi == -1000) {
                    holder.rssi.setVisibility(View.GONE);
                    return view;
                }
                holder.rssi.setImageLevel((int) ((100.0f * (127.0f + ((float) device.rssi))) / 147.0f));
                holder.rssi.setVisibility(View.VISIBLE);
                return view;
        }
    }
}
