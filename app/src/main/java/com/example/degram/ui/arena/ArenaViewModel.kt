package com.example.degram.ui.arena

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArenaViewModel @Inject constructor() : ViewModel() {

    private val _authenticateUser = MutableLiveData<Boolean>()
    val authenticateUser : LiveData<Boolean>
       get() = _authenticateUser

    fun newArena() {
        //Check if the user is authenticated
        if (FirebaseAuth.getInstance().currentUser == null) {
            _authenticateUser.value = true
        }
    }

}