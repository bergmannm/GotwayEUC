//package app.gotway.euc.ui.fragment;
//
//import android.app.Activity;
//import android.app.Fragment;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ExpandableListView;
//import android.widget.ExpandableListView.OnChildClickListener;
//
//import app.gotway.euc.R;
//import app.gotway.euc.ble.profile.BleProfileActivity;
//import app.gotway.euc.ui.adapter.SettingListAdapter;
//import app.gotway.euc.util.DebugLogger;
//
//public class SettingFragment2 extends Fragment {
//    private BleProfileActivity act;
//    private SettingListAdapter adapter;
//    private ExpandableListView mRootView;
//
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if (this.mRootView != null) {
//            ViewGroup parent = (ViewGroup) this.mRootView.getParent();
//            if (parent != null) {
//                parent.removeView(this.mRootView);
//            }
//        } else {
//            this.mRootView = (ExpandableListView) inflater.inflate(R.layout.fragment_setting2, container, false);
//            initView();
//        }
//        return this.mRootView;
//    }
//
//    private void initView() {
//        ExpandableListView v = this.mRootView;
//        DebugLogger.i(getTag(), "initView");
//        this.adapter = new SettingListAdapter(getActivity());
//        v.setAdapter(this.adapter);
//        v.setOnChildClickListener(new OnChildClickListener() {
//            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                DebugLogger.i(SettingFragment2.this.getTag(), "click : " + SettingFragment2.this.getString(((Integer) SettingFragment2.this.adapter.getChild(groupPosition, childPosition)).intValue()));
//                SettingFragment2.this.act.writeData(SettingListAdapter.getCMDByPosition(groupPosition, childPosition));
//                return false;
//            }
//        });
//    }
//
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        this.act = (BleProfileActivity) activity;
//    }
//}
