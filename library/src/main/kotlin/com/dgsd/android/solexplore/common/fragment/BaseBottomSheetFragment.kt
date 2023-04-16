package com.dgsd.android.solexplore.common.fragment

import android.os.Bundle
import android.view.View
import com.dgsd.android.solexplore.extensions.enableBackgroundBlur
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BaseBottomSheetFragment internal constructor() : BottomSheetDialogFragment() {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    dialog?.window?.enableBackgroundBlur()
  }
}