package guepardoapps.lucahome.common.adapter

import guepardoapps.lucahome.common.enums.common.DownloadState
import guepardoapps.lucahome.common.enums.common.ServerAction

interface OnDownloadAdapter {
    fun onFinished(serverAction: ServerAction, state: DownloadState, message: String)
}