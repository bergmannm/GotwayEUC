package app.gotway.euc.ble.scanner;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import app.gotway.euc.util.DebugLogger;

class ScannerServiceParser {
    private static final int COMPLETE_LOCAL_NAME = 9;
    private static final int FLAGS_BIT = 1;
    private static final byte LE_GENERAL_DISCOVERABLE_MODE = (byte) 2;
    private static final byte LE_LIMITED_DISCOVERABLE_MODE = (byte) 1;
    private static final int SERVICES_COMPLETE_LIST_128_BIT = 7;
    private static final int SERVICES_COMPLETE_LIST_16_BIT = 3;
    private static final int SERVICES_COMPLETE_LIST_32_BIT = 5;
    private static final int SERVICES_MORE_AVAILABLE_128_BIT = 6;
    private static final int SERVICES_MORE_AVAILABLE_16_BIT = 2;
    private static final int SERVICES_MORE_AVAILABLE_32_BIT = 4;
    private static final int SHORTENED_LOCAL_NAME = 8;
    private static final String TAG = "ScannerServiceParser";

    public static boolean decodeDeviceAdvData(byte[] data, UUID requiredUUID) {
        String uuid = requiredUUID != null ? requiredUUID.toString() : null;
        if (data == null) {
            return false;
        }
        boolean valid;
        boolean connectable = false;
        valid = uuid == null;
        int packetLength = data.length;
        int index = 0;
        while (index < packetLength) {
            int fieldLength = data[index];
            if (fieldLength != 0) {
                index += FLAGS_BIT;
                int fieldName = data[index];
                if (uuid != null) {
                    int i;
                    if (fieldName == SERVICES_MORE_AVAILABLE_16_BIT || fieldName == SERVICES_COMPLETE_LIST_16_BIT) {
                        i = index + FLAGS_BIT;
                        while (i < (index + fieldLength) - 1) {
                            valid = valid || decodeService16BitUUID(uuid, data, i, SERVICES_MORE_AVAILABLE_16_BIT);
                            i += SERVICES_MORE_AVAILABLE_16_BIT;
                        }
                    } else if (fieldName == SERVICES_MORE_AVAILABLE_32_BIT || fieldName == SERVICES_COMPLETE_LIST_32_BIT) {
                        i = index + FLAGS_BIT;
                        while (i < (index + fieldLength) - 1) {
                            valid = valid || decodeService32BitUUID(uuid, data, i, SERVICES_MORE_AVAILABLE_32_BIT);
                            i += SERVICES_MORE_AVAILABLE_32_BIT;
                        }
                    } else if (fieldName == SERVICES_MORE_AVAILABLE_128_BIT || fieldName == SERVICES_COMPLETE_LIST_128_BIT) {
                        i = index + FLAGS_BIT;
                        while (i < (index + fieldLength) - 1) {
                            valid = valid || decodeService128BitUUID(uuid, data, i, 16);
                            i += 16;
                        }
                    }
                }
                if (fieldName == FLAGS_BIT) {
                    connectable = (data[index + FLAGS_BIT] & SERVICES_COMPLETE_LIST_16_BIT) > 0;
                }
                index = (index + (fieldLength - 1)) + FLAGS_BIT;
            } else return connectable && valid;
        }
        return connectable && valid;
    }

    public static String decodeDeviceName(byte[] data) {
        int packetLength = data.length;
        int index = 0;
        while (index < packetLength) {
            int fieldLength = data[index];
            if (fieldLength == 0) {
                return null;
            }
            index += FLAGS_BIT;
            int fieldName = data[index];
            if (fieldName == COMPLETE_LOCAL_NAME || fieldName == SHORTENED_LOCAL_NAME) {
                return decodeLocalName(data, index + FLAGS_BIT, fieldLength - 1);
            }
            index = (index + (fieldLength - 1)) + FLAGS_BIT;
        }
        return null;
    }

    private static String decodeLocalName(byte[] data, int start, int length) {
        try {
            return new String(data, start, length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unable to convert the complete local name to UTF-8", e);
            return null;
        } catch (IndexOutOfBoundsException e2) {
            Log.e(TAG, "Error when reading complete local name", e2);
            return null;
        }
    }

    private static boolean decodeService16BitUUID(String uuid, byte[] data, int startPosition, int serviceDataLength) {
        String serviceUUID = Integer.toHexString(decodeUuid16(data, startPosition));
        DebugLogger.d(TAG, serviceUUID);
        String requiredUUID = uuid.substring(SERVICES_MORE_AVAILABLE_32_BIT, SHORTENED_LOCAL_NAME);
        DebugLogger.d(TAG, requiredUUID);
        return serviceUUID.equals(requiredUUID);
    }

    private static boolean decodeService32BitUUID(String uuid, byte[] data, int startPosition, int serviceDataLength) {
        String serviceUUID = Integer.toHexString(decodeUuid16(data, (startPosition + serviceDataLength) - 4));
        DebugLogger.d(TAG, serviceUUID);
        return serviceUUID.equals(uuid.substring(SERVICES_MORE_AVAILABLE_32_BIT, SHORTENED_LOCAL_NAME));
    }

    private static boolean decodeService128BitUUID(String uuid, byte[] data, int startPosition, int serviceDataLength) {
        String serviceUUID = Integer.toHexString(decodeUuid16(data, (startPosition + serviceDataLength) - 4));
        DebugLogger.d(TAG, serviceUUID);
        return serviceUUID.equals(uuid.substring(SERVICES_MORE_AVAILABLE_32_BIT, SHORTENED_LOCAL_NAME));
    }

    private static int decodeUuid16(byte[] data, int start) {
        return ((data[start + FLAGS_BIT] & 255) << SHORTENED_LOCAL_NAME) | ((data[start] & 255) << 0);
    }
}
