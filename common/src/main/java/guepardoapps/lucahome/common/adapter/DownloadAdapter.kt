package guepardoapps.lucahome.common.adapter

import android.content.Context
import guepardoapps.lucahome.common.R
import guepardoapps.lucahome.common.controller.NetworkController
import guepardoapps.lucahome.common.enums.common.DownloadState
import guepardoapps.lucahome.common.enums.common.NetworkType
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.extensions.enums.getNeededNetwork
import guepardoapps.lucahome.common.services.validation.ValidationService
import guepardoapps.lucahome.common.services.user.UserService
import guepardoapps.lucahome.common.task.DownloadSendTask
import guepardoapps.lucahome.common.utils.Logger

class DownloadAdapter(private val context: Context) {
    private val tag = DownloadAdapter::class.java.simpleName

    private var networkController: NetworkController = NetworkController(context)
    private var validationService: ValidationService = ValidationService()

    fun send(action: String, serverAction: ServerAction, onDownloadAdapter: OnDownloadAdapter) {
        if (this.canSend(action, serverAction, onDownloadAdapter)) {

            val user = UserService.instance.get()

            val downloadSendTask = DownloadSendTask()
            downloadSendTask.context = context
            downloadSendTask.serverAction = serverAction
            downloadSendTask.onDownloadAdapter = onDownloadAdapter
            downloadSendTask.execute("${user?.name}:${user?.password}:$action")
        }
    }

    private fun canSend(action: String, serverAction: ServerAction, onDownloadAdapter: OnDownloadAdapter): Boolean {
        val validationResult = validationService.mayPerform(serverAction)
        if (!validationResult.first) {
            onDownloadAdapter.onFinished(serverAction, DownloadState.ValidationFailed, validationResult.second)
            return false
        }

        if (serverAction.getNeededNetwork().networkType != NetworkType.No && !networkController.isInternetConnected().second) {
            Logger.instance.warning(tag, DownloadState.NoNetwork)
            onDownloadAdapter.onFinished(serverAction, DownloadState.NoNetwork, "")
            return false
        }

        // TODO get homeSsid dynamically
        val homeSsid = this.context.getString(R.string.home_ssid)
        if (serverAction.getNeededNetwork().networkType == NetworkType.HomeWifi && !networkController.isHomeWifiConnected(homeSsid)) {
            Logger.instance.warning(tag, DownloadState.NoHomeNetwork)
            onDownloadAdapter.onFinished(serverAction, DownloadState.NoHomeNetwork, "")
            return false
        }

        if (action.isEmpty()) {
            Logger.instance.warning(tag, DownloadState.InvalidUrl)
            onDownloadAdapter.onFinished(serverAction, DownloadState.InvalidUrl, "")
            return false
        }

        return true
    }
}