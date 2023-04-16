package com.dgsd.android.solexplore.common.util

import com.dgsd.android.solexplore.common.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext

/**
 * Converts a `suspend` method into a `Flow<Resource>`
 */
fun <T> resourceFlowOf(
  context: CoroutineContext = Dispatchers.IO,
  action: suspend () -> T,
): Flow<Resource<T>> {
  return flow<Resource<T>> {
    emit(Resource.Loading())
    runCatching {
      action.invoke()
    }.onSuccess {
      emit(Resource.Success(it))
    }.onFailure {
      emit(Resource.Error(it))
    }
  }.flowOn(context)
}