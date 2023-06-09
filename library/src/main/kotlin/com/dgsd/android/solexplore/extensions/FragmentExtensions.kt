package com.dgsd.android.solexplore.extensions

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

internal fun Fragment.showSnackbar(
  @StringRes messagesResId: Int,
  @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_SHORT,
) {
  showSnackbar(getString(messagesResId), duration)
}

internal fun Fragment.showSnackbar(
  message: CharSequence,
  @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_SHORT,
) {
  val fragmentView = view
  if (fragmentView != null) {
    Snackbar.make(fragmentView, message, duration)
      .setBehavior(BaseTransientBottomBar.Behavior().apply {
        setSwipeDirection(BaseTransientBottomBar.Behavior.SWIPE_DIRECTION_START_TO_END)
      })
      .show()
  }
}