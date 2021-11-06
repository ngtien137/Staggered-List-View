package lib.tienuu.staggeredlistview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception

class StaggeredListView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ScrollView(context, attrs) {

    companion object {
        const val TAG = "StaggeredListView"
        const val SHOW_LOG = true
    }

    private val scrollLayout =
        LayoutInflater.from(context).inflate(R.layout.staggered_layout, this, true)

    private val parentWrap =
        scrollLayout.findViewById<RelativeLayout>(R.id.parentWrap)

    private val itemLayoutParent = scrollLayout.findViewById<FrameLayout>(R.id.layoutParent)


    var adapter: StaggeredAdapter<out StaggeredData, out ViewDataBinding>? = null
        set(value) {
            field = value
            value?.staggeredListView = this
            value?.invalidate()
        }

    var countOnLayout = 0
    private var currentSectionScroll = -1

    private var sectionOffset = 1

    private val heightScreen by lazy {
        context.resources.displayMetrics.heightPixels
    }

    fun validateWithAdapter() {
        if (width == 0)
            return
        adapter?.data?.firstOrNull()?.let command@{
            //Wrap relative layout
            parentWrap.layoutParams =
                parentWrap.layoutParams.also { it.height = adapter!!.maxHeight }

            viewTreeObserver.addOnScrollChangedListener {
                val scrollY = scrollLayout.scrollY
                checkVisibleWithScrollPosition(scrollY)
            }

            checkVisibleWithScrollPosition(scrollLayout.scrollY)
            itemLayoutParent.layoutParams.height = adapter!!.maxHeight
        }
    }

    fun invalidateWithCurrentScroll() {
        checkVisibleWithScrollPosition(scrollLayout.scrollY, true)
    }

    private fun checkVisibleWithScrollPosition(
        scrollY: Int, forceScroll: Boolean = false
    ) {
        val indexSection = scrollY / heightScreen
        loge("ParentSize: ${itemLayoutParent.childCount}, currentSection: $currentSectionScroll")
        if (indexSection != currentSectionScroll || forceScroll) {
            currentSectionScroll = indexSection
            adapter?.let { adapter ->
                val startIndexVisible = if (indexSection - sectionOffset >= 0)
                    indexSection - sectionOffset else 0
                for (index in startIndexVisible..(indexSection + sectionOffset)) {
                    adapter.listSectionIndex.getOrNull(index)?.forEach { mapIndex ->
                        loge("AddIndex: $mapIndex")
                        addBindingItemToView(mapIndex, itemLayoutParent)
                    }
                }

                adapter.listSectionIndex.getOrNull(indexSection - sectionOffset - 1)
                    ?.forEach { mapIndex ->
                        loge("RemoveIndex: $mapIndex")
                        adapter.mapBinding[mapIndex]?.let { itemBinding ->
                            setBindingGone(itemBinding, itemLayoutParent)
                        }
                    }
                adapter.listSectionIndex.getOrNull(indexSection + sectionOffset + 1)
                    ?.forEach { mapIndex ->
                        loge("RemoveIndex: $mapIndex")
                        adapter.mapBinding[mapIndex]?.let { itemBinding ->
                            setBindingGone(itemBinding, itemLayoutParent)
                        }
                    }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        countOnLayout++
        loge("Count Measure: $countOnLayout")

    }

    private fun addBindingItemToView(indexInData: Int, itemLayoutParent: FrameLayout) {
        adapter!!.mapRowData[adapter!!.getItem(indexInData)]?.let { rowData ->
            val positionX = rowData.columnIndex * (width / adapter!!.span)
            val positionY = rowData.offsetY
            val itemBinding = adapter!!.getViewBinding(indexInData)
            itemBinding.root.layoutParams =
                LayoutParams(rowData.width, rowData.height)
            itemBinding.root.x = positionX.toFloat()
            itemBinding.root.y = positionY
            if (itemBinding.root.parent != null) {
                (itemBinding.root.parent as ViewGroup?)?.removeView(itemBinding.root)
            }
            itemLayoutParent.addView(itemBinding.root)
        }
    }

    private fun <ViewBinding : ViewDataBinding> setBindingGone(
        itemBinding: ViewBinding, itemLayoutParent: FrameLayout
    ) {
        itemLayoutParent.removeView(itemBinding.root)
    }

    fun loge(message: String) {
        if (SHOW_LOG) {
            Log.e(TAG, message)
        }
    }

    open class StaggeredAdapter<Data : StaggeredData, ViewBinding : ViewDataBinding>(
        private val context: Context,
        @LayoutRes private val layoutResource: Int,
        private val lifecycleOwner: LifecycleOwner? = null
    ) {

        internal var staggeredListView: StaggeredListView? = null

        /**
         * Current version i'm only calculate for span = 2, i'm not sure if it's working correctly with other values
         */
        var span = 2
            private set

        private val layoutInflater by lazy {
            LayoutInflater.from(context)
        }

        var data = ArrayList<Data>()
            set(value) {
                field = value
                invalidate()
            }

        var listener: Any? = null

        val heightScreen by lazy {
            context.resources.displayMetrics.heightPixels
        }

        private val listColumnsData = ArrayList<DataColumn<Data>>()

        val mapBinding = HashMap<Int, ViewBinding>()

        val mapRowData = HashMap<Data, DataRow<Data>>()

        var maxHeight = 0
            private set

        val listSectionIndex = ArrayList<ArrayList<Int>>()

        private var lastValidateSize = 0

        private fun createItemBinding(): ViewBinding {
            return DataBindingUtil.inflate(layoutInflater, layoutResource, null, false)
        }

        fun getViewBinding(itemPosition: Int): ViewBinding {
            if (mapBinding[itemPosition] == null) {
                mapBinding[itemPosition] = createItemBinding()
            }
            val binding = mapBinding[itemPosition]!!
            binding.setVariable(BR.item, data[itemPosition])
            binding.setVariable(BR.listener, listener)
            binding.setVariable(BR.itemPosition, itemPosition)
            onConfigureWithBinding(binding, itemPosition)
            if (lifecycleOwner != null) {
                binding.lifecycleOwner = lifecycleOwner
            } else {
                if (context is LifecycleOwner) {
                    binding.lifecycleOwner = context
                }
            }
            binding.executePendingBindings()
            return binding
        }

        open fun onConfigureWithBinding(binding: ViewBinding, itemPosition: Int) {
        }

        fun getItem(position: Int): Data? {
            return data.getOrNull(position)
        }

        fun getCount() = data.size

        fun invalidate(offsetIndex: Int = -1) {
            if (offsetIndex == -1)
                clearData()
            val widthView = staggeredListView?.width ?: 0
            if (!data.isNullOrEmpty() && widthView != 0) {
                val widthItem = widthView / span
                var sumHeight = 0f
                data.forEach { staggeredData ->
                    sumHeight += widthItem / staggeredData.getRatio()
                }
                //Chiều cao dự kiến của mỗi cột
                val heightPerColumns = sumHeight / span
                var col = 0
                var currentItemIndex = 0
                var heightOfColumns = 0f
                var currentOffset = 0f
                if (offsetIndex != -1) {
                    currentOffset = listColumnsData[col].height.toFloat()
                    currentItemIndex = offsetIndex
                    heightOfColumns = listColumnsData[col].height.toFloat()
                }
                while (col < span && currentItemIndex < data.size) {
                    val staggeredData = data[currentItemIndex]
                    val heightOfThisItem = widthItem / staggeredData.getRatio()
                    val heightLess = sumHeight - heightOfColumns - heightOfThisItem
                    if (heightLess < heightOfColumns && col < span - 1) {
                        if (heightOfColumns > maxHeight) {
                            maxHeight = heightOfColumns.toInt()
                        }
                        listColumnsData[col].height = heightOfColumns.toInt()
                        col++
                        currentOffset = 0f
                        heightOfColumns = 0f
                        if (offsetIndex != -1) {
                            currentOffset = listColumnsData[col].height.toFloat()
                            heightOfColumns = listColumnsData[col].height.toFloat()
                        }
                    }
                    if (listColumnsData.size < col + 1) {
                        listColumnsData.add(DataColumn())
                    }
                    val rowData = DataRow<Data>(
                        col, currentOffset, widthItem,
                        heightOfThisItem.toInt()
                    )

                    //add list for checking scroll

                    val indexOfSectionScroll = currentOffset.toInt() / heightScreen
                    if (listSectionIndex.getOrNull(indexOfSectionScroll) == null) {
                        listSectionIndex.add(arrayListOf())
                    }
                    listSectionIndex[indexOfSectionScroll].add(currentItemIndex)

                    mapRowData[staggeredData] = rowData
                    listColumnsData[col].listData.add(staggeredData)
                    heightOfColumns += heightOfThisItem
                    currentItemIndex++

                    if (heightOfColumns >= heightPerColumns || currentItemIndex == getCount()) {
                        if (heightOfColumns > maxHeight) {
                            maxHeight = heightOfColumns.toInt()
                        }
                        listColumnsData[col].height = heightOfColumns.toInt()
                        col++
                        currentOffset = 0f
                        heightOfColumns = 0f
                        if (offsetIndex != -1 && col < span) {
                            currentOffset = listColumnsData[col].height.toFloat()
                            heightOfColumns = listColumnsData[col].height.toFloat()
                        }
                    } else {
                        currentOffset += heightOfThisItem
                    }
                }
                try {
                    refreshData()
                } catch (e: Exception) {
                    postRefreshData()
                }
            }
        }

        fun refreshData() {
            staggeredListView?.validateWithAdapter()
            lastValidateSize = getCount()
        }

        fun postRefreshData() {
            Handler(Looper.getMainLooper()).post {
                staggeredListView?.validateWithAdapter()
                lastValidateSize = getCount()
            }
        }

        fun validateWithList() {
            if (getCount() != lastValidateSize) {
                invalidate(lastValidateSize)
                staggeredListView?.invalidateWithCurrentScroll()
            }
        }

        private fun clearData() {
            mapBinding.clear()
            listColumnsData.clear()
            mapRowData.clear()
            maxHeight = 0

        }

        class DataColumn<Data : StaggeredData>(
            var height: Int = 0,
            var listData: ArrayList<Data> = arrayListOf()
        )

        class DataRow<Data : StaggeredData>(
            var columnIndex: Int,
            var offsetY: Float,
            var width: Int,
            var height: Int
        )

    }

}