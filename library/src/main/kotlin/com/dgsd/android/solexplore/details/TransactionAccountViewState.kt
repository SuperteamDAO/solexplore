package com.dgsd.android.solexplore.details

import com.dgsd.ksol.core.model.PublicKey

internal data class TransactionAccountViewState(
  val accountKey: PublicKey,
  val accountDisplayText: CharSequence,
  val isWriter: Boolean,
  val isProgram: Boolean,
  val isSigner: Boolean,
  val isFeePayer: Boolean,
  val balanceAfterText: CharSequence?,
  val changeInBalanceText: CharSequence?
)