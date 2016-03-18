package app.gotway.euc.ui.fragment;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

import app.gotway.euc.util.DebugLogger;

class PowerStats {
    private static final int MAX_PERIOD = 15;// sec
    private static final int TRIM_PERIOD = 10;// sec
    private static final int MIN_PERIOD = 3;// sec
    private float totalWh;

    static class Item {
        float power;
        float wh;
        int distance;
        long ts;
    }

    private List<Item> items = new ArrayList<>();

    private Item lastItem;
    void add(float power, int distance) {
        power = Math.abs(power);
        long ts = SystemClock.elapsedRealtimeNanos();
        if (lastItem != null) {
            long ns = ts - lastItem.ts;
            lastItem.wh = (power + lastItem.power) * ns / 2 / 3.6e12f;
            totalWh += lastItem.wh;
        }

        Item item = new Item();
        item.distance = distance;
        item.power = power;
        item.ts = ts;
        lastItem = item;
        items.add(item);

        if ((ts - items.get(0).ts)>MAX_PERIOD * 1e9) {
            int idx = 0;
            while((ts - items.get(idx).ts)>TRIM_PERIOD * 1e9) {
                idx++;
            }
            if (idx>0) {
                for(int i = 0;i<idx;i++) {
                    totalWh-=items.get(i).wh;
                }
                items.subList(0, idx).clear();
            }
        }
    }

    float getWhPerKm() {
        if (items.size()>2) {
            long elapsed = items.get(items.size() - 1).ts - items.get(0).ts;
            int distance  =  items.get(items.size() - 1).distance - items.get(0).distance;
            DebugLogger.i("PowerStats", "distance = " + distance);
            if (elapsed>MIN_PERIOD && distance>1) {
                float whPerKm = totalWh / (distance / 1000.0f);
                return whPerKm;
            }
        }
        return -1.0f;
    }

    float getSamplesPerSec() {
        if (items.size()>2) {
            long elapsed = items.get(items.size() - 1).ts - items.get(0).ts;
            if (elapsed>MIN_PERIOD) {
                return (1e9f * (items.size() - 1)) / elapsed;
            }
        }
        return -1.0f;
    }
}
