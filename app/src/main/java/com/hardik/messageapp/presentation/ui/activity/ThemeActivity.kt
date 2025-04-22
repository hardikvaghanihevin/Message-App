package com.hardik.messageapp.presentation.ui.activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.ActivityThemeBinding
import com.hardik.messageapp.databinding.ItemOptionSelectLay01Binding
import com.hardik.messageapp.util.AppTheme
import com.hardik.messageapp.util.Constants.BASE_TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ThemeActivity : BaseActivity() {
    private val TAG = BASE_TAG + ThemeActivity::class.java.simpleName

    private lateinit var binding: ActivityThemeBinding
    private var selectedAppTheme: AppTheme = AppTheme.SYSTEM
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSettingsUI()

        binding.toolbarBack.setOnClickListener { if (!handleOnSoftBackPress()){ onBackPressedDispatcher.onBackPressed() } }

        binding.toolbarDone.setOnClickListener {
            if (!handleOnSoftBackPress()) { onBackPressedDispatcher.onBackPressed() } //finish()//recreate()
            lifecycleScope.launch {
                //delay(100) // Wait for theme to apply
                AppTheme.mTheme = selectedAppTheme
            }
        }
    }

    private fun setupSettingsUI() {
        binding.apply {
            val lightBinding = ItemOptionSelectLay01Binding.bind(includedItemLightMode.root)
            val darkBinding = ItemOptionSelectLay01Binding.bind(includedItemDarkMode.root)
            val systemBinding = ItemOptionSelectLay01Binding.bind(includedItemSystemMode.root)

            val selectedItem = R.drawable.ic_round_selected_item

            val themeItems = listOf(lightBinding, darkBinding, systemBinding)
            resetSelections(themeItems)

            val appTheme = AppTheme.mTheme
            selectedAppTheme = appTheme

            when (selectedAppTheme) {
                AppTheme.DARK -> markSelected(darkBinding, R.drawable.theme_img_night, selectedItem)
                AppTheme.LIGHT -> markSelected(lightBinding, R.drawable.theme_img_day, selectedItem)
                AppTheme.SYSTEM -> markSelected(systemBinding, R.drawable.theme_img_default, selectedItem)
            }

            lightBinding.setup(AppTheme.LIGHT, R.drawable.theme_img_day, themeItems, selectedItem)
            darkBinding.setup(AppTheme.DARK, R.drawable.theme_img_night, themeItems, selectedItem)
            systemBinding.setup(AppTheme.SYSTEM, R.drawable.theme_img_default, themeItems, selectedItem)

            systemBinding.itemOptionDivider.root.visibility = View.GONE
        }
    }
    //TextViewCompat.setTextAppearance(tvSettingItemTitle, R.style.ItemOptionSelectLay_02_ItemInfoText_1)

    private fun resetSelections(listOf: List<ItemOptionSelectLay01Binding>) {
        Glide.with(binding.ivTheme)
            .load(R.drawable.theme_img_default)
            .into(binding.ivTheme)

        val unselectedItem = R.drawable.ic_round_unselected_item
        listOf.forEach { it.tvItemOptionTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, unselectedItem, 0) }
    }

    private fun ItemOptionSelectLay01Binding.setup(
        theme: AppTheme,
        imageRes: Int,
        allItems: List<ItemOptionSelectLay01Binding>,
        selectedDrawable: Int
    ) {
        tvItemOptionTitle.text = when (theme) {
            AppTheme.LIGHT -> root.context.getString(R.string.light_mode)
            AppTheme.DARK -> root.context.getString(R.string.dark_mode)
            AppTheme.SYSTEM -> root.context.getString(R.string.system_mode)
        }

        root.setOnClickListener {
            resetSelections(allItems)
            markSelected(this, imageRes, selectedDrawable)
            selectedAppTheme = theme
        }
    }

    private fun markSelected(binding: ItemOptionSelectLay01Binding, imageRes: Int, drawableRes: Int) {
        Glide.with(binding.root.context)
            .load(imageRes)
            .into(binding.root.rootView.findViewById(R.id.iv_theme))

        binding.tvItemOptionTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawableRes, 0)
    }
    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}