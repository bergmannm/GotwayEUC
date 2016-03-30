package app.gotway.euc.ui.fragment;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import app.gotway.euc.R;
import app.gotway.euc.ble.profile.BleProfileActivity;

public class DisconnDialog extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.frgament_disconn_dialog, null);
        dialogView.findViewById(R.id.yes).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ((BleProfileActivity) DisconnDialog.this.getActivity()).disconnect();
                DisconnDialog.this.dismiss();
            }
        });
        dialogView.findViewById(R.id.no).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                DisconnDialog.this.dismiss();
            }
        });
        AlertDialog dialog = builder.setView(dialogView).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        return dialog;
    }
}
