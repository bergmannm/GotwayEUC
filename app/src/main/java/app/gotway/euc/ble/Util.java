package app.gotway.euc.ble;

public class Util {
    public static int[] bytes2ints(byte[] datas) {
        int[] d = new int[datas.length];
        for (int i = 0; i < datas.length; i++) {
            d[i] = datas[i] & 255;
        }
        return d;
    }

    public static String bytes2HexStr(byte[] datas) {
        StringBuilder sb = new StringBuilder("[");
        for (byte data : datas) {
            sb.append(String.valueOf(String.format("%02x", data & 255))).append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "]");
        return sb.toString();
    }
}
