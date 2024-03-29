package com.example.katify.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.katify.R
import com.example.katify.data.model.User
import com.example.katify.databinding.ActivityLoginBinding
import com.example.katify.viewModel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider


/**
 *
 * Class that inflates the [R.layout.activity_login] layout
 *
 * This activity only appears at the first opening, and it's where the user can log to the app
 *
 * Inherits [AppCompatActivity] and implements [View.OnClickListener]
 *
 */
class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var loginVM: LoginViewModel

    lateinit var mGoogleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // Defines google sign in
        val gso: GoogleSignInOptions = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleLoginBtn.setOnClickListener(this)
        binding.anonymLoginBtn.setOnClickListener(this)

        loginVM = ViewModelProvider(this).get(LoginViewModel::class.java)
    }


    override fun onClick(v: View?) {
        if (v!!.id == R.id.google_login_btn) {
            openSomeActivityForResult()
        } else if (v!!.id == R.id.anonym_login_btn) {
            createAnonymousUser()
        }
    }

    /**
     * Gets google sign in intent, from  which is taken the account logged
     * Finally, a new activity is started passing the user
     */
    private fun openSomeActivityForResult() {
        val signInIntent = mGoogleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }


    /**
     * Get account result from [completedTask]
     */
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            if (account != null) {
                getGoogleAuthCredential(account)
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }


    /**
     * Get [googleSignInAccount] credentials
     */
    private fun getGoogleAuthCredential(googleSignInAccount: GoogleSignInAccount) {
        val googleTokenId = googleSignInAccount.idToken
        val googleAuthCredential = GoogleAuthProvider.getCredential(googleTokenId, null)
        signInWithGoogleAuthCredential(googleAuthCredential)
    }

    /**
     * If [googleAuthCredential] never signed, calls [createNewUser]
     * else calls [goToKanbansActivity]
     */
    private fun signInWithGoogleAuthCredential(googleAuthCredential: AuthCredential) {
        loginVM.signInWithGoogle(googleAuthCredential)
        loginVM.authenticatedUserLiveData.observe(this) { authenticatedUser ->
            if (authenticatedUser.isNew) {
                createNewUser(authenticatedUser)
            } else {
                goToKanbansActivity(authenticatedUser)
            }
        }
    }

    /**
     * Create a new [authenticatedUser] in the firebase
     */
    private fun createNewUser(authenticatedUser: User) {
        loginVM.createUser(authenticatedUser)
        loginVM.createdUserLiveData.observe(this) { user ->
            if (user.isCreated) {
                goToKanbansActivity(user)
            }
        }
    }

    /**
     * Create a new anonymous [User]
     */
    private fun createAnonymousUser() {
        loginVM.createAnonymUser()
        loginVM.createdUserLiveData.observe(this) { user ->
            if (user.isCreated) {
                goToKanbansActivity(user)
            }
        }
    }

    /**
     * Starts [KanbansPageActivity] activity, passing [user] to it
     */
    private fun goToKanbansActivity(user: User) {
        val intent = Intent(this, KanbansPageActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "GoogleActivity"
    }

}
