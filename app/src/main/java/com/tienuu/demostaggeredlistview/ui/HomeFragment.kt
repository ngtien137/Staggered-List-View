package com.tienuu.demostaggeredlistview.ui

import android.os.Bundle
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.base.baselibrary.fragment.BaseFragment
import com.base.baselibrary.utils.observer
import com.base.baselibrary.viewmodel.autoViewModels
import com.base.baselibrary.views.ext.getWidthScreen
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.tienuu.demostaggeredlistview.MainActivity
import com.tienuu.demostaggeredlistview.R
import com.tienuu.demostaggeredlistview.adapter.ImageListener
import com.tienuu.demostaggeredlistview.data.AppImage
import com.tienuu.demostaggeredlistview.databinding.FragmentHomeBinding
import com.tienuu.demostaggeredlistview.databinding.ItemImageBinding
import com.tienuu.demostaggeredlistview.viewmodels.HomeViewModel
import lib.tienuu.staggeredlistview.StaggeredListView

class HomeFragment : BaseFragment<FragmentHomeBinding, MainActivity>(), ImageListener {

    private val viewModel by autoViewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootActivity.grantPermissionStorage {
            viewModel.loadListImage()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    override fun initBinding() {
        binding.viewModel = viewModel
    }

    override fun initView() {
        val adapter =
            (object : StaggeredListView.StaggeredAdapter<AppImage, ItemImageBinding>(
                requireContext(),
                R.layout.item_image
            ) {
                override fun onConfigureWithBinding(binding: ItemImageBinding, itemPosition: Int) {
                    ViewCompat.setTransitionName(
                        binding.imgItem,
                        "img_${data[itemPosition].getPathForVisible()}"
                    )
                }
            }).also { adapter ->
                adapter.listener = this
            }
        binding.staggeredListView.adapter = adapter
        observer(viewModel.liveListImage) {
            binding.staggeredListView.post {
                adapter.data = it ?: arrayListOf()
            }
        }

        binding.refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                viewModel.loadListImage {
                    binding.refreshLayout.finishRefresh()
                }
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                viewModel.loadMoreListImage {
                    adapter.validateWithList()
                    binding.staggeredListView.validateWithAdapter()
                    binding.refreshLayout.finishLoadMore()
                }
            }

        })
    }

    override fun onImageClick(imageView: AppCompatImageView, image: AppImage, itemPosition: Int) {
        val extras = FragmentNavigatorExtras(
            imageView to "img_${image.getPathForVisible()}"
        )
        val action =
            HomeFragmentDirections.actionHomeFragmentToPreviewImageFragment(
                image.getPathForVisible(),
                imageHeight = (getWidthScreen() / image.getRatio()).toInt()
            )
        findNavController().navigate(action, extras)
    }

}