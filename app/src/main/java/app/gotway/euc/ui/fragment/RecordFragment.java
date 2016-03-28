package app.gotway.euc.ui.fragment;

import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import app.gotway.euc.BuildConfig;
import app.gotway.euc.R;
import app.gotway.euc.ui.AboutDialog;

public class RecordFragment extends Fragment {

    private View mRootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootView != null) {
            ViewGroup parent = (ViewGroup) this.mRootView.getParent();
            if (parent != null) {
                parent.removeView(this.mRootView);
            }
        } else {
            this.mRootView = inflater.inflate(R.layout.fragment_record, container, false);
            initView();
        }

        return this.mRootView;
    }

    private void initView() {
        ((Button)mRootView.findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
