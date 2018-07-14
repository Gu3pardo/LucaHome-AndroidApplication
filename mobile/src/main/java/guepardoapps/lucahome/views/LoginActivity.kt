package guepardoapps.lucahome.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import guepardoapps.lucahome.R
import guepardoapps.lucahome.common.models.user.User
import guepardoapps.lucahome.common.services.user.UserService
import guepardoapps.lucahome.common.utils.Logger
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*

/**
 * A login screen that offers login via username/password.
 */
class LoginActivity : AppCompatActivity() {
    private val tag = LoginActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        username_sign_in_button.setOnClickListener { attemptLogin() }

        UserService.instance.responsePublishSubject
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { response ->
                            // TODO
                        },
                        { responseError ->
                            Logger.instance.error(tag, responseError)
                            username_sign_in_button.isEnabled = true
                            username.error = getString(R.string.error_validating_user)
                            showProgress(false)
                        })
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (!username_sign_in_button.isEnabled) {
            return
        }

        username_sign_in_button.isEnabled = false

        // Reset errors.
        username.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val userNameStr = username.text.toString()
        val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(userNameStr)) {
            username.error = getString(R.string.error_field_required)
            focusView = username
            cancel = true
        } else if (!isUserNameValid(userNameStr)) {
            username.error = getString(R.string.error_invalid_username)
            focusView = username
            cancel = true
        }

        if (cancel) {
            username_sign_in_button.isEnabled = true
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)

            val user = User()
            user.name = userNameStr
            user.password = passwordStr

            UserService.instance.validate(user)
        }
    }

    private fun isUserNameValid(userName: String): Boolean {
        //TODO: Replace this with your own logic
        return userName.length > 4
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        // we have the ViewPropertyAnimator APIs, which allow for very easy
        // animations. If available, use these APIs to fade-in the progress
        // spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        login_form.visibility = if (show) View.GONE else View.VISIBLE
        login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        login_progress.visibility = if (show) View.VISIBLE else View.GONE
        login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }
}
