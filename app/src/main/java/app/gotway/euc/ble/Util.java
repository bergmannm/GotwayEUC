package app.gotway.euc.ble;

public class Util {
//    public static int[] bytes2ints(byte[] datas) {
//        int[] d = new int[datas.length];
//        for (int i = 0; i < datas.length; i++) {
//            d[i] = datas[i] & 255;
//        }
//        return d;
//    }

//    public static String bytes2HexStr(byte[] datas) {
//        StringBuilder sb = new StringBuilder("[");
//        for (byte data : datas) {
//            sb.append(String.valueOf(String.format("%02x", data & 255))).append(", ");
//        }
//        sb.replace(sb.length() - 2, sb.length(), "]");
//        return sb.toString();
//    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytes2HexStr(byte[] bytes) {
        if (bytes == null) {
            return "null, ";
        } else {
            char[] hexChars = new char[bytes.length * 4];

            for ( int j = 0, k = 0; j < bytes.length; j++ ) {
                int v = bytes[j] & 0xFF;
                hexChars[k++] = hexArray[v >>> 4];
                hexChars[k++] = hexArray[v & 0x0F];
                hexChars[k++] = ',';
                hexChars[k++] = ' ';
            }
            return new String(hexChars);
        }
    }
}
