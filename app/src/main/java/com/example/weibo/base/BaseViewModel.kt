package com.example.weibo.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {

    protected fun launchSafe(block: suspend () -> Unit) {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            
            throwable.printStackTrace()
        }
        
        viewModelScope.launch(exceptionHandler) {
            block()
        }
    }
}


