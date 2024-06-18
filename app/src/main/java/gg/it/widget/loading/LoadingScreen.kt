package gg.it.widget.loading

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import gg.it.R
import gg.it.widget.pulse.PulseLayout

class LoadingScreen(context: Context) :
    Dialog(context, R.style.LoadingScreen) {
    private var pulseLayout: PulseLayout

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.view_loading)

        setCancelable(false)
        setCanceledOnTouchOutside(false)

        pulseLayout = findViewById(R.id.pulse_view)
    }

    override fun show() {
        pulseLayout.start()
        super.show()
    }

    override fun dismiss() {
        pulseLayout.stop()
        super.dismiss()
    }
}
