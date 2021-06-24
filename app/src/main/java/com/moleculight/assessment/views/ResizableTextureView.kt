package com.moleculight.assessment.views

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import kotlin.math.roundToInt

class ResizableTextureView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet,
    defStyle: Int = 0
) : TextureView(context, attributeSet, defStyle) {
    private var aspectRatio = 0f


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        if (aspectRatio == 0f) {
            setMeasuredDimension(width, height)
        } else {
            val newWidth: Int
            val newHeight: Int
            val actualRatio = if (width > height) aspectRatio else 1f / aspectRatio

            if (width < height * actualRatio) {
                newHeight = height
                newWidth = (height * actualRatio).roundToInt()
            } else {
                newWidth = width
                newHeight = (width / actualRatio).roundToInt()
            }

            setMeasuredDimension(newWidth, newHeight)
        }
    }

    fun setAspectRatio(width: Int, height: Int) {
        require(width > 0 && height > 0) { "Size cannot be negative" }

        aspectRatio = width.toFloat() / height.toFloat()

        requestLayout()
    }
}