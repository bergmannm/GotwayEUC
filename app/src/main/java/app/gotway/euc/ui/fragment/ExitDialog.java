package app.gotway.euc.ui.fragment;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

import app.gotway.euc.R;
import app.gotway.euc.ble.profile.BleProfileActivity;
import app.gotway.euc.share.SharePreference;

public class ExitDialog extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.frgament_exit_dialog, null);
        final CheckBox box = (CheckBox) dialogView.findViewById(R.id.remember);
        dialogView.findViewById(R.id.yes).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int mode = 2;
                if (box.isChecked()) {
                    mode = 2 | 1;
                    ExitDialog.this.getActivity().getSharedPreferences(SharePreference.FILE_NMAE, 0).edit().putInt(SharePreference.EXIT_MODE, mode).commit();
                }
                ExitDialog.this.dismiss();
                ((BleProfileActivity) ExitDialog.this.getActivity()).exit(mode);
            }
        });
        dialogView.findViewById(R.id.no).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int mode = 0;
                if (box.isChecked()) {
                    mode = 1;
                    ExitDialog.this.getActivity().getSharedPreferences(SharePreference.FILE_NMAE, 0).edit().putInt(SharePreference.EXIT_MODE, mode).commit();
                }
                ExitDialog.this.dismiss();
                ((BleProfileActivity) ExitDialog.this.getActivity()).exit(mode);
            }
        });
        AlertDialog dialog = builder.setView(dialogView).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        return dialog;
    }
}
