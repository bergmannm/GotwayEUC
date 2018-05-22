package app.gotway.euc.ui.fragment;

import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.gotway.euc.R;

public class AboutFragment extends Fragment {
    private View mRootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootView != null) {
            ViewGroup parent = (ViewGroup) this.mRootView.getParent();
            if (parent != null) {
                parent.removeView(this.mRootView);
            }
        } else {
            this.mRootView = inflater.inflate(R.layout.fragment_about, container, false);
            initView();
        }
        return this.mRootView;
    }

    private void initView() {
        TextView version = (TextView) this.mRootView.findViewById(R.id.version);
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), PackageManager.GET_CONFIGURATIONS);
            version.setText(getActivity().getString(R.string.about_version, pInfo.versionName));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
