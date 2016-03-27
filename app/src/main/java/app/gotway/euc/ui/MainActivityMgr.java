package app.gotway.euc.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import app.gotway.euc.BuildConfig;
import app.gotway.euc.R;
import app.gotway.euc.data.Data0x00;
import app.gotway.euc.ui.fragment.AboutFragment;
import app.gotway.euc.ui.fragment.MainFragment;
import app.gotway.euc.ui.fragment.PrefsFragment;

public class MainActivityMgr implements OnCheckedChangeListener {
    private Activity activity;
    private Fragment mAboutFragment;
    private Fragment mLastFragment;
    private MainFragment mMainFragment;
    private Fragment mSettingFragment2;
    private ViewStub mViewStub;

    private PrefsFragment mPrefsFragment;
    // private SettingsFragment mSettingsFragment;

    public MainActivityMgr(Activity act) {
        this.activity = act;
        this.mViewStub = (ViewStub) act.findViewById(R.id.flash);
        this.mViewStub.inflate();
        if (BuildConfig.DEBUG) {
            initMainActivity();
        } else {
            View v = act.findViewById(R.id.stub_flash);
            Animation anim = AnimationUtils.loadAnimation(this.activity, R.anim.flash_alpha);
            anim.setAnimationListener(new AnimationListener() {
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
        mViewStub.setVisibility(View.GONE);
        mViewStub = (ViewStub) activity.findViewById(R.id.main);
        mViewStub.inflate();
        mViewStub = null;

        ((RadioGroup) activity.findViewById(R.id.radioGp)).setOnCheckedChangeListener(this);
        toggleRadio(BuildConfig.DEBUG ? R.id.prefRadio : R.id.mainRudio);
    }

    void toggleRadio(int radioId) {
        RadioButton radioButton = ((RadioButton) activity.findViewById(radioId));
        if (radioButton.isChecked()) {
            onCheckedChanged(null, radioId);
        } else {
            radioButton.toggle();
        }
    }


    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.mainRudio:
                showMain();
                break;
            case R.id.settingRudio:
                showSettings();
                break;
            case R.id.prefRadio:
                showPrefs();
                break;
            case R.id.aboutRudio:
                if (this.mAboutFragment == null) {
                    this.mAboutFragment = new AboutFragment();
                }
                changeFragment(this.mAboutFragment);
                break;
            default:
        }
    }

    private void showPrefs() {
        if (this.mPrefsFragment == null) {
            this.mPrefsFragment = new PrefsFragment();
        }
        changeFragment(this.mPrefsFragment);
    }

    private void showSettings() {
        /*
        if (this.mSettingFragment2 == null) {
            this.mSettingFragment2 = new SettingFragment2();
        }
        changeFragment(this.mSettingFragment2);
        */
//        if (this.mSettingsFragment == null) {
//            this.mSettingsFragment = new SettingsFragment();
//        }
//        changeFragment(this.mSettingsFragment);
    }

    private void showMain() {
        if (this.mMainFragment == null) {
            this.mMainFragment = new MainFragment();
        }
        changeFragment(this.mMainFragment);
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
