package guepardoapps.lucahome.bixby.tasks

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.os.AsyncTask
import guepardoapps.lucahome.bixby.services.BixbyService
import guepardoapps.lucahome.common.utils.Logger
import java.lang.ref.WeakReference

class DelayedBackButtonTask() : AsyncTask<Void, Void, Void>() {
    private val tag = DelayedBackButtonTask::class.java.simpleName
    private lateinit var bixbyServiceWeakReference: WeakReference<BixbyService>

    constructor(context: BixbyService) : this() {
        bixbyServiceWeakReference = WeakReference(context)
    }

    override fun doInBackground(vararg void: Void?): Void? {
        try {
            Thread.sleep(50)
        } catch (exception: InterruptedException) {
            Logger.instance.error(tag, "interrupted")
        }

        bixbyServiceWeakReference.get()?.performGlobalAction(GLOBAL_ACTION_BACK)

        return null
    }
}