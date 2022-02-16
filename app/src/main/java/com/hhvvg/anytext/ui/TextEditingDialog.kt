package com.hhvvg.anytext.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.hhvvg.anytext.R
import com.hhvvg.anytext.hook.AnyHookZygote.Companion.moduleRes
import com.hhvvg.anytext.utils.dp2px
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        val parentView = LinearLayout(context)
        parentView.gravity = Gravity.CENTER
        parentView.orientation = LinearLayout.VERTICAL
        val pad = dp2px(context, 24.0f).toInt()
        parentView.setPadding(pad, pad, pad, pad)
        applyButton = Button(context)
        originButton = Button(context)
        editText = EditText(context)
        parentView.apply {
            addView(editText)
            addView(originButton)
            addView(applyButton)
        }
        applyButton.text = moduleRes.getText(R.string.apply)
        originButton.text = moduleRes.getText(R.string.perform_origin_click)

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

    override fun show() {
        super.show()
        window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }
}
