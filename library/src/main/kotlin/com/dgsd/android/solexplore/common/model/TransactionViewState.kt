package com.dgsd.android.solexplore.common.model

import com.dgsd.ksol.core.model.TransactionSignature

sealed interface TransactionViewState {

  data class Loading(val transactionSignature: TransactionSignature?) : TransactionViewState

  data class Error(val transactionSignature: TransactionSignature?) : TransactionViewState

  data class Transaction(
    val transactionSignature: TransactionSignature,
    val direction: Direction,
    val displayAccountText: CharSequence,
    val amountText: CharSequence,
    val dateText: CharSequence?,
  ) : TransactionViewState {
    enum class Direction {
      INCOMING,
      OUTGOING,
      NONE,
    }
  }
}