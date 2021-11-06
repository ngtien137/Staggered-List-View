# Staggered List View
- Custom a Staggered list item looks like Pinterest. Not using Recycler view and StaggeredGridLayoutManager.
- It's just a custom ScrollLayout with something I just made it look a bit like RecyclerView but it's not RecyclerView
- Using Kotlin and databiding for binding item of list. So if you use java or using without databinding, you can refer my library
## Preview 

<img src="https://github.com/ngtien137/Staggered-List-View/blob/master/preview.png" width="540" height="1116"/>
  
## Getting Started
### Configure build.gradle (Project)
* Add these lines:
```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
### Configure build gradle (Module):
* Import module base:
```gradle
dependencies {
  implementation 'com.github.ngtien137:Staggered-List-View:TAG'
}
```
* You can get version of this module [here](https://jitpack.io/#ngtien137/Staggered-List-View)
* Apply plugin for data binding:
```gradle
apply plugin: 'kotlin-kapt'
```
* Add these tags into android tag:
```gradle
android {
  ...
  buildFeatures {
    dataBinding true
}
```
### Configure Model and using
* First, your model need to be extends StaggeredData for define the ratio width/height
```kotlin
interface StaggeredData {
    fun getRatio() : Float
}
//Example
data class AppImage(val id:Long = 0, val path:String = "", val width: Int = 0, val height: Int = 0){
  override fun getRatio(): Float {
        return if (height == 0) 1f else width.toFloat() / height
    }
}
```
* With this ratio, i can calculate the height per item in list
* Then, you initialize Adapter like RecyclerView.Adapter
```kotlin
open class StaggeredAdapter<Data : StaggeredData, ViewBinding : ViewDataBinding>(
        private val context: Context,
        @LayoutRes private val layoutResource: Int,
        private val lifecycleOwner: LifecycleOwner? = null
  ) {
  ...
  }
```
* Initialzie in two ways
```
//Simple
  val adapter = StaggeredListView.StaggeredAdapter<AppImage, ItemImageBinding>(requireContext(),R.layout.item_image)
//If you want configure item like onBindViewHolder, override this function:
  val adapter = (object : StaggeredListView.StaggeredAdapter<AppImage, ItemImageBinding>(
                requireContext(),
                R.layout.item_image
            ) {
                override fun onConfigureWithBinding(binding: ItemImageBinding, itemPosition: Int) {
                    //This function like onBindViewHolder in RecyclerView.Adapter
                }
            }).also { adapter ->
                adapter.listener = this //This is listener which will be passed to item layout
            }
```
* Set adapter to layout
You need set adapter after view posted, so use post function:
```kotlin
binding.staggeredListView.post {
    adapter.data = it ?: arrayListOf()
}
```
* I support you with three variables in itemlayout, if you create three variable with these name, it's will have value in xml layout
```xml
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <data>

        <variable
            name="item"
            type="com.tienuu.demostaggeredlistview.data.AppImage" />

        <variable
            name="itemPosition"
            type="Integer" />

        <variable
            name="listener"
            type="com.tienuu.demostaggeredlistview.adapter.ImageListener" />

    </data>
  
</layout>
```

