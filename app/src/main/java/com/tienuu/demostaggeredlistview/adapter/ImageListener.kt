package com.tienuu.demostaggeredlistview.adapter

import com.tienuu.demostaggeredlistview.data.AppImage

interface ImageListener {
    fun onImageClick(image: AppImage, itemPosition: Int)
}