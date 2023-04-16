package com.dgsd.android.solexplore.common.error

import android.content.Context
import com.dgsd.android.solexplore.R
import com.dgsd.android.solexplore.common.model.UserFacingException

/**
 * For creating common error messages shown throughout the app
 */
class ErrorMessageFactory(
  private val context: Context,
) {

  fun create(error: Throwable): CharSequence {
    return create(error, createDefault())
  }

  fun create(error: Throwable, defaultMessage: CharSequence): CharSequence {
    return when (error) {
      is UserFacingException -> error.userVisibleMessage.ifEmpty { defaultMessage }
      else -> defaultMessage
    }
  }

  fun createDefault(): CharSequence {
    return context.getString(R.string.error_message_generic)
  }
}