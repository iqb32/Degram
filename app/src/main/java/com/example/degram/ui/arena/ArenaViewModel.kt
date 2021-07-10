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


    fun newArena() {
        viewModelScope.launch {
            _isLoading.value = true
            if (repository.createNewArena()) {
                _isLoading.value = false
                _dismissSheet.value = true
            } else {
                _isLoading.value = false
                _dismissSheet.value = true
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

    companion object {
        private const val TAG = "ArenaViewModel"
    }

}