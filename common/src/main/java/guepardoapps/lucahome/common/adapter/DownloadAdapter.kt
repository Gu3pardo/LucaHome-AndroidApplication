package guepardoapps.lucahome.common.adapter

import android.content.Context
import guepardoapps.lucahome.common.R
import guepardoapps.lucahome.common.controller.NetworkController
import guepardoapps.lucahome.common.enums.DownloadState
import guepardoapps.lucahome.common.enums.NetworkType
import guepardoapps.lucahome.common.enums.ServerAction
import guepardoapps.lucahome.common.extensions.getNeededNetwork
import guepardoapps.lucahome.common.task.DownloadSendTask
import guepardoapps.lucahome.common.utils.Logger

class DownloadAdapter(private val context: Context) {
    private val tag = DownloadAdapter::class.java.simpleName

    private var networkController: NetworkController = NetworkController(context)

    fun send(actionPath: String, serverAction: ServerAction, onDownloadAdapter: OnDownloadAdapter) {
        if (this.canSend(actionPath, serverAction, onDownloadAdapter)) {
            val serverIp = this.context.getString(R.string.server_ip)
            val libActionPath = this.context.getString(R.string.raspberry_pi_lib_action)
            val requestUrl = "$serverIp$libActionPath$actionPath"

            val downloadSendTask = DownloadSendTask()
            downloadSendTask.serverAction = serverAction
            downloadSendTask.onDownloadAdapter = onDownloadAdapter
            downloadSendTask.execute(requestUrl)
        }
    }

    private fun canSend(actionPath: String, serverAction: ServerAction, onDownloadAdapter: OnDownloadAdapter): Boolean {
        if (serverAction.getNeededNetwork().networkType != NetworkType.No && !networkController.IsNetworkAvailable()) {
            Logger.instance.warning(tag, DownloadState.NoNetwork)
            onDownloadAdapter.onFinished(serverAction, DownloadState.NoNetwork, "")
            return false
        }

        // TODO get homeSsid dynamically
        val homeSsid = this.context.getString(R.string.home_ssid)
        if (serverAction.getNeededNetwork().networkType == NetworkType.HomeWifi && !networkController.IsHomeNetwork(homeSsid)) {
            Logger.instance.warning(tag, DownloadState.NoHomeNetwork)
            onDownloadAdapter.onFinished(serverAction, DownloadState.NoHomeNetwork, "")
            return false
        }

        if (actionPath.isEmpty()) {
            Logger.instance.warning(tag, DownloadState.InvalidUrl)
            onDownloadAdapter.onFinished(serverAction, DownloadState.InvalidUrl, "")
            return false
        }

        return true
    }
}