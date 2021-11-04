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

    companion object {
        const val PAGE_SIZE = 20
    }

    val liveListImage = MutableLiveData(arrayListOf<AppImage>())

    val eventLoading by lazy {
        MutableLiveData(Event())
    }

    fun loadListImage() {
        eventLoading.value = Event(true)
        viewModelScope.launch(Dispatchers.IO) {
            val list = imageRepository.loadImages(PAGE_SIZE)
            withContext(Dispatchers.Main) {
                liveListImage.value = list
                eventLoading.value = Event(false)
            }
        }
    }

    fun loadMoreListImage() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = imageRepository.loadImages(PAGE_SIZE, liveListImage.value?.size ?: 0)
            withContext(Dispatchers.Main) {
                liveListImage.value = list
                eventLoading.value = Event(false)
            }
        }
    }
}