package app.gotway.euc.ui.activity;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentCompat;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.gotway.euc.BuildConfig;
import app.gotway.euc.R;
import app.gotway.euc.ble.profile.BleProfileActivity;
import app.gotway.euc.ble.profile.BleService;
import app.gotway.euc.data.Data0x00;
import app.gotway.euc.share.SharePreference;
import app.gotway.euc.ui.MainActivityMgr;
import app.gotway.euc.ui.fragment.ExitDialog;
import app.gotway.euc.ui.fragment.MainFragment;
import app.gotway.euc.ui.fragment.PrefsFragment;
import app.gotway.euc.ui.fragment.RecordFragment;
import app.gotway.euc.util.DebugLogger;

public class MainTabsActivity extends BleProfileActivity {
    public Data0x00 mData;
    private long mLastBackTime;
    private MainFragment mainFragment;

    @SuppressLint({"InlinedApi"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (VERSION.SDK_INT >= 19) {
//            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        }
        setContentView(R.layout.activity_main);

        if (BuildConfig.DEBUG) {
            initMainActivity();
        } else {
            final ViewStub mViewStub = (ViewStub) findViewById(R.id.flash);
            mViewStub.inflate();
            View v = findViewById(R.id.stub_flash);
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.flash_alpha);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    initMainActivity();
                }
            });
            v.startAnimation(anim);
        }
    }

    private void initMainActivity() {
        ViewStub mViewStub = (ViewStub) findViewById(R.id.main_tabs);
        mViewStub.inflate();

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons(tabLayout);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                TextView view = (TextView) tab.getCustomView();
                view.setCompoundDrawablesWithIntrinsicBounds(0, tabIconsSelected[tab.getPosition()], 0, 0);
                view.setTextColor(getResources().getColor(R.color.radioBtnCheck));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView view = (TextView) tab.getCustomView();
                view.setCompoundDrawablesWithIntrinsicBounds(0, tabIconsNormal[tab.getPosition()], 0, 0);
                view.setTextColor(getResources().getColor(R.color.radioBtnUnCheck));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public static final String PREFERENCES = "Preferences";
    public static final String RECORD = "Record";
    public static final String HOME = "Home";
    static String[] tabTexts = {HOME, RECORD, PREFERENCES};
    static int[] tabIconsSelected = {R.drawable.tab_home_check, R.drawable.tab_record_check, R.drawable.tab_pref_check};
    static int[] tabIconsNormal = {R.drawable.tab_home_nomal, R.drawable.tab_record_normal, R.drawable.tab_pref_normal};

    private void setupTabIcons(TabLayout tabLayout) {
        for(int i = 0;i<3;i++) {
            createTabView(tabLayout, i);
        }
    }

    private static final int initallySelectedTab = /*BuildConfig.DEBUG ? 2 :*/ 0;

    void createTabView(TabLayout tabLayout, int num) {
        boolean selected = num == initallySelectedTab;
        TextView view = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        view.setText(tabTexts[num]);
        view.setCompoundDrawablesWithIntrinsicBounds(0, (selected ? tabIconsSelected : tabIconsNormal)[num], 0, 0);
        view.setTextColor(getResources().getColor(selected ? R.color.radioBtnCheck : R.color.radioBtnUnCheck));
        tabLayout.getTabAt(num).setCustomView(view);
    }

    /**
     * Adding fragments to ViewPager
     * @param viewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        FragmentManager fm = getFragmentManager();
        ViewPagerAdapter adapter = new ViewPagerAdapter(fm);
        MainFragment mainFragment = (MainFragment) fm.findFragmentByTag(HOME);
        if (mainFragment == null) {
            mainFragment = new MainFragment();
        }
        adapter.addFrag(mainFragment, HOME, HOME);
        this.mainFragment = mainFragment;

        RecordFragment recordFragment = (RecordFragment) fm.findFragmentByTag(RECORD);
        if (recordFragment == null) {
            recordFragment = new RecordFragment();
        }
        adapter.addFrag(recordFragment, RECORD, RECORD);

        PrefsFragment prefsFragment = (PrefsFragment) fm.findFragmentByTag(PREFERENCES);
        if (prefsFragment == null) {
            prefsFragment = new PrefsFragment();
        }
        adapter.addFrag(prefsFragment, PREFERENCES, PREFERENCES);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(initallySelectedTab);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        long time = System.currentTimeMillis();
        if (time - this.mLastBackTime < 2000) {
            judgeExit();
        } else {
            this.mLastBackTime = time;
            toast(R.string.exit_by_muilt_click);
        }
        return true;
    }

    private void judgeExit() {
        int mode = mShare.getInt(SharePreference.EXIT_MODE, 0);
        DebugLogger.i("judgeExit", String.valueOf(mode));
        if ((mode & 1) == 0) {
            new ExitDialog().show(getFragmentManager(), null);
        } else {
            exit(mode);
        }
    }

    public void onReceiveCurrentData(Data0x00 data) {
        super.onReceiveCurrentData(data);
        if (data != null) {
            if (this.mData != null) {
                data.totalDistance = this.mData.totalDistance;
            }
            this.mData = data;
        } else {
            this.mData = new Data0x00();
        }
        runOnUiThread(new Runnable() {
            public void run() {
                mainFragment.setData(MainTabsActivity.this.mData);
            }
        });
    }

    public void onReceiveTotalData(float totalDistance) {
        super.onReceiveTotalData(totalDistance);
        if (this.mData == null) {
            this.mData = new Data0x00();
        }
        this.mData.totalDistance = totalDistance;
        runOnUiThread(new Runnable() {
            public void run() {
                mainFragment.setData(MainTabsActivity.this.mData);
            }
        });
    }

    static class FragmentData {
        Fragment fragment;
        String title;
        String tag;

        public FragmentData(Fragment fragment, String title, String tag) {
            this.fragment = fragment;
            this.title = title;
            this.tag = tag;
        }
    }
    public static class ViewPagerAdapter extends PagerAdapter {

        private final List<FragmentData> mFragmentList = new ArrayList<>();


        public ViewPagerAdapter(FragmentManager fm) {
            this.mFragmentManager = fm;
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public Fragment getItem(int position) {
            return mFragmentList.get(position).fragment;
        }

        public String getTag(int position) {
            return mFragmentList.get(position).tag;
        }

        public void addFrag(Fragment fragment, String title, String tag) {
            mFragmentList.add(new FragmentData(fragment, title, tag));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentList.get(position).title;
        }

        private static final String TAG = "FrgStatePagerAdapter";
        private static final boolean DEBUG = false;

        private final FragmentManager mFragmentManager;
        private FragmentTransaction mCurTransaction = null;

        private ArrayList<Fragment.SavedState> mSavedState = new ArrayList<Fragment.SavedState>();
        private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
        private Fragment mCurrentPrimaryItem = null;

        @Override
        public void startUpdate(ViewGroup container) {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // If we already have this item instantiated, there is nothing
            // to do.  This can happen when we are restoring the entire pager
            // from its saved state, where the fragment manager has already
            // taken care of restoring the fragments we previously had instantiated.
            if (mFragments.size() > position) {
                Fragment f = mFragments.get(position);
                if (f != null) {
                    return f;
                }
            }

            if (mCurTransaction == null) {
                mCurTransaction = mFragmentManager.beginTransaction();
            }

            Fragment fragment = getItem(position);
            if (DEBUG) Log.v(TAG, "Adding item #" + position + ": f=" + fragment);
            if (mSavedState.size() > position) {
                Fragment.SavedState fss = mSavedState.get(position);
                if (fss != null) {
                    fragment.setInitialSavedState(fss);
                }
            }
            while (mFragments.size() <= position) {
                mFragments.add(null);
            }
            FragmentCompat.setMenuVisibility(fragment, false);
            FragmentCompat.setUserVisibleHint(fragment, false);
            mFragments.set(position, fragment);
            mCurTransaction.add(container.getId(), fragment, getTag(position));

            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Fragment fragment = (Fragment)object;

            if (mCurTransaction == null) {
                mCurTransaction = mFragmentManager.beginTransaction();
            }
            if (DEBUG) Log.v(TAG, "Removing item #" + position + ": f=" + object
                    + " v=" + ((Fragment)object).getView());
            while (mSavedState.size() <= position) {
                mSavedState.add(null);
            }
            mSavedState.set(position, mFragmentManager.saveFragmentInstanceState(fragment));
            mFragments.set(position, null);

            mCurTransaction.remove(fragment);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            Fragment fragment = (Fragment)object;
            if (fragment != mCurrentPrimaryItem) {
                if (mCurrentPrimaryItem != null) {
                    FragmentCompat.setMenuVisibility(mCurrentPrimaryItem, false);
                    FragmentCompat.setUserVisibleHint(mCurrentPrimaryItem, false);
                }
                if (fragment != null) {
                    FragmentCompat.setMenuVisibility(fragment, true);
                    FragmentCompat.setUserVisibleHint(fragment, true);
                }
                mCurrentPrimaryItem = fragment;
            }
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            if (mCurTransaction != null) {
                mCurTransaction.commitAllowingStateLoss();
                mCurTransaction = null;
                mFragmentManager.executePendingTransactions();
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return ((Fragment)object).getView() == view;
        }

        @Override
        public Parcelable saveState() {
            Bundle state = null;
            if (mSavedState.size() > 0) {
                state = new Bundle();
                Fragment.SavedState[] fss = new Fragment.SavedState[mSavedState.size()];
                mSavedState.toArray(fss);
                state.putParcelableArray("states", fss);
            }
            for (int i=0; i<mFragments.size(); i++) {
                Fragment f = mFragments.get(i);
                if (f != null && f.isAdded()) {
                    if (state == null) {
                        state = new Bundle();
                    }
                    String key = "f" + i;
                    mFragmentManager.putFragment(state, key, f);
                }
            }
            return state;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
            if (state != null) {
                Bundle bundle = (Bundle)state;
                bundle.setClassLoader(loader);
                Parcelable[] fss = bundle.getParcelableArray("states");
                mSavedState.clear();
                mFragments.clear();
                if (fss != null) {
                    for (int i=0; i<fss.length; i++) {
                        mSavedState.add((Fragment.SavedState)fss[i]);
                    }
                }
                Iterable<String> keys = bundle.keySet();
                for (String key: keys) {
                    if (key.startsWith("f")) {
                        int index = Integer.parseInt(key.substring(1));
                        Fragment f = mFragmentManager.getFragment(bundle, key);
                        if (f != null) {
                            while (mFragments.size() <= index) {
                                mFragments.add(null);
                            }
                            FragmentCompat.setMenuVisibility(f, false);
                            mFragments.set(index, f);
                        } else {
                            Log.w(TAG, "Bad fragment at key " + key);
                        }
                    }
                }
            }
        }
    }


}
