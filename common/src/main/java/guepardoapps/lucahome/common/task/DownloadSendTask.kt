package guepardoapps.lucahome.common.task

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import guepardoapps.lucahome.common.R
import guepardoapps.lucahome.common.adapter.OnDownloadAdapter
import guepardoapps.lucahome.common.crypto.Decrypter
import guepardoapps.lucahome.common.crypto.Encrypter
import guepardoapps.lucahome.common.enums.common.DownloadState
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.services.validation.ValidationService
import guepardoapps.lucahome.common.utils.Logger
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody

class DownloadSendTask : AsyncTask<String, Void, String>() {
    private val tag = DownloadSendTask::class.java.simpleName

    @SuppressLint("StaticFieldLeak")
    lateinit var context: Context
    private val okHttpClient = OkHttpClient()
    private val validationService = ValidationService()

    lateinit var serverAction: ServerAction
    lateinit var onDownloadAdapter: OnDownloadAdapter

    private lateinit var key: String

    override fun doInBackground(vararg actions: String?): String {
        for (action in actions) {
            try {

                val serverIp = this.context.getString(R.string.server_ip)
                val libActionPath = this.context.getString(R.string.raspberry_pi_lib_action)

                val requestUrlHandshake = "$serverIp$libActionPath"

                val handshakeRequest: Request = Request.Builder().url(requestUrlHandshake).build()
                val handshakeResponse: Response = okHttpClient.newCall(handshakeRequest).execute()
                val handshakeResponseBody: ResponseBody? = handshakeResponse.body()

                if (handshakeResponseBody != null) {
                    key = handshakeResponseBody.string()
                } else {
                    return ""
                }

                val encryptedAction = Encrypter().encrypt(key, action)
                if (!encryptedAction.first) {
                    return ""
                }

                val requestUrlAction = "$serverIp$libActionPath$encryptedAction"

                val request: Request = Request.Builder().url(requestUrlAction).build()
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

        val decryptedResult = Decrypter().decrypt(key, result)
        if (!decryptedResult.first) {
            onDownloadAdapter.onFinished(serverAction, DownloadState.DecryptionFailed, decryptedResult.second)
            return
        }

        val resultValidation = this.validationService.validateDownloadResponse(decryptedResult.second)
        if (!resultValidation.first) {
            onDownloadAdapter.onFinished(serverAction, DownloadState.ValidationFailed, resultValidation.second)
            return
        }

        onDownloadAdapter.onFinished(serverAction, DownloadState.Success, decryptedResult.second)
    }
}