package com.hhvvg.anytext.ui

import android.app.AlertDialog
import android.app.AndroidAppHelper
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableString
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.isVisible
import com.hhvvg.anytext.R
import com.hhvvg.anytext.hook.AnyHookZygote.Companion.moduleRes
import com.hhvvg.anytext.utils.APP_HIGHLIGHT_FIELD_NAME
import com.hhvvg.anytext.utils.DEFAULT_SHARED_PREFERENCES_FILE_NAME
import com.hhvvg.anytext.utils.KEY_SHOW_TEXT_BORDER
import com.hhvvg.anytext.utils.appPropertyInject
import com.hhvvg.anytext.utils.clearTextHighlight
import com.hhvvg.anytext.utils.dp2px
import com.hhvvg.anytext.utils.highlightText
import com.hhvvg.anytext.wrapper.IGNORE_HOOK
import com.hhvvg.anytext.wrapper.TextViewOnClickWrapper

private const val TAG = "TextEditingDialog"

class TextEditingDialog(
    context: Context,
    private val textView: TextView,
    private val onClickListener: View.OnClickListener? = null
) : AlertDialog(context) {
    private lateinit var applyButton: Button
    private lateinit var originButton: Button
    private lateinit var editText: EditText
    private lateinit var highlightTextCheckBox: CheckBox
    private val spInstance: SharedPreferences by lazy {
        context.getSharedPreferences(
            DEFAULT_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE
        )
    }
    private var showTextHighlight: Boolean = false
        get() = spInstance.getBoolean(KEY_SHOW_TEXT_BORDER, false)
        set(value) {
            field = value
            val edit = spInstance.edit()
            edit.putBoolean(KEY_SHOW_TEXT_BORDER, value)
            edit.apply()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        val parentView = LinearLayout(context)
        parentView.gravity = Gravity.CENTER
        parentView.orientation = LinearLayout.VERTICAL
        val padding = dp2px(context, 24.0f).toInt()
        parentView.setPadding(padding, padding, padding, padding)
        applyButton = Button(context)
        originButton = Button(context)
        editText = EditText(context)
        highlightTextCheckBox = CheckBox(context)
        applyButton.tag = IGNORE_HOOK
        originButton.tag = IGNORE_HOOK
        highlightTextCheckBox.tag = IGNORE_HOOK
        editText.tag = IGNORE_HOOK
        parentView.apply {
            addView(editText)
            addView(originButton)
            addView(applyButton)
            addView(highlightTextCheckBox)
        }
        applyButton.text = moduleRes.getText(R.string.apply)
        originButton.text = moduleRes.getText(R.string.perform_origin_click)
        highlightTextCheckBox.text = moduleRes.getText(R.string.highlight_text)

        highlightTextCheckBox.isChecked = showTextHighlight
        highlightTextCheckBox.setOnCheckedChangeListener { _, isChecked ->
            showTextHighlight = isChecked
            appPropertyInject(AndroidAppHelper.currentApplication(), APP_HIGHLIGHT_FIELD_NAME, isChecked)
            val rootView = textView.rootView as ViewGroup
            dfsHighlightText(rootView, isChecked)
        }

        applyButton.setOnClickListener {
            val text = editText.text.toString()
            if (text.isEmpty()) {
                Toast.makeText(
                    context,
                    moduleRes.getString(R.string.text_cannot_be_empty),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            textView.text = SpannableString(text)
            Toast.makeText(
                context,
                moduleRes.getString(R.string.successfully_modified),
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
        }
        if (onClickListener == null) {
            originButton.isVisible = false
        } else {
            originButton.setOnClickListener {
                if (onClickListener is TextViewOnClickWrapper) {
                    onClickListener.performOriginClick()
                } else {
                    onClickListener.onClick(textView)
                }
                dismiss()
            }
        }
        val originText = textView.text.toString()
        editText.setText(originText)
        setContentView(parentView)
    }

    private fun dfsHighlightText(parent: ViewGroup, highlight: Boolean) {
        for (child in parent.children) {
            if (child is ViewGroup) {
                dfsHighlightText(child, highlight)
                continue
            }
            if (child !is TextView) {
                continue
            }
            if (highlight) {
                highlightText(child)
            } else {
                clearTextHighlight(child)
            }
        }
    }

    override fun show() {
        super.show()
        window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }
}
