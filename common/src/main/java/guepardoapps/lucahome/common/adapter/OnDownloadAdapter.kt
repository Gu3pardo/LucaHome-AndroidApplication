package guepardoapps.lucahome.common.adapter

import guepardoapps.lucahome.common.enums.DownloadState
import guepardoapps.lucahome.common.enums.DownloadType

interface OnDownloadAdapter {
    fun onFinished(type: DownloadType, state: DownloadState, message: String)
}