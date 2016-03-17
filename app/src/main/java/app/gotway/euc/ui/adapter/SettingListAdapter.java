package app.gotway.euc.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import app.gotway.euc.R;
import app.gotway.euc.ble.cmd.CMDMgr;

public class SettingListAdapter extends BaseExpandableListAdapter {
    private static final int[][] CHILD_STR_ID;
    private static final byte[][][] CMDS;
    private static final int[] GROUP_STR_ID;
    private LayoutInflater inflater;

    static {
        GROUP_STR_ID = new int[]{R.string.setMode, R.string.setWarning, R.string.setPaddleSpeed, R.string.setCorrect};
        int[][] iArr = new int[4][];
        iArr[0] = new int[]{R.string.setModeExplode, R.string.setModeComfortable, R.string.setModeSoft};
        iArr[1] = new int[]{R.string.setWarningFirst, R.string.setWarningSecond, R.string.setWarningOpenAll};
        iArr[2] = new int[]{R.string.setPaddleSpeedA, R.string.setPaddleSpeedS, R.string.setPaddleSpeedD, R.string.setPaddleSpeedF, R.string.setPaddleSpeedG, R.string.setPaddleSpeedH, R.string.setPaddleSpeedJ, R.string.setPaddleSpeedK, R.string.setPaddleSpeedL, R.string.setPaddleSpeedCancel};
        iArr[3] = new int[]{R.string.setCorrectStart};
        CHILD_STR_ID = iArr;
        byte[][][] r0 = new byte[4][][];
        r0[0] = new byte[][]{CMDMgr.MODE_EXPLORE, CMDMgr.MODE_COMFORTABLE, CMDMgr.MODE_SOFT};
        r0[1] = new byte[][]{CMDMgr.ALARM_FIRST, CMDMgr.ALARM_SECOND, CMDMgr.ALARM_OPEN};
        r0[2] = new byte[][]{CMDMgr.PADDLE_A, CMDMgr.PADDLE_S, CMDMgr.PADDLE_D, CMDMgr.PADDLE_F, CMDMgr.PADDLE_G, CMDMgr.PADDLE_H, CMDMgr.PADDLE_J, CMDMgr.PADDLE_K, CMDMgr.PADDLE_L, CMDMgr.PADDLE_CANCEL};
        r0[3] = new byte[][]{CMDMgr.CORRECT_START};
        CMDS = r0;
    }

    public static byte[] getCMDByPosition(int groupPosition, int childPosition) {
        return CMDS[groupPosition][childPosition];
    }

    public SettingListAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public int getGroupCount() {
        return GROUP_STR_ID.length;
    }

    public int getChildrenCount(int groupPosition) {
        return CHILD_STR_ID[groupPosition].length;
    }

    public Object getGroup(int groupPosition) {
        return GROUP_STR_ID[groupPosition];
    }

    public Object getChild(int groupPosition, int childPosition) {
        return CHILD_STR_ID[groupPosition][childPosition];
    }

    public long getGroupId(int groupPosition) {
        return (long) groupPosition;
    }

    public long getChildId(int groupPosition, int childPosition) {
        return (long) childPosition;
    }

    public boolean hasStableIds() {
        return true;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.setting_group, null);
        }
        TextView t = (TextView) convertView;
        t.setLayoutParams(new LayoutParams(-1, (int) parent.getContext().getResources().getDimension(R.dimen.settingItemTittleHeight)));
        t.setPadding(t.getPaddingLeft(), t.getPaddingTop(), t.getPaddingRight(), (int) parent.getContext().getResources().getDimension(R.dimen.settingItemTittlePaddingBottom));
        t.setText(GROUP_STR_ID[groupPosition]);
        return convertView;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.setting_item, null);
        }
        TextView t = (TextView) convertView;
        t.setLayoutParams(new LayoutParams(-1, (int) parent.getContext().getResources().getDimension(R.dimen.item_height)));
        t.setText(CHILD_STR_ID[groupPosition][childPosition]);
        return convertView;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
