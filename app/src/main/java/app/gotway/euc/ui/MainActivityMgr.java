package app.gotway.euc.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import app.gotway.euc.R;
import app.gotway.euc.data.Data0x00;
import app.gotway.euc.ui.fragment.AboutFragment;
import app.gotway.euc.ui.fragment.MainFragment;
import app.gotway.euc.ui.fragment.SettingFragment2;

public class MainActivityMgr implements OnCheckedChangeListener {
    private Activity activity;
    private FragmentManager fm;
    private Fragment mAboutFragment;
    private Fragment mLastFragment;
    private MainFragment mMainFragment;
    private Fragment mSettingFragment2;
    private ViewStub mViewStub;

    public MainActivityMgr(Activity act) {
        this.activity = act;
        this.mViewStub = (ViewStub) act.findViewById(R.id.flash);
        this.mViewStub.inflate();
        View v = act.findViewById(R.id.stub_flash);
        Animation anim = AnimationUtils.loadAnimation(this.activity, R.anim.flash_alpha);
        anim.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                MainActivityMgr.this.mViewStub.setVisibility(View.GONE);
                MainActivityMgr.this.mViewStub = (ViewStub) MainActivityMgr.this.activity.findViewById(R.id.main);
                MainActivityMgr.this.mViewStub.inflate();
                MainActivityMgr.this.mViewStub = null;
                MainActivityMgr.this.fm = MainActivityMgr.this.activity.getFragmentManager();
                MainActivityMgr mainActivityMgr = MainActivityMgr.this;
                MainActivityMgr mainActivityMgr2 = MainActivityMgr.this;
                MainFragment mainFragment = new MainFragment();
                mainActivityMgr2.mMainFragment = mainFragment;
                mainActivityMgr.mLastFragment = mainFragment;
                MainActivityMgr.this.fm.beginTransaction().add(R.id.container, MainActivityMgr.this.mMainFragment, null).commit();
                ((RadioGroup) MainActivityMgr.this.activity.findViewById(R.id.radioGp)).setOnCheckedChangeListener(MainActivityMgr.this);
            }
        });
        v.startAnimation(anim);
    }

    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.mainRudio /*2131361812*/:
                if (this.mMainFragment == null) {
                    this.mMainFragment = new MainFragment();
                }
                changeFragment(this.mMainFragment);
                break;
            case R.id.settingRudio /*2131361813*/:
                if (this.mSettingFragment2 == null) {
                    this.mSettingFragment2 = new SettingFragment2();
                }
                changeFragment(this.mSettingFragment2);
                break;
            case R.id.aboutRudio /*2131361814*/:
                if (this.mAboutFragment == null) {
                    this.mAboutFragment = new AboutFragment();
                }
                changeFragment(this.mAboutFragment);
                break;
            default:
        }
    }

    private void changeFragment(Fragment newFragment) {
        if (newFragment != null && newFragment != this.mLastFragment) {
            FragmentTransaction transaction = this.activity.getFragmentManager().beginTransaction();
            if (!(this.mLastFragment == null || this.mLastFragment == newFragment)) {
                transaction.detach(this.mLastFragment);
            }
            if (!newFragment.isAdded()) {
                transaction.add(R.id.container, newFragment);
            }
            if (newFragment.isDetached()) {
                transaction.attach(newFragment);
            }
            this.mLastFragment = newFragment;
            transaction.commitAllowingStateLoss();
        }
    }

    public void setData(final Data0x00 data) {
        if (this.mLastFragment == this.mMainFragment) {
            this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    if (MainActivityMgr.this.mMainFragment != null) {
                        MainActivityMgr.this.mMainFragment.setData(data);
                    }
                }
            });
        }
    }
}
