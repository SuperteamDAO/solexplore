package com.dgsd.android.solexplore.extensions

import android.content.Context
import android.util.TypedValue
import androidx.annotation.Dimension

@Dimension(unit = Dimension.PX)
fun Context.dpToPx(@Dimension(unit = Dimension.DP) dp: Int): Float {
  return TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    dp.toFloat(),
    resources.displayMetrics
  )
}