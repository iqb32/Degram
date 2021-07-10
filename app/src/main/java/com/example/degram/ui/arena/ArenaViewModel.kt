package com.example.degram.ui.arena

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.degram.data.DegramRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArenaViewModel @Inject constructor(
        private val repository: DegramRepository
) : ViewModel() {

    private val _showAddArenaLayout = MutableLiveData<Boolean>()
    val showAddArenaLayout: LiveData<Boolean>
        get() = _showAddArenaLayout

    private val _authenticateUser = MutableLiveData<Boolean>()
    val authenticateUser: LiveData<Boolean>
        get() = _authenticateUser

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _dismissSheet = MutableLiveData<Boolean>()
    val dismissSheet : LiveData<Boolean>
        get() = _dismissSheet

    private val _askForArenaCode = MutableLiveData<Boolean>()
    val askForArenaCode : LiveData<Boolean>
        get() = _askForArenaCode

    val code = MutableLiveData<String>()

    private val _toast = MutableLiveData<String>()
    val toast : LiveData<String>
        get() = _toast

    fun newArena() {
        viewModelScope.launch {
            _isLoading.value = true
            val isArenaCreated = repository.createNewArena()
            _isLoading.value = false
            _dismissSheet.value = true
           if (!isArenaCreated) _toast.value = "Failed to create arena. Please try again later."
        }
    }

    fun joinExistingArena() {
        when {
            code.value == null -> {
                _toast.value = "Please enter a arena code"
            }
            code.value!!.length != 6 -> {
                _toast.value  = "Code should be a 6 digit number"
            }
            else -> {
                viewModelScope.launch {
                    _askForArenaCode.value = false
                    _isLoading.value = true
                    val isArenaJoined = repository.joinExistingArena(code.value!!)
                    _isLoading.value = false
                    _dismissSheet.value = true
                    if (!isArenaJoined) _toast.value = "Failed to join arena. Please try again later."
                }
            }
        }
    }

    fun addArena() {
        //Show options only is the user is authenticated
        if (FirebaseAuth.getInstance().currentUser == null) {
            _authenticateUser.value = true
        } else {
            _showAddArenaLayout.value = true
        }
    }

    fun askForCode() { _askForArenaCode.value = true }

    companion object {
        private const val TAG = "ArenaViewModel"
    }

}