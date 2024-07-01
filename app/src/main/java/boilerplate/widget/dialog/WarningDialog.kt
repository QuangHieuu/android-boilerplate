package boilerplate.widget.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import boilerplate.R
import boilerplate.widget.customText.TextViewFont
import com.google.android.material.button.MaterialButton
import java.lang.ref.WeakReference

class WarningDialog : DialogFragment() {
    private var mTitleChar: String? = null
    private var mDescriptionChar: String? = null
    private var mConfirmChar: String? = null
    private var mCancelChar: String? = null
    private var mTitleRes = 0
    private var mDescriptionRes = 0
    private var mConfirmRes = 0
    private var mConfirmDrawable = 0
    private var mListener: View.OnClickListener? = null
    private var mCancel: View.OnClickListener? = null
    private var mCustomView: WeakReference<View>? = null

    private var mIsHideConfirm = false

    fun reset() {
        mCustomView = null
        mTitleChar = null
        mDescriptionChar = null
        mConfirmChar = null
        mCancelChar = null
        mTitleRes = 0
        mDescriptionRes = 0
        mConfirmRes = 0
        mConfirmDrawable = 0
        mIsHideConfirm = false
    }

    fun show(
        manager: FragmentManager?,
        title: String?,
        description: String?,
        confirm: String?,
        listener: View.OnClickListener?
    ) {
        mTitleChar = title
        mDescriptionChar = description
        mConfirmChar = confirm
        mListener = listener
        super.show(manager!!, TAG)
    }

    fun show(
        manager: FragmentManager?,
        title: String?,
        description: String?,
        confirm: String?,
        cancelChar: String?,
        confirmDrawable: Int,
        listener: View.OnClickListener?
    ) {
        mTitleChar = title
        mDescriptionChar = description
        mConfirmChar = confirm
        mListener = listener
        mCancelChar = cancelChar
        mConfirmDrawable = confirmDrawable
        super.show(manager!!, TAG)
    }

    fun show(
        manager: FragmentManager?,
        title: String?,
        description: String?,
        confirm: String?,
        confirmDrawable: Int,
        listener: View.OnClickListener?,
        cancel: View.OnClickListener?
    ) {
        mTitleChar = title
        mDescriptionChar = description
        mConfirmChar = confirm
        mListener = listener
        mCancel = cancel
        mConfirmDrawable = confirmDrawable
        super.show(manager!!, TAG)
    }

    fun show(
        manager: FragmentManager?,
        title: String?,
        description: String?,
        confirm: String?,
        confirmDrawable: Int,
        listener: View.OnClickListener?
    ) {
        mTitleChar = title
        mDescriptionChar = description
        mConfirmChar = confirm
        mListener = listener
        mConfirmDrawable = confirmDrawable
        super.show(manager!!, TAG)
    }

    fun show(
        manager: FragmentManager?,
        title: Int,
        description: Int,
        confirm: Int,
        listener: View.OnClickListener?
    ) {
        mTitleRes = title
        mDescriptionRes = description
        mConfirmRes = confirm
        mListener = listener
        super.show(manager!!, TAG)
    }

    fun show(
        manager: FragmentManager?,
        description: Int,
        confirm: Int,
        listener: View.OnClickListener?
    ) {
        mDescriptionRes = description
        mConfirmRes = confirm
        mListener = listener
        super.show(manager!!, TAG)
    }

    fun show(
        manager: FragmentManager?,
        description: String?,
        confirm: String?,
        listener: View.OnClickListener?
    ) {
        mDescriptionChar = description
        mConfirmChar = confirm
        mListener = listener
        super.show(manager!!, TAG)
    }

    fun showWithLayout(
        manager: FragmentManager?,
        title: Int,
        description: Int,
        confirm: Int,
        view: View,
        listener: View.OnClickListener?
    ) {
        mCustomView = WeakReference(view)
        mTitleRes = title
        mDescriptionRes = description
        mConfirmRes = confirm
        mListener = listener
        super.show(manager!!, TAG)
    }

    fun showOnlyWarning(
        manager: FragmentManager?,
        title: String?,
        description: String?,
        confirm: Int
    ) {
        mTitleChar = title
        mDescriptionChar = description
        mConfirmRes = confirm
        mIsHideConfirm = true
        super.show(manager!!, TAG)
    }

    fun showOnlyWarning(manager: FragmentManager?, title: Int, description: Int, confirm: Int) {
        mTitleRes = title
        mDescriptionRes = description
        mConfirmRes = confirm
        mIsHideConfirm = true
        super.show(manager!!, TAG)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false)

        val view = View.inflate(context, R.layout.dialog_base, null)
        builder.setView(view)

        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_base_dialog, null)
        val margin = resources.getDimension(R.dimen.dp_10).toInt()
        val insetDrawable = InsetDrawable(drawable, margin)
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(insetDrawable)

        val title: TextViewFont = view.findViewById(R.id.tv_title)
        val description: TextViewFont = view.findViewById(R.id.tv_description)
        val cancel: MaterialButton = view.findViewById<MaterialButton>(R.id.btn_cancel)
        val confirm: MaterialButton = view.findViewById<MaterialButton>(R.id.btn_confirm)
        val layout = view.findViewById<FrameLayout>(R.id.frame_layout)

        if (mTitleChar != null) {
            title.setText(mTitleChar)
        } else {
            if (mTitleRes != 0) {
                title.setText(mTitleRes)
            } else {
                title.setText(R.string.app_name)
            }
        }

        if (mDescriptionChar != null) {
            description.setText(mDescriptionChar)
        } else {
            if (mDescriptionRes != 0) {
                description.setText(mDescriptionRes)
            }
        }

        if (mConfirmChar != null) {
            confirm.setText(mConfirmChar)
        } else {
            if (mConfirmRes != 0) {
                confirm.setText(mConfirmRes)
            }
        }
        if (mConfirmDrawable != 0) {
            confirm.setBackgroundResource(mConfirmDrawable)
        } else {
            confirm.setBackgroundResource(R.drawable.bg_button_red)
        }

        layout.removeAllViews()
        if (mCustomView != null) {
            layout.visibility = View.VISIBLE
            layout.addView(mCustomView!!.get())
        }

        if (mIsHideConfirm) {
            confirm.setVisibility(View.GONE)
            cancel.setText(mConfirmRes)
        } else {
            if (mCancelChar != null) {
                cancel.setText(mCancelChar)
            }
        }

        confirm.setOnClickListener(View.OnClickListener { v: View? ->
            mListener!!.onClick(v)
            dismiss()
        })

        cancel.setOnClickListener(View.OnClickListener { v: View? ->
            if (mCancel != null) {
                mCancel!!.onClick(v)
            }
            dismiss()
        })

        dialog.setOnKeyListener { v: DialogInterface?, keyCode: Int, event: KeyEvent ->
            if ((keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP)) {
                dismiss()
                return@setOnKeyListener true
            } else {
                return@setOnKeyListener false
            }
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.setContentView(view)

        return dialog
    }

    companion object {
        var TAG: String = "WarningDialog"
        private var sInstance: WarningDialog? = null

        //Need To reset value before get instance
        fun newInstance(): WarningDialog {
            return sInstance?.apply { reset() } ?: WarningDialog().also { sInstance = it }
        }
    }
}
