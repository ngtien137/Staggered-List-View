package com.tienuu.demostaggeredlistview.ui

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import com.base.baselibrary.fragment.BaseNavigationFragment
import com.tienuu.demostaggeredlistview.MainActivity
import com.tienuu.demostaggeredlistview.R
import com.tienuu.demostaggeredlistview.databinding.FragmentPreviewImageBinding
import com.tienuu.demostaggeredlistview.viewmodels.PreviewViewModel

class PreviewImageFragment : BaseNavigationFragment<FragmentPreviewImageBinding, MainActivity>() {

    private val viewModel by viewModels<PreviewViewModel>()

    override fun getLayoutId(): Int {
        return R.layout.fragment_preview_image
    }

    override fun initBinding() {
        binding.viewModel = viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imgMain.layoutParams.height = viewModel.imageHeight
        val transitionName = "img_${viewModel.liveImageData.value}"
        ViewCompat.setTransitionName(binding.imgMain, transitionName)
    }

    override fun initView() {

    }


}