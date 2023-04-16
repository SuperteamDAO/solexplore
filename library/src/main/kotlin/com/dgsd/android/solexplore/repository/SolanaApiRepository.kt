package com.dgsd.android.solexplore.repository

import com.dgsd.android.solexplore.common.model.Resource
import com.dgsd.android.solexplore.common.util.resourceFlowOf
import com.dgsd.ksol.SolanaApi
import com.dgsd.ksol.core.model.Commitment
import com.dgsd.ksol.core.model.Transaction
import com.dgsd.ksol.core.model.TransactionSignature
import kotlinx.coroutines.flow.Flow

internal class SolanaApiRepository(
  private val solanaApi: SolanaApi,
){

  fun getTransaction(
    transactionSignature: TransactionSignature,
    commitment: Commitment = Commitment.CONFIRMED
  ): Flow<Resource<Transaction>> {
    return resourceFlowOf {
      checkNotNull(solanaApi.getTransaction(transactionSignature, commitment))
    }
  }
}