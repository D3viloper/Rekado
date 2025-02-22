package com.pavelrekun.rekado.screens.settings_activity.main_settings_fragment

import android.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.pavelrekun.rekado.R
import com.pavelrekun.rekado.base.BaseActivity
import com.pavelrekun.rekado.services.Logger
import com.pavelrekun.rekado.services.dialogs.Dialogs
import com.pavelrekun.rekado.services.payloads.PayloadHelper
import com.pavelrekun.rekado.services.utils.Utils
import com.pavelrekun.siga.pickers.theme.ThemePickerFragment
import kotlinx.android.synthetic.main.activity_settings.*

class MainSettingsView(private val activity: BaseActivity, private val fragment: PreferenceFragmentCompat) : MainSettingsContract.View {

    private lateinit var appearanceTheme: Preference
    private lateinit var appearanceAccentColor: Preference

    private lateinit var autoInjectorEnable: CheckBoxPreference
    private lateinit var autoInjectorPayload: ListPreference

    private lateinit var payloadsHideBundled: Preference
    private lateinit var payloadsResetPreference: Preference

    init {
        onCreatePreferences()
    }

    override fun onCreatePreferences() {
        preparePreferences()

        initAppearanceCategory()
        initAutoInjectorCategory()
        initPayloadsCategory()
    }

    override fun preparePreferences() {
        payloadsHideBundled = fragment.findPreference("payloads_hide_bundled")
        payloadsResetPreference = fragment.findPreference("payloads_reset")

        autoInjectorEnable = fragment.findPreference("auto_injector_enable") as CheckBoxPreference
        autoInjectorPayload = fragment.findPreference("auto_injector_payload") as ListPreference

        appearanceTheme = fragment.findPreference("appearance_theme")
        appearanceAccentColor = fragment.findPreference("appearance_accent_color")
    }

    override fun initAppearanceCategory() {
        val themePickerFragment = ThemePickerFragment().apply { setClickListener { openUpdatingMessage() } }

        appearanceTheme.setOnPreferenceClickListener {
            openSettingsFragment(themePickerFragment)

            true
        }

        appearanceAccentColor.setOnPreferenceChangeListener { _, _ ->
            openUpdatingMessage()
            true
        }
    }

    override fun initAutoInjectorCategory() {
        autoInjectorEnable.setTitle(if (autoInjectorEnable.isChecked) R.string.settings_auto_injector_status_title_enabled else R.string.settings_auto_injector_status_title_disabled)

        autoInjectorPayload.entryValues = PayloadHelper.getNames().toTypedArray()
        autoInjectorPayload.entries = PayloadHelper.getNames().toTypedArray()
        if (autoInjectorPayload.value == null && PayloadHelper.getNames().isNotEmpty()) autoInjectorPayload.setValueIndex(0)
        autoInjectorPayload.isEnabled = autoInjectorEnable.isChecked

        autoInjectorEnable.setOnPreferenceChangeListener { _, newValue ->
            autoInjectorPayload.isEnabled = newValue as Boolean

            if (newValue) {
                Logger.info("\"Auto injector\" enabled!")
                autoInjectorEnable.setTitle(R.string.settings_auto_injector_status_title_enabled)
            } else {
                Logger.info("\"Auto injector\" disabled!")
                autoInjectorEnable.setTitle(R.string.settings_auto_injector_status_title_disabled)
            }

            true
        }
    }

    override fun initPayloadsCategory() {
        payloadsHideBundled.setOnPreferenceChangeListener { _, newValue ->
            PayloadHelper.clearBundled()
            true
        }

        payloadsResetPreference.setOnPreferenceClickListener {
            val dialog = Dialogs.showPayloadsResetDialog(activity)

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                PayloadHelper.clearFolderWithoutBundled()
                dialog.dismiss()

                Logger.info("Payloads database cleaned!")
            }
            true
        }
    }

    override fun openSettingsFragment(fragment: Fragment) {
        activity.supportFragmentManager.apply {
            beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.settingsFragmentFrame, fragment, fragment::class.java.simpleName).addToBackStack(null).commit()
        }
    }

    override fun openUpdatingMessage() {
        Snackbar.make(activity.settingsFragmentFrame, R.string.settings_appearance_updating, 500).addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                Utils.restartActivity(activity)
            }
        }).show()
    }
}