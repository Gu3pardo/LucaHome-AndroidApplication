package guepardoapps.lucahome.common.task

import android.os.AsyncTask
import guepardoapps.lucahome.common.adapter.OnDownloadAdapter
import guepardoapps.lucahome.common.enums.DownloadState
import guepardoapps.lucahome.common.enums.ServerAction
import guepardoapps.lucahome.common.utils.Logger
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody

class DownloadSendTask : AsyncTask<String, Void, String>() {
    private val tag = DownloadSendTask::class.java.simpleName

    lateinit var serverAction: ServerAction
    lateinit var onDownloadAdapter: OnDownloadAdapter
    private val okHttpClient = OkHttpClient()

    override fun doInBackground(vararg requestUrls: String?): String {
        for (requestUrl in requestUrls) {
            try {
                val request: Request = Request.Builder().url(requestUrl!!).build()
                val response: Response = okHttpClient.newCall(request).execute()
                val responseBody: ResponseBody? = response.body()

                if (responseBody != null) {
                    return responseBody.string()
                }

                return ""
            } catch (exception: Exception) {
                Logger.instance.error(tag, exception)
            }
        }

        return ""
    }

    override fun onPostExecute(result: String?) {
        if (result.isNullOrEmpty()) {
            onDownloadAdapter.onFinished(serverAction, DownloadState.Canceled, "")
            return
        }
        onDownloadAdapter.onFinished(serverAction, DownloadState.Success, result!!)
    }
}