package app.gotway.euc.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import app.gotway.euc.R;

public class AboutDialog extends Dialog {
    public AboutDialog(Context context) {
        super(context);
    }

    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.about);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AboutDialog.this.dismiss();
            }
        }, 5000);
    }
}
