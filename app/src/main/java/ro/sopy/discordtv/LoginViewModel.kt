package ro.sopy.discordtv

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ro.sopy.discordtv.auth.LoginUser
import ro.sopy.discordtv.auth.QrAuth

class LoginViewModel: ViewModel(), QrAuth.QrAuthListener {
    private var qrServerJob: Job = GlobalScope.launch {
        QrAuth(this@LoginViewModel)
    }
    var user: MutableLiveData<LoginUser?> = MutableLiveData<LoginUser?>(null)
        private set
    var qrUrl: MutableLiveData<String?> = MutableLiveData<String?>(null)
        private set


    override fun onCleared() {
        qrServerJob.cancel(null)
        super.onCleared()
    }

    /**
     * Qr auth server listeners
     */
    override fun onFingerprintReady(fingerprint: String) {
        qrUrl.postValue("https://discordapp.com/ra/$fingerprint")
    }

    override fun onUserScan(user: LoginUser) {
        this.user.postValue(user)
    }

    override fun onUserCancel() {
        user.postValue(null)
        qrUrl.postValue(null)
        qrServerJob.cancel(null)
        qrServerJob.start()
    }

    override fun onFinishLogin(user: LoginUser) {
        this.user.postValue(user)
    }

    override fun onError(e: Exception) {

    }
}