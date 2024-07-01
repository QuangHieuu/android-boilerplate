package boilerplate.widget.loading

import android.app.Dialog
import android.content.Context
import android.view.Window
import boilerplate.R
import boilerplate.widget.pulse.PulseLayout

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
