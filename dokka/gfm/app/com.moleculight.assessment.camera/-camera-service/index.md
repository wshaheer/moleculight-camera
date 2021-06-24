//[app](../../../index.md)/[com.moleculight.assessment.camera](../index.md)/[CameraService](index.md)



# CameraService  
 [androidJvm] class [CameraService](index.md)(**id**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **view**: [TextureView](https://developer.android.com/reference/kotlin/android/view/TextureView.html), **thread**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **listener**: [ICamera](../-i-camera/index.md))   


## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="com.moleculight.assessment.camera/CameraService.Companion///PointingToDeclaration/"></a>[Companion](-companion/index.md)| <a name="com.moleculight.assessment.camera/CameraService.Companion///PointingToDeclaration/"></a>[androidJvm]  <br>Content  <br>object [Companion](-companion/index.md)  <br><br><br>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="com.moleculight.assessment.camera/CameraService/activity/#/PointingToDeclaration/"></a>[activity](activity.md)| <a name="com.moleculight.assessment.camera/CameraService/activity/#/PointingToDeclaration/"></a> [androidJvm] val [activity](activity.md): [Activity](https://developer.android.com/reference/kotlin/android/app/Activity.html)   <br>|
| <a name="com.moleculight.assessment.camera/CameraService/elapsedFrames/#/PointingToDeclaration/"></a>[elapsedFrames](elapsed-frames.md)| <a name="com.moleculight.assessment.camera/CameraService/elapsedFrames/#/PointingToDeclaration/"></a> [androidJvm] var [elapsedFrames](elapsed-frames.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0   <br>|
| <a name="com.moleculight.assessment.camera/CameraService/elapsedSeconds/#/PointingToDeclaration/"></a>[elapsedSeconds](elapsed-seconds.md)| <a name="com.moleculight.assessment.camera/CameraService/elapsedSeconds/#/PointingToDeclaration/"></a> [androidJvm] var [elapsedSeconds](elapsed-seconds.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)   <br>|
| <a name="com.moleculight.assessment.camera/CameraService/id/#/PointingToDeclaration/"></a>[id](id.md)| <a name="com.moleculight.assessment.camera/CameraService/id/#/PointingToDeclaration/"></a> [androidJvm] val [id](id.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br>|
| <a name="com.moleculight.assessment.camera/CameraService/listener/#/PointingToDeclaration/"></a>[listener](listener.md)| <a name="com.moleculight.assessment.camera/CameraService/listener/#/PointingToDeclaration/"></a> [androidJvm] val [listener](listener.md): [ICamera](../-i-camera/index.md)   <br>|
| <a name="com.moleculight.assessment.camera/CameraService/previewing/#/PointingToDeclaration/"></a>[previewing](previewing.md)| <a name="com.moleculight.assessment.camera/CameraService/previewing/#/PointingToDeclaration/"></a> [androidJvm] var [previewing](previewing.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false   <br>|
| <a name="com.moleculight.assessment.camera/CameraService/state/#/PointingToDeclaration/"></a>[state](state.md)| <a name="com.moleculight.assessment.camera/CameraService/state/#/PointingToDeclaration/"></a> [androidJvm] var [state](state.md): [CameraState](../-camera-state/index.md)   <br>|
| <a name="com.moleculight.assessment.camera/CameraService/thread/#/PointingToDeclaration/"></a>[thread](thread.md)| <a name="com.moleculight.assessment.camera/CameraService/thread/#/PointingToDeclaration/"></a> [androidJvm] val [thread](thread.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br>|
| <a name="com.moleculight.assessment.camera/CameraService/view/#/PointingToDeclaration/"></a>[view](view.md)| <a name="com.moleculight.assessment.camera/CameraService/view/#/PointingToDeclaration/"></a> [androidJvm] val [view](view.md): [TextureView](https://developer.android.com/reference/kotlin/android/view/TextureView.html)   <br>|


## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="com.moleculight.assessment.camera/CameraService/closeCamera/#/PointingToDeclaration/"></a>[closeCamera](close-camera.md)| <a name="com.moleculight.assessment.camera/CameraService/closeCamera/#/PointingToDeclaration/"></a>[androidJvm]  <br>Content  <br>fun [closeCamera](close-camera.md)()  <br><br><br>|
| <a name="com.moleculight.assessment.camera/CameraService/openCamera/#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>[openCamera](open-camera.md)| <a name="com.moleculight.assessment.camera/CameraService/openCamera/#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>[androidJvm]  <br>Content  <br>fun [openCamera](open-camera.md)(width: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), height: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))  <br><br><br>|
| <a name="com.moleculight.assessment.camera/CameraService/shouldCaptureImage/#/PointingToDeclaration/"></a>[shouldCaptureImage](should-capture-image.md)| <a name="com.moleculight.assessment.camera/CameraService/shouldCaptureImage/#/PointingToDeclaration/"></a>[androidJvm]  <br>Content  <br>fun [shouldCaptureImage](should-capture-image.md)()  <br><br><br>|
| <a name="com.moleculight.assessment.camera/CameraService/shouldLockFocus/#/PointingToDeclaration/"></a>[shouldLockFocus](should-lock-focus.md)| <a name="com.moleculight.assessment.camera/CameraService/shouldLockFocus/#/PointingToDeclaration/"></a>[androidJvm]  <br>Content  <br>fun [shouldLockFocus](should-lock-focus.md)()  <br><br><br>|
| <a name="com.moleculight.assessment.camera/CameraService/shouldStartPreview/#/PointingToDeclaration/"></a>[shouldStartPreview](should-start-preview.md)| <a name="com.moleculight.assessment.camera/CameraService/shouldStartPreview/#/PointingToDeclaration/"></a>[androidJvm]  <br>Content  <br>fun [shouldStartPreview](should-start-preview.md)()  <br><br><br>|
| <a name="com.moleculight.assessment.camera/CameraService/shouldStopPreview/#/PointingToDeclaration/"></a>[shouldStopPreview](should-stop-preview.md)| <a name="com.moleculight.assessment.camera/CameraService/shouldStopPreview/#/PointingToDeclaration/"></a>[androidJvm]  <br>Content  <br>fun [shouldStopPreview](should-stop-preview.md)()  <br><br><br>|
| <a name="com.moleculight.assessment.camera/CameraService/shouldUnlockFocus/#/PointingToDeclaration/"></a>[shouldUnlockFocus](should-unlock-focus.md)| <a name="com.moleculight.assessment.camera/CameraService/shouldUnlockFocus/#/PointingToDeclaration/"></a>[androidJvm]  <br>Content  <br>fun [shouldUnlockFocus](should-unlock-focus.md)()  <br><br><br>|
| <a name="com.moleculight.assessment.camera/CameraService/shouldUpdatePreview/#/PointingToDeclaration/"></a>[shouldUpdatePreview](should-update-preview.md)| <a name="com.moleculight.assessment.camera/CameraService/shouldUpdatePreview/#/PointingToDeclaration/"></a>[androidJvm]  <br>Content  <br>fun [shouldUpdatePreview](should-update-preview.md)()  <br><br><br>|

