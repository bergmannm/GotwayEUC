package app.gotway.euc.ui.pref;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import app.gotway.euc.R;

public class TiltbackPreferenceDialog extends SeekbarPreferenceDialog {

    public TiltbackPreferenceDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMaxValue = 9;
        setDialogLayoutResource(R.layout.pref_tiltback_speed_dlg);
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();
        mSeekBar = (SeekBar) view.findViewById(R.id.prefTiltbackSeekBar);
        return view;
    }
}