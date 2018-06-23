package guepardoapps.lucahome.common.adapter

import android.content.Context
import guepardoapps.lucahome.common.controller.NetworkController
import guepardoapps.lucahome.common.enums.DownloadState
import guepardoapps.lucahome.common.enums.DownloadType
import guepardoapps.lucahome.common.task.DownloadSendTask
import guepardoapps.lucahome.common.utils.Logger

class DownloadAdapter(context: Context) {
    private val tag = DownloadAdapter::class.java.simpleName

    private var networkController: NetworkController = NetworkController(context)

    fun send(requestUrl: String, downloadType: DownloadType, needsHomeNetwork: Boolean, onDownloadAdapter: OnDownloadAdapter) {
        if (this.canSend(requestUrl, downloadType, needsHomeNetwork, onDownloadAdapter)) {
            val downloadSendTask = DownloadSendTask()
            downloadSendTask.downloadType = downloadType
            downloadSendTask.onDownloadAdapter = onDownloadAdapter
            downloadSendTask.execute(requestUrl)
        }
    }

    private fun canSend(requestUrl: String, downloadType: DownloadType, needsHomeNetwork: Boolean, onDownloadAdapter: OnDownloadAdapter): Boolean {
        if (!networkController.IsNetworkAvailable()) {
            Logger.instance.warning(tag, DownloadState.NoNetwork)
            onDownloadAdapter.onFinished(downloadType, DownloadState.NoNetwork, "")
            return false
        }

        // TODO add homeSSID
        if (needsHomeNetwork && !networkController.IsHomeNetwork("")) {
            Logger.instance.warning(tag, DownloadState.NoHomeNetwork)
            onDownloadAdapter.onFinished(downloadType, DownloadState.NoHomeNetwork, "")
            return false
        }

        if (requestUrl.length < 15) {
            Logger.instance.warning(tag, DownloadState.InvalidUrl)
            onDownloadAdapter.onFinished(downloadType, DownloadState.InvalidUrl, "")
            return false
        }

        return true
    }
}