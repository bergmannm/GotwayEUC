package app.gotway.euc.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.gotway.euc.BuildConfig;
import app.gotway.euc.R;
import app.gotway.euc.ble.cmd.CMDMgr;
import app.gotway.euc.ble.profile.BleProfileActivity;
import app.gotway.euc.ui.pref.SeekbarPreferenceDialog;
import app.gotway.euc.util.DebugLogger;

public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        for(PrefItem item:prefItems) {
            if (item.key.equals(key)) {
                return act.writeData(item.cmds[((Integer) newValue)]);
            }
        }
        return false;
    }

    class NumberOnPreferenceChangeListener implements Preference.OnPreferenceChangeListener{
        float minValue, maxValue;
        int errTitleMsgId;
        boolean allowEmpty;
        NumberOnPreferenceChangeListener(float minValue, float maxValue, int errTitleMsgId) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.errTitleMsgId = errTitleMsgId;
        }
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean ok = false;
            if (newValue == null || "".equals(newValue)) {
                ok = true;
            } else {
                try {
                    float parsedValue = Float.parseFloat(String.valueOf(newValue));
                    ok = parsedValue >= minValue && parsedValue <= maxValue;
                } catch (Exception e) {
                    DebugLogger.e("PrefsFragment", e.toString(), e);
                }
            }
            if (!ok) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.error_title))
                        .setMessage(getString(errTitleMsgId))
                        .setIcon(android.R.drawable.ic_delete)
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }
            return ok;
        }
    }

    private BleProfileActivity act;

    static class PrefItem {
        String key;
        int[] texts;
        byte[][] cmds;

        public PrefItem(String key, int[] texts, byte[][] cmds) {
            this.key = key;
            this.texts = texts;
            this.cmds = cmds;
        }
    }

    List<PrefItem> prefItems = new ArrayList<>();

    public PrefsFragment(){
        prefItems.add(new PrefItem("ride_mode",
                new int[]{R.string.setModeExplode, R.string.setModeComfortable, R.string.setModeSoft},
                new byte[][]{CMDMgr.MODE_EXPLORE, CMDMgr.MODE_COMFORTABLE, CMDMgr.MODE_SOFT}
        ));
        prefItems.add(new PrefItem("tiltback_speed",
                new int[]{R.string.setPaddleSpeedA, R.string.setPaddleSpeedS, R.string.setPaddleSpeedD, R.string.setPaddleSpeedF, R.string.setPaddleSpeedG, R.string.setPaddleSpeedH, R.string.setPaddleSpeedJ, R.string.setPaddleSpeedK, R.string.setPaddleSpeedL, R.string.pref_tiltback_off},
                new byte[][]{CMDMgr.PADDLE_A, CMDMgr.PADDLE_S, CMDMgr.PADDLE_D, CMDMgr.PADDLE_F, CMDMgr.PADDLE_G, CMDMgr.PADDLE_H, CMDMgr.PADDLE_J, CMDMgr.PADDLE_K, CMDMgr.PADDLE_L, CMDMgr.PADDLE_CANCEL}
        ));
        prefItems.add(new PrefItem("alarm_speed",
                new int[] {R.string.pref_alarm1, R.string.pref_alarm2, R.string.pref_alarm3},
                new byte[][] {CMDMgr.ALARM_OPEN, CMDMgr.ALARM_FIRST, CMDMgr.ALARM_SECOND}
        ));
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.act = (BleProfileActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (BuildConfig.DEBUG) {
            addPreferencesFromResource(R.xml.dummy);
            PreferenceScreen screen = this.getPreferenceScreen();
            Preference crashPref = new Preference(screen.getContext());
            crashPref.setTitle("Let's crash");
            crashPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DebugLogger.w(this.getClass().getSimpleName(), "Fuck fuck fuck fuck");
                    //onPreferenceClick(null);
                    throw new NullPointerException();
                }
            });
            screen.addPreference(crashPref);
        }

        addPreferencesFromResource(R.xml.preferences);
        //PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
        initSummary(getPreferenceScreen());

        findPreference("speed_divider").setOnPreferenceChangeListener(new NumberOnPreferenceChangeListener(1.0f, 2.0f, R.string.pref_speed_divider_err));
        findPreference("current_divider").setOnPreferenceChangeListener(new NumberOnPreferenceChangeListener(100.0f, 400.0f, R.string.pref_current_divider_err));

        Preference.OnPreferenceChangeListener alarm_speed_listener =
                new NumberOnPreferenceChangeListener(1.0f, 40.0f, R.string.pref_alarm_speed_err);
        findPreference("vib_alarm_speed1").setOnPreferenceChangeListener(alarm_speed_listener);
        findPreference("vib_alarm_speed2").setOnPreferenceChangeListener(alarm_speed_listener);
        findPreference("vib_alarm_speed3").setOnPreferenceChangeListener(alarm_speed_listener);

        for(PrefItem item:prefItems) {
            findPreference(item.key).setOnPreferenceChangeListener(this);
        }
        findPreference("horizontal_calibration").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return act.writeData(CMDMgr.CORRECT_START);
            }
        });
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        updatePrefSummary(pref);
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    Map<String, CharSequence> defaultPrefSummaryTexts = new HashMap<>();

    private void updatePrefSummary(Preference p) {
        /*
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
        }
        if (p instanceof MultiSelectListPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText());
        }
        */
        CharSequence defaultSummaryText;
        String key = p.getKey();
        if ("speed_divider".equals(key) || "current_divider".equals(key)) {
            if (defaultPrefSummaryTexts.containsKey(key)) {
                defaultSummaryText = defaultPrefSummaryTexts.get(key);
            } else {
                defaultSummaryText = p.getSummary();
                defaultPrefSummaryTexts.put(key, p.getSummary());
            }
            if (p instanceof EditTextPreference) {
                EditTextPreference editTextPref = (EditTextPreference) p;
                p.setSummary(defaultSummaryText + editTextPref.getText());
            }
        }
        for(PrefItem item:prefItems) {
            if (item.key.equals(key)) {
                p.setSummary(getString(item.texts[((SeekbarPreferenceDialog)p).getValue()]));
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            Activity a = getActivity();
            if(a != null) {
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            }
        }
    }

}
