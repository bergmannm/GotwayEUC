package app.gotway.euc.ui.pref;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import app.gotway.euc.R;

public class ModePreferenceDialog extends SeekbarPreferenceDialog {

    public ModePreferenceDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMaxValue = 2;
        setDialogLayoutResource(R.layout.pref_mode_dlg);
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();
        mSeekBar = (SeekBar) view.findViewById(R.id.prefModeSeekBar);
        return view;
    }
}
