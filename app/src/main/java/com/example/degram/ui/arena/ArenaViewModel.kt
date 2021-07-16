package com.example.degram.ui.arena

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.degram.data.Arena
import com.example.degram.data.DegramRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ArenaViewModel @Inject constructor(
        @ApplicationContext private val context: Context,
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

    private val _showSignIn = MutableLiveData<Boolean>()
    val showSignIn : LiveData<Boolean>
        get() = _showSignIn

    private val _arenas = MutableLiveData<List<Arena>>()
    val arenas : LiveData<List<Arena>>
        get() = _arenas

    private val _arenaIndex = MutableLiveData<Int>()
    val arenaIndex : LiveData<Int>
        get() = _arenaIndex

    private val _isArenaLoading = MutableLiveData<Boolean>()
    val isArenaLoading : LiveData<Boolean>
       get() = _isArenaLoading

    private val _noArenas = MutableLiveData<Boolean>()
    val noArenas : LiveData<Boolean>
        get() = _noArenas

    init {
        getArenas()
        _arenaIndex.value = 0
    }

    @ExperimentalCoroutinesApi
    fun getArenas() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            _showSignIn.value = true
        } else {
            _isArenaLoading.value = true
            viewModelScope.launch {
                repository.getArenas().collect {arenas ->
                    _isArenaLoading.value =  false
                    if (!arenas.isNullOrEmpty()) {
                        _arenaIndex.value = 0
                        _noArenas.value = false
                        _arenas.value = arenas
                    } else _noArenas.value = true
                }
            }
        }
    }

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
                    if (!isArenaJoined) _toast.value = "Failed to join arena. Please check your code"
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

    fun nextArena() {if (_arenaIndex.value!! + 1 < _arenas.value!!.size) _arenaIndex.value = _arenaIndex.value!!.plus(1)}

    fun prevArena() {if (_arenaIndex.value!!.minus(1) >= 0) _arenaIndex.value = _arenaIndex.value!!.minus(1)}

    fun isHost(arena : Arena?) : Boolean = FirebaseAuth.getInstance().currentUser?.uid == arena?.hostUid

    fun startAuthentication() { _authenticateUser.value = true}

    fun leaveArena(arena : Arena) {
        viewModelScope.launch {
            repository.leaveArena(arena.code)
        }
    }

    fun deleteArena(arena : Arena?) {
        viewModelScope.launch {
            repository.deleteArena(arena!!.code)
        }
    }

    fun authenticationCompleted() {_showSignIn.value = false}

    companion object {
        private const val TAG = "ArenaViewModel"
    }

}