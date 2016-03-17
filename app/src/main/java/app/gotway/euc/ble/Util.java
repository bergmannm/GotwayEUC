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
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0; i < datas.length; i++) {
            sb.append(new StringBuilder(String.valueOf(String.format("%02x", new Object[]{Integer.valueOf(datas[i] & 255)}))).append(", ").toString());
        }
        sb.replace(sb.length() - 2, sb.length(), "]");
        return sb.toString();
    }
}
