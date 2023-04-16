package com.dgsd.android.solexplore.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal fun <T> Fragment.onEach(flow: Flow<T>, action: suspend (T) -> Unit) {
  flow.onEach(action).launchIn(viewLifecycleOwner.lifecycleScope)
}