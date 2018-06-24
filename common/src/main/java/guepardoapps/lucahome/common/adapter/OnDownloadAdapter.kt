package guepardoapps.lucahome.common.adapter

import guepardoapps.lucahome.common.enums.DownloadState
import guepardoapps.lucahome.common.enums.ServerAction

interface OnDownloadAdapter {
    fun onFinished(serverAction: ServerAction, state: DownloadState, message: String)
}