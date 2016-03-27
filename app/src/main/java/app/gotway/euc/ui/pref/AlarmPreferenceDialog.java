package app.gotway.euc.ui.pref;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

import app.gotway.euc.R;

public class AlarmPreferenceDialog extends SeekbarPreferenceDialog {

    public AlarmPreferenceDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMaxValue = 2;
        setDialogLayoutResource(R.layout.pref_alarm_dlg);
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();
        mSeekBar = (SeekBar) view.findViewById(R.id.prefAlarmSeekBar);
        return view;
    }
}