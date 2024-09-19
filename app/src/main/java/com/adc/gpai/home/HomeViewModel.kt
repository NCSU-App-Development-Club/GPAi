package com.adc.gpai.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    // Mutable state for the HomeViewState (either Forecaster or Advisor)
    private val _homeState = MutableLiveData<HomeViewState>(HomeViewState.FORECASTER)
    val homeState: LiveData<HomeViewState> = _homeState

    // Function to change the HomeViewState (e.g., on toggle click)
    fun setHomeState(state: HomeViewState) {
        _homeState.value = state
    }

}
