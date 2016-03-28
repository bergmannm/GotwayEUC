package app.gotway.euc.ble;

import app.gotway.euc.ble.profile.BleManagerCallbacks;
import app.gotway.euc.data.Data0x00;
import app.gotway.euc.util.DebugLogger;

public class DataParser {
    private static final byte[] DATA;
    private static final byte[] END;
    private static final int END_OFFSET = 20;
    private static final byte[] HADER;
    private static final int HEADER_OFFSET = 2;
    private static final String TAG = "DataParser";
    private static byte[] mData;
    private static int mDataIndex;
    private static int mTestIndex;
    private static int start;

    static {
        HADER = new byte[]{(byte) 85, (byte) -86};
        END = new byte[]{(byte) 90, (byte) 90, (byte) 90, (byte) 90};
        byte[] bArr = new byte[24];
        bArr[0] = (byte) 85;
        bArr[1] = (byte) -86;
        bArr[2] = (byte) 22;
        bArr[3] = (byte) 111;
        bArr[4] = (byte) -4;
        bArr[5] = (byte) 24;
        bArr[8] = (byte) 18;
        bArr[10] = (byte) -1;
        bArr[11] = (byte) -17;
        bArr[12] = (byte) -82;
        bArr[13] = (byte) 93;
        bArr[15] = (byte) 1;
        bArr[16] = (byte) -1;
        bArr[17] = (byte) -8;
        bArr[19] = (byte) 24;
        bArr[20] = (byte) 90;
        bArr[21] = (byte) 90;
        bArr[22] = (byte) 90;
        bArr[23] = (byte) 90;
        DATA = bArr;
        mData = new byte[24];
        mTestIndex = 0;
        start = 7;
    }

    public static void parser(BleManagerCallbacks bleManagerCallbacks, byte[] arrby) {
        synchronized (DataParser.class) {
            int n = 0;
            while (n < (arrby.length)) {
                if (mDataIndex == 0) {
                    if (arrby[n] == 85) {
                        byte[] arrby2 = mData;
                        int n3 = mDataIndex;
                        mDataIndex = n3 + 1;
                        arrby2[n3] = arrby[n];
                    }
                } else if (mDataIndex == 1) {
                    if (arrby[n] == -86) {
                        byte[] arrby3 = mData;
                        int n4 = mDataIndex;
                        mDataIndex = n4 + 1;
                        arrby3[n4] = arrby[n];
                    } else {
                        mDataIndex = 0;
                    }
                } else {
                    byte[] arrby4 = mData;
                    int n5 = mDataIndex;
                    mDataIndex = n5 + 1;
                    arrby4[n5] = arrby[n];
                    if (mDataIndex > 20) {
                        if (arrby[n] == 90) {
                            if (mDataIndex == 23) {
                                //DebugLogger.e("DataParser", "\u6536\u5230\u5b8c\u6574\u5305:" + Util.bytes2HexStr(mData));
                                if (mData[18] == 0) {
                                    DataParser.parser0x00(bleManagerCallbacks, mData);
                                } else if (mData[18] == 4) {
                                    DataParser.parser0x04(bleManagerCallbacks, mData);
                                }
                                mDataIndex = 0;
                            }
                        } else {
                            mDataIndex = 0;
                        }
                    }
                }
                ++n;
            }
        }
    }


    private static byte[] createData() {
        byte[] data = new byte[END_OFFSET];
        for (int i = start; i < data.length; i++) {
            data[i] = DATA[mTestIndex % 24];
            mTestIndex++;
            start++;
            start %= END_OFFSET;
        }
        //DebugLogger.i(TAG, "receive:" + Util.bytes2HexStr(data));
        return data;
    }

    private static void parser0x00(BleManagerCallbacks bleManagerCallbacks, byte[] arrby) {
        short[] arrs = DataParser.convertToShort(arrby);
        Data0x00 data0x00 = new Data0x00();
        data0x00.voltageInt = (short) ((arrs[2] << 8) + arrs[3]);
        data0x00.energe = DataParser.getEnergeByVoltage(data0x00.voltageInt);
        data0x00.speed = Math.abs(DataParser.getSpeed((float) ((short) ((arrs[4] << 8) | arrs[5]))));
        data0x00.distance = (arrs[8] << 8) + arrs[9];
        data0x00.currentInt = (short) ((arrs[10] << 8) + arrs[11]);
        data0x00.temperature = DataParser.getTrueTemper((int) ((short) (arrs[12] << 8 | arrs[13])));
        // DebugLogger.i("DataParser", "voltage="+data0x00.voltageInt+"**********current=" + data0x00.currentInt + "*******speed = " + data0x00.speed + "*******temper = " + data0x00.temperature + "*****distance = " + data0x00.distance + "********energe = " + data0x00.energe);
        bleManagerCallbacks.onReceiveCurrentData(data0x00);
    }

    private static void parser0x04(BleManagerCallbacks bleManagerCallbacks, byte[] arrby) {
        short[] arrs = DataParser.convertToShort(arrby);
        bleManagerCallbacks.onReceiveTotalData((float) ((arrs[2] << 24) + (arrs[3] << 16) + (arrs[4] << 8) + arrs[5]) / 1000.0f);
    }


    private static short[] convertToShort(byte[] value) {
        short[] data = new short[value.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = (short) (value[i] & 255);
        }
        return data;
    }

    private static int getTrueTemper(int source) {
        return (int) ((((float) (source - 521)) / 340.0f) + 35.0f);
    }

    private static float getSpeed(float val) {
        return 3.6f * (val / 100.0f);
    }

    private static final int[] VOLTAGE_LEVELS = {5300, 5410, 5620, 5770, 5910, 6050, 6170, 6280, 6390, 6500, 6610};

    private static int getEnergeByVoltage(int voltage) {
        if (voltage < VOLTAGE_LEVELS[0]) {
            return 0;
        }
        int n = VOLTAGE_LEVELS.length - 1;
        for (int i = 0; i < n; i++) {
            if (voltage >= VOLTAGE_LEVELS[i] && voltage < VOLTAGE_LEVELS[i + 1]) {
                return i * 10 + (10 * (voltage - VOLTAGE_LEVELS[i])) / (VOLTAGE_LEVELS[i + 1] - VOLTAGE_LEVELS[i]);
            }
        }
        return 100;
    }

//    private static boolean checkEnd(byte[] data) {
//        return Arrays.equals(Arrays.copyOfRange(data, END_OFFSET, data.length), END);
//    }
//
//    private static boolean checkHead(byte[] data) {
//        return Arrays.equals(Arrays.copyOfRange(data, 0, HEADER_OFFSET), HADER);
//    }
//
//    private static boolean arrayContains(byte[] data, byte[] target) {
//        for (byte b : data) {
//            byte b2 = target[0];
//        }
//        return false;
//    }
}
