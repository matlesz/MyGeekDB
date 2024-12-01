package ie.matlesz.mygeekdb.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashViewModel : ViewModel() {

  private val _isInitialized = MutableLiveData(false)
  val isInitialized: LiveData<Boolean> = _isInitialized

  fun initializeApp() {
    viewModelScope.launch {
      // Simulate initialization tasks (e.g., API calls, database checks)
      delay(2000) // Simulate a delay
      _isInitialized.value = true
      Log.d("SplashViewModel", "Initialization complete: $isInitialized")
    }
  }
}