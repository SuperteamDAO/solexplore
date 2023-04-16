package com.dgsd.android.solexplore.modalsheet.extensions

import androidx.fragment.app.Fragment
import com.dgsd.android.solexplore.R
import com.dgsd.android.solexplore.modalsheet.ModalSheetFragment
import com.dgsd.android.solexplore.modalsheet.model.ModalInfo

internal fun Fragment.showModal(modalInfo: ModalInfo) {
  val fragment = ModalSheetFragment()
  fragment.modalInfo = modalInfo
  fragment.show(childFragmentManager, null)
}

internal fun Fragment.showModalFromErrorMessage(message: CharSequence) {
  showModal(
    modalInfo = ModalInfo(
      title = getString(R.string.error_modal_default_title),
      message = message,
      positiveButton = ModalInfo.ButtonInfo(
        getString(android.R.string.ok)
      )
    )
  )
}