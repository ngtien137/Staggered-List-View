package com.tienuu.demostaggeredlistview.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class PreviewViewModel constructor(val stateHandle: SavedStateHandle) : ViewModel() {

    val liveImageData = stateHandle.getLiveData<String>("data_image")

    val imageHeight = stateHandle.get("image_height")?:0


}