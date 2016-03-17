package app.gotway.euc.ble.cmd;

import java.util.ArrayList;
import java.util.Arrays;

public class CMDMgr {
    public static final byte[] ALARM_FIRST;
    public static final byte[] ALARM_OPEN;
    public static final byte[] ALARM_SECOND;
    public static final byte[] CALL;
    public static final byte[] CORRECT_END;
    public static final byte[] CORRECT_START;
    public static final byte[] MODE_COMFORTABLE;
    public static final byte[] MODE_EXPLORE;
    public static final byte[] MODE_SOFT;
    private static ArrayList<byte[]> NO_NEED_ALERT;
    public static final byte[] PADDLE_A;
    public static final byte[] PADDLE_CANCEL;
    private static final byte[] PADDLE_CLOSE;
    public static final byte[] PADDLE_D;
    public static final byte[] PADDLE_F;
    public static final byte[] PADDLE_G;
    public static final byte[] PADDLE_H;
    public static final byte[] PADDLE_J;
    public static final byte[] PADDLE_K;
    public static final byte[] PADDLE_L;
    private static final byte[] PADDLE_OPEN;
    public static final byte[] PADDLE_S;
    private static final byte[] PADDLE_Z;

    public CMDMgr() {
        NO_NEED_ALERT.add(CALL);
        NO_NEED_ALERT.add(CORRECT_START);
        NO_NEED_ALERT.add(CORRECT_END);
    }

    static {
        CALL = new byte[]{(byte) 98};
        MODE_EXPLORE = new byte[]{(byte) 104};
        MODE_COMFORTABLE = new byte[]{(byte) 102};
        MODE_SOFT = new byte[]{(byte) 115};
        ALARM_FIRST = new byte[]{(byte) 117};
        ALARM_SECOND = new byte[]{(byte) 105};
        ALARM_OPEN = new byte[]{(byte) 111};
        PADDLE_CLOSE = new byte[]{(byte) 73};
        PADDLE_OPEN = new byte[]{(byte) 79};
        PADDLE_A = new byte[]{(byte) 65};
        PADDLE_S = new byte[]{(byte) 83};
        PADDLE_D = new byte[]{(byte) 68};
        PADDLE_F = new byte[]{(byte) 70};
        PADDLE_G = new byte[]{(byte) 71};
        PADDLE_H = new byte[]{(byte) 72};
        PADDLE_J = new byte[]{(byte) 74};
        PADDLE_K = new byte[]{(byte) 75};
        PADDLE_L = new byte[]{(byte) 76};
        PADDLE_Z = new byte[]{(byte) 59};
        PADDLE_CANCEL = new byte[]{(byte) 34};
        CORRECT_START = new byte[]{(byte) 99};
        CORRECT_END = new byte[]{(byte) 121};
        NO_NEED_ALERT = new ArrayList();
    }

    public static boolean isNeedAlert(byte[] data) {
        return (!(Arrays.equals(data, CALL) || Arrays.equals(data, CORRECT_START) || Arrays.equals(data, CORRECT_END)));
    }
}
