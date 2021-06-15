# SimpleGallery：以非侵入为目标的相册获取框架

## 使用：

1. 添加 maven

   ```groovy
   allprojects {
   		repositories {
   			...
   			maven { url 'https://jitpack.io' }
   		}
   }
   ```

2. 在module 的 build.gradle中添加dataBinding

   ```groovy
   android {
       dataBinding {
           enabled = true
       }

       /*...*/
   }
   ```

3. 添加依赖

   [![](https://jitpack.io/v/gfzy9876/GalleryModel.svg)](https://jitpack.io/#gfzy9876/GalleryModel)

   ```groovy
   dependencies {
     	implementation 'com.github.gfzy9876.SimpleGallery:gallerylib:latest-release'
     	kapt 'com.github.gfzy9876.SimpleGallery:apt-processor:latest-release' //注解解释器
   }
   ```

4. 使用

   1. 在Application.onCreate 中调用(若项目无Application需自行创建)

      ```kotlin
      class TestApplication : Application() {
          override fun onCreate() {
              super.onCreate()
              GalleryCommon.init(this) //调用这个
          }
      }
      ```

   2. 调用入口在`MediaInfoDispatcher`类

      ```kotlin
      MediaInfoDispatcher.newInstance() //创建实例
                  .ofImage() // 获取图片，支持ofImage, ofVideo, ofAll
                  .supportGIF() //是否显示gif
                  .setMaxMediaCount(3) //最多选择文件数量
                  .setMinMediaCount(1) //最少选择文件数量
                  .start(this) //启动


      ```

      `MediaInfoDispatcher.start()` 有两种复写：

      其中`target`表示`相册数据接收类`

      ```kotlin
      fun start(context: Context, target: Any)  //自定义数据接收类
      fun start(activity: Activity)  //默认数据接收类为Activity
      ```

   3. 获取回调结果

      在`相册数据接收类`中添加方法，并在方法处添加`@MediaInfoReceived`注解：

      ```kotlin
      @MediaInfoReceived
      fun onMediaInfoReceived(result: List<MediaInfo>) {
          val gson = Gson()
          result.forEach {
            Log.d("GFZY", "onMediaInfoReceived ${gson.toJson(it)}")
          }
          val realPath = result[0].realPath
          Glide.with(this)
               .load(realPath)
               .into(iv_test)
      }
      ```

   4. 添加混淆

      ```
      -keep class **_MediaInfoProxy {*;}
      ```

   框架还在完善中，很多功能可能还需要添加，希望各位在使用后可以在issue提交反馈。

