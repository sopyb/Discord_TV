package ro.sopy.discordtv

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import ro.sopy.discordtv.auth.LoginUser

class LoginActivity : AppCompatActivity() {
    private lateinit var qrImageView: ImageView
    private lateinit var avatarImageView: ImageView
    private lateinit var usernameField: TextView
    private lateinit var loadingBar: ProgressBar
    private lateinit var loginModal: ConstraintLayout

    /**
     * Util functions
     */
    private fun changeVisibility(state: CurrentlyVisible) {
        this.loadingBar.visibility = View.GONE
        this.qrImageView.visibility = View.GONE
        this.loginModal.visibility = View.GONE
        when (state) {
            CurrentlyVisible.LOADING_QR -> this.loadingBar.visibility = View.VISIBLE
            CurrentlyVisible.SHOW_QR -> this.qrImageView.visibility = View.VISIBLE
            CurrentlyVisible.SHOW_USER -> this.loginModal.visibility = View.VISIBLE
        }
    }

    private fun qrUpdate(url: String?) {
        Log.d("LoginActivity", "qrUpdate started with $url")
        if (url != null) {
            val qr = QRGEncoder(
                url,
                null,
                QRGContents.Type.TEXT, 1
            )

            qr.colorWhite = ContextCompat.getColor(this, R.color.greyple)
            qr.colorBlack = ContextCompat.getColor(this, R.color.blurple)

            val qrBitmap = Bitmap.createScaledBitmap(
                qr.bitmap,
                300,
                300,
                false
            )

            qrImageView.setImageBitmap(qrBitmap)

            changeVisibility(CurrentlyVisible.SHOW_QR)
        } else {
            changeVisibility(CurrentlyVisible.LOADING_QR)
        }
    }

    private fun userUpdate(user: LoginUser?) {
        Log.d("LoginActivity", "userUpdate started")
        when {
            user == null -> return
            user.token.isNotEmpty() -> {
                Log.d("LoginActivity", "successfully authenticated")
                /**
                 * TODO successful login
                 */
            }
            user.token.isEmpty() -> {
                //user scan case
                Log.d("LoginActivity", "userUpdate started ")
                usernameField.text = getString(R.string.userTag, user.username, user.discriminator)

                changeVisibility(CurrentlyVisible.SHOW_USER)

                Picasso.get()
                    .load("https://cdn.discordapp.com/avatars/${user.userId}/${user.avatarHash}.png")
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .into(this.avatarImageView)
            }
        }
    }

    /**
     * Activity overwrite functions
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        qrImageView = findViewById(R.id.QrHolder)
        avatarImageView = findViewById(R.id.avatarPreview)
        usernameField = findViewById(R.id.userName)
        loadingBar = findViewById(R.id.QrLoading)
        loginModal = findViewById(R.id.loginModal)

        val viewModel: LoginViewModel by viewModels()

        viewModel.qrUrl.observe(this, { url -> qrUpdate(url) })
        viewModel.user.observe(this, { user ->
            run {
                userUpdate(user)
            }
        })
    }

    companion object {
        enum class CurrentlyVisible {
            LOADING_QR,
            SHOW_QR,
            SHOW_USER
        }
    }
}