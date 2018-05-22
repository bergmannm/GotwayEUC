package app.gotway.euc.ui.activity;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.gotway.euc.BuildConfig;
import app.gotway.euc.R;
import app.gotway.euc.ble.profile.BleProfileActivity;
import app.gotway.euc.data.Data0x00;
import app.gotway.euc.share.SharePreference;
import app.gotway.euc.ui.fragment.ExitDialog;
import app.gotway.euc.ui.fragment.MainFragment;
import app.gotway.euc.ui.fragment.PrefsFragment;
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

//        if (BuildConfig.DEBUG) {
            initMainActivity();
//        } else {
//            final ViewStub mViewStub = (ViewStub) findViewById(R.id.flash);
//            mViewStub.inflate();
//            View v = findViewById(R.id.stub_flash);
//            Animation anim = AnimationUtils.loadAnimation(this, R.anim.flash_alpha);
//            anim.setAnimationListener(new Animation.AnimationListener() {
//                public void onAnimationStart(Animation animation) {
//                }
//
//                public void onAnimationRepeat(Animation animation) {
//                }
//
//                public void onAnimationEnd(Animation animation) {
//                    initMainActivity();
//                }
//            });
//            v.startAnimation(anim);
//        }
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

    static String[] tabTexts = {"Home", "Preferences"};
    static int[] tabIconsSelected = {R.drawable.tab_home_check, R.drawable.tab_pref_check};
    static int[] tabIconsNormal = {R.drawable.tab_home_nomal, R.drawable.tab_pref_normal};

    private void setupTabIcons(TabLayout tabLayout) {
        tabLayout.getTabAt(0).setCustomView(createTabView(0, true));
        tabLayout.getTabAt(1).setCustomView(createTabView(1, false));
    }

    View createTabView(int num, boolean selected) {
        TextView view = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        view.setText(tabTexts[num]);
        view.setCompoundDrawablesWithIntrinsicBounds(0, (selected ? tabIconsSelected : tabIconsNormal)[num], 0, 0);
        view.setTextColor(getResources().getColor(selected ? R.color.radioBtnCheck : R.color.radioBtnUnCheck));
        return view;
    }

    /**
     * Adding fragments to ViewPager
     * @param viewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());
        adapter.addFrag(this.mainFragment = new MainFragment(), "Home");
        adapter.addFrag(new PrefsFragment(), "Preferences");
        viewPager.setAdapter(adapter);
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
                if (mainFragment != null) {
                    mainFragment.setData(MainTabsActivity.this.mData);
                }
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
                if (mainFragment != null) {
                    mainFragment.setData(MainTabsActivity.this.mData);
                }
            }
        });
    }

    public static class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();


        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

}
