package app.gotway.euc.ui.fragment;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import java.util.HashMap;
import java.util.Map;

import app.gotway.euc.R;
import app.gotway.euc.util.DebugLogger;

public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        //PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
        initSummary(getPreferenceScreen());

        findPreference("speed_divider").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean ok = false;
                try {
                    float parsedValue = Float.parseFloat(String.valueOf(newValue));
                    ok = parsedValue >= 1.0 && parsedValue <= 2.0;
                } catch (Exception e) {
                    DebugLogger.e("PrefsFragment", e.toString(), e);
                }
                if (!ok) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.error_title))
                            .setMessage(getString(R.string.pref_speed_divider_err))
                            .setIcon(android.R.drawable.ic_delete)
                            .setNeutralButton(android.R.string.ok, null)
                            .show();
                }
                return ok;
            }
        });
        findPreference("current_divider").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean ok = false;
                try {
                    float parsedValue = Float.parseFloat(String.valueOf(newValue));
                    ok = parsedValue >= 100.0 && parsedValue <= 400.0;
                } catch (Exception e) {
                    DebugLogger.e("PrefsFragment", e.toString(), e);
                }
                if (!ok) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.error_title))
                            .setMessage(getString(R.string.pref_current_divider_err))
                            .setIcon(android.R.drawable.ic_delete)
                            .setNeutralButton(android.R.string.ok, null)
                            .show();
                }
                return ok;
            }
        });

        Preference.OnPreferenceChangeListener alarm_speed_listener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean ok = false;
                if (newValue == null || "".equals(newValue)) {
                    ok = true;
                } else {
                    try {
                        float parsedValue = Float.parseFloat(String.valueOf(newValue));
                        ok = parsedValue >= 1.0 && parsedValue <= 40.0;
                    } catch (Exception e) {
                        DebugLogger.e("PrefsFragment", e.toString(), e);
                    }
                }
                if (!ok) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.error_title))
                            .setMessage(getString(R.string.pref_alarm_speed_err))
                            .setIcon(android.R.drawable.ic_delete)
                            .setNeutralButton(android.R.string.ok, null)
                            .show();
                }
                return ok;
            }
        };
        findPreference("vib_alarm_speed1").setOnPreferenceChangeListener(alarm_speed_listener);
        findPreference("vib_alarm_speed2").setOnPreferenceChangeListener(alarm_speed_listener);
        findPreference("vib_alarm_speed3").setOnPreferenceChangeListener(alarm_speed_listener);
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
}
