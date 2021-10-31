package com.tienuu.demostaggeredlistview.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.base.baselibrary.viewmodel.Auto
import com.base.baselibrary.viewmodel.Event
import com.tienuu.demostaggeredlistview.data.AppImage
import com.tienuu.demostaggeredlistview.data.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel @Auto private constructor(private val imageRepository: ImageRepository) :
    ViewModel() {

    val liveListImage = MutableLiveData(arrayListOf<AppImage>())

    val eventLoading by lazy {
        MutableLiveData(Event())
    }

    fun loadListImage(pageSize:Int) {
        eventLoading.value = Event(true)
        viewModelScope.launch(Dispatchers.IO) {
            val list = imageRepository.loadImages(pageSize)
            withContext(Dispatchers.Main) {
                liveListImage.value = list
                eventLoading.value = Event(false)
            }
        }
    }
}