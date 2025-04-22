package com.hardik.messageapp.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.lifecycleScope
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.ActivitySettingsBinding
import com.hardik.messageapp.databinding.ItemOptionSelectLay02Binding
import com.hardik.messageapp.presentation.ui.viewmodel.SettingViewModel
import com.hardik.messageapp.util.AnimationViewHelper
import com.hardik.messageapp.util.CollapsingToolbarStateManager
import com.hardik.messageapp.util.Constants.BASE_TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {
    private val TAG = BASE_TAG + SettingsActivity::class.java.simpleName

    lateinit var binding: ActivitySettingsBinding

    val settingViewModel: SettingViewModel by viewModels()

    private lateinit var toolbarStateManager: CollapsingToolbarStateManager
    private lateinit var toolbarStateChangeListener: CollapsingToolbarStateManager.OnStateChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSettingsUI()
        //region toolbar selected item count management & Toolbar selected count management
        lifecycleScope.launch {
            settingViewModel.toolbarCollapsedState.collectLatest {toolbarState ->
                val isCollapsed = toolbarState in listOf(CollapsingToolbarStateManager.STATE_COLLAPSED, )
                val isExpanded = toolbarState in listOf(CollapsingToolbarStateManager.STATE_EXPANDED, CollapsingToolbarStateManager.STATE_INTERMEDIATE)

                binding.toolbarTitle.apply {
                    val visible = View.VISIBLE.takeIf { isCollapsed } ?: View.GONE
                    val duration = if (visible == View.VISIBLE) 300L else 100L
                    AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
                }

                binding.expandedContent.apply {
                    val visible = View.VISIBLE.takeIf { isExpanded } ?: View.GONE
                    val duration = if (visible == View.VISIBLE) 300L else 100L
                    AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
                }
            }
        }
        //endregion toolbar selected item count management & Toolbar selected count management

        //region toolbar management
        /** Initialize the manager */
        toolbarStateManager = CollapsingToolbarStateManager(binding.appbarLayout)

        /** Create an anonymous implementation of the listener */
        toolbarStateChangeListener = object : CollapsingToolbarStateManager.OnStateChangeListener { override fun onStateChanged(newState: Int) { settingViewModel.onToolbarStateChanged(newState) } }

        /** Register the anonymous listener */
        toolbarStateManager.addOnStateChangeListener(toolbarStateChangeListener)

        binding.toolbarBack.setOnClickListener { if (!handleOnSoftBackPress()){ onBackPressedDispatcher.onBackPressed() } }
        //endregion toolbar management
    }

    private fun setupSettingsUI(){
        binding.apply {
            val languageBinding = ItemOptionSelectLay02Binding.bind(includedItemLanguage.root)

            val swipeActionsBinding = ItemOptionSelectLay02Binding.bind(includedItemSwipeActions.root)
            val themesBinding = ItemOptionSelectLay02Binding.bind(includedItemThemes.root)
            val fontSizeBinding = ItemOptionSelectLay02Binding.bind(includedItemFontSize.root)

            val afterCallFeatureBinding = ItemOptionSelectLay02Binding.bind(includedItemAfterCallFeature.root)
            val backupRestoreBinding = ItemOptionSelectLay02Binding.bind(includedItemBackupAndRestore.root)

            val shareAppBinding = ItemOptionSelectLay02Binding.bind(includedItemShareApp.root)
            val rateAppBinding = ItemOptionSelectLay02Binding.bind(includedItemRateApp.root)
            val privacyPolicyBinding = ItemOptionSelectLay02Binding.bind(includedItemPrivacyPolicy.root)

            /** Language */
            languageBinding.apply {
                tvItemOptionTitle.apply { text = getString(R.string.language) }
                tvItemOptionInfo.apply {
                    text = getString(R.string.english)
                    TextViewCompat.setTextAppearance(tvItemOptionInfo, R.style.ItemOptionSelectLay_02_ItemInfoText_1)
                }
                itemOptionDivider.root.visibility = View.GONE
                root.setOnClickListener { startActivity(Intent(this@SettingsActivity, LanguageActivity::class.java)) }
            }

            /** Swipe action */
            swipeActionsBinding.apply {
                tvItemOptionTitle.apply { text = getString(R.string.swipe_actions) }
                tvItemOptionInfo.apply { text = getString(R.string.configure_swipe_actions_for_conversations) }

                root.setOnClickListener { startActivity(Intent(this@SettingsActivity, SwipeActionActivity::class.java)) }
            }

            /** Themes */
            themesBinding.apply {
                tvItemOptionTitle.apply { text = getString(R.string.themes) }
                tvItemOptionInfo.apply { text = getString(R.string.default_) }

                root.setOnClickListener { startActivity(Intent(this@SettingsActivity, ThemeActivity::class.java)) }
            }

            /** Font size */
            fontSizeBinding.apply {
                tvItemOptionTitle.apply { text = getString(R.string.font_size) }
                tvItemOptionInfo.apply { text = getString(R.string.normal) }
                itemOptionDivider.root.visibility = View.GONE

                root.setOnClickListener {}
            }

            /** After call feature */
            afterCallFeatureBinding.apply {
                tvItemOptionTitle.apply { text = getString(R.string.after_call_feature) }
                tvItemOptionInfo.apply { text = getString(R.string.default_) }

                root.setOnClickListener { startActivity(Intent(this@SettingsActivity, AfterCallActivity::class.java)) }
            }

            /** Backup & Restore */
            backupRestoreBinding.apply {
                tvItemOptionTitle.apply { text = getString(R.string.backup_restore) }
                tvItemOptionInfo.apply { text = getString(R.string.default_) }
                itemOptionDivider.root.visibility = View.GONE

                root.setOnClickListener {}
            }

            /** Share app */
            shareAppBinding.apply {
                tvItemOptionTitle.apply { text = getString(R.string.share_app) }
                tvItemOptionInfo.visibility = View.GONE
                root.setOnClickListener {}
            }

            /** Rate app */
            rateAppBinding.apply {
                tvItemOptionTitle.apply { text = getString(R.string.rate_app) }
                tvItemOptionInfo.visibility = View.GONE
                root.setOnClickListener {}
            }

            /** Privacy policy */
            privacyPolicyBinding.apply {
                tvItemOptionTitle.apply { text = getString(R.string.privacy_policy) }
                tvItemOptionInfo.visibility = View.GONE
                itemOptionDivider.root.visibility = View.GONE
                root.setOnClickListener {}
            }
        }

    }


    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}