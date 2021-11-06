package com.tienuu.demostaggeredlistview.adapter

import androidx.appcompat.widget.AppCompatImageView
import com.tienuu.demostaggeredlistview.data.AppImage

interface ImageListener {
    fun onImageClick(imageView: AppCompatImageView, image: AppImage, itemPosition: Int)
}