package boilerplate.widget.chatBox

import android.text.Editable
import android.text.TextWatcher

class BoxTextWatcher(private val listener: OnListener) : TextWatcher {
    interface OnListener {
        fun changed(s: CharSequence)
        fun after(s: Editable)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        listener.changed(s)
    }

    override fun afterTextChanged(s: Editable) {
        listener.after(s)
    }

    companion object {
        fun watcher(
            change: (s: CharSequence) -> Unit,
            after: (s: Editable) -> Unit
        ): BoxTextWatcher {
            return BoxTextWatcher(object : OnListener {
                override fun changed(s: CharSequence) {
                    change(s)
                }

                override fun after(s: Editable) {
                    after(s)
                }
            })
        }
    }
}