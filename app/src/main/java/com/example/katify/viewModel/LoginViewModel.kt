package com.example.katify.viewModel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.AuthCredential
import com.example.katify.data.LoginRepository
import com.example.katify.data.model.Kanban
import com.example.katify.data.model.User

/**
 * This class interacts with the [User] entity and firebase
 *
 * Inherits [ViewModel]
 *
 * @property application
 */
class LoginViewModel : ViewModel() {
    private var authRepository: LoginRepository = LoginRepository()
    lateinit var authenticatedUserLiveData : LiveData<User>
    lateinit var createdUserLiveData : LiveData<User>

    /**
     *  Authenticates user with [googleAuthCredential] in firebase
    */
    fun signInWithGoogle(googleAuthCredential : AuthCredential) {
        authenticatedUserLiveData = authRepository.firebaseSignInWithGoogle(googleAuthCredential)
    }

    /**
     * Creates [authenticatedUser] in firebase
     */
    fun createUser(authenticatedUser : User) {
        createdUserLiveData = authRepository.createUserInFirestoreIfNotExists(authenticatedUser)
    }

    /**
     * Creates an anonymous [User]
     */
    fun createAnonymUser()  {
        val user = MutableLiveData<User>()
        user.value = User("1", "Anonymous",
            "https://cdn-icons-png.flaticon.com/512/235/235349.png", "" , isCreated = true)
        createdUserLiveData = user
    }
}
