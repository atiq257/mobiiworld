package com.mobiiworld.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobiiworld.repository.SquareRepository
/*
To define how our view model should be created
 */
class SquareViewModelProviderFactory(
    val app: Application,
    val squareRepository: SquareRepository
    ) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SquareViewModel(app, squareRepository) as T
    }
}