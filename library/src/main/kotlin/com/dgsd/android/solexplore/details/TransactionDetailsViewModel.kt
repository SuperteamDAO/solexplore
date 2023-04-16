package com.dgsd.android.solexplore.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dgsd.android.solexplore.R
import com.dgsd.android.solexplore.common.clipboard.SystemClipboard
import com.dgsd.android.solexplore.common.error.ErrorMessageFactory
import com.dgsd.android.solexplore.common.flow.MutableEventFlow
import com.dgsd.android.solexplore.common.flow.asEventFlow
import com.dgsd.android.solexplore.common.util.DateTimeFormatter
import com.dgsd.android.solexplore.common.util.PublicKeyFormatter
import com.dgsd.android.solexplore.common.util.ResourceFlowConsumer
import com.dgsd.android.solexplore.common.util.SolTokenFormatter
import com.dgsd.android.solexplore.extensions.getMemoMessage
import com.dgsd.android.solexplore.extensions.getString
import com.dgsd.android.solexplore.repository.SolanaApiRepository
import com.dgsd.ksol.SolanaApi
import com.dgsd.ksol.core.model.Cluster
import com.dgsd.ksol.core.model.PublicKey
import com.dgsd.ksol.core.model.Transaction
import com.dgsd.ksol.core.model.TransactionSignature
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.text.NumberFormat
import kotlin.math.abs

internal class TransactionDetailsViewModel(
  application: Application,
  private val publicKeyFormatter: PublicKeyFormatter,
  private val solanaApiRepository: SolanaApiRepository,
  private val errorMessageFactory: ErrorMessageFactory,
  private val systemClipboard: SystemClipboard,
  private val transactionSignature: TransactionSignature,
) : AndroidViewModel(application) {

  private val transactionResourceConsumer = ResourceFlowConsumer<Transaction>(viewModelScope)

  private val transaction = transactionResourceConsumer.data.filterNotNull()

  val showLoadingState = transactionResourceConsumer.isLoadingOrError

  val transactionSignatureHeaderText = transaction.map {
    if (it.signatures.size != 1) {
      getString(R.string.transaction_details_section_title_signatures)
    } else {
      getString(R.string.transaction_details_section_title_signature)
    }
  }

  val transactionSignatures = transaction.map { it.signatures }

  val feeText = transaction.map { SolTokenFormatter.format(it.metadata.fee) }

  val memoText = transaction.map {
    it.message.getMemoMessage()
  }

  val blockTimeText = transaction.map {
    val blockTime = it.blockTime
    if (blockTime == null) {
      null
    } else {
      DateTimeFormatter.formatDateAndTimeLong(application, blockTime)
    }
  }

  val recentBlockHashText = transaction.map {
    publicKeyFormatter.format(it.message.recentBlockhash)
  }

  val slotNumber = transaction.map {
    NumberFormat.getNumberInstance().format(it.slot)
  }

  val logMessages = transaction.map { it.metadata.logMessages }

  val accountDetails = transaction.map { transaction ->
    transaction.message.accountKeys.map { accountMetadata ->
      val accountDisplayText = publicKeyFormatter.format(accountMetadata.publicKey)

      val programAccounts = transaction.message.instructions.map { it.programAccount }.toSet()

      val balanceAfter = transaction.metadata.accountBalances.firstOrNull {
        it.accountKey == accountMetadata.publicKey
      }?.balanceAfter
      val balanceBefore = transaction.metadata.accountBalances.firstOrNull {
        it.accountKey == accountMetadata.publicKey
      }?.balanceBefore

      val balanceAfterText = if (balanceAfter == null) {
        null
      } else {
        SolTokenFormatter.format(balanceAfter)
      }

      val changeInBalanceText = if (balanceAfter == null || balanceBefore == null) {
        null
      } else {
        val difference = balanceAfter - balanceBefore
        if (difference == 0L) {
          null
        } else if (difference < 0) {
          "- ${SolTokenFormatter.format(abs(difference))}"
        } else {
          "+ ${SolTokenFormatter.format(difference)}"
        }
      }

      TransactionAccountViewState(
        accountKey = accountMetadata.publicKey,
        accountDisplayText = accountDisplayText,
        isWriter = accountMetadata.isWritable,
        isSigner = accountMetadata.isSigner,
        isFeePayer = accountMetadata.isFeePayer,
        isProgram = accountMetadata.publicKey in programAccounts,
        balanceAfterText = balanceAfterText,
        changeInBalanceText = changeInBalanceText
      )
    }
  }

  val errorMessage = transactionResourceConsumer.error
    .filterNotNull()
    .map { errorMessageFactory.create(it) }
    .asEventFlow(viewModelScope)

  private val _showConfirmationMessage = MutableEventFlow<CharSequence>()
  val showConfirmationMessage = _showConfirmationMessage.asEventFlow()

  fun onCreate() {
    reloadData()
  }

  fun onRecentBlockhasClicked() {
    val blockHash = transactionResourceConsumer.data.value?.message?.recentBlockhash
    if (blockHash != null) {
      systemClipboard.copy(blockHash.toBase58String())
      _showConfirmationMessage.tryEmit(
        getString(R.string.copied_to_clipboard)
      )
    }
  }

  fun onSignatureClicked(signature: TransactionSignature) {
    systemClipboard.copy(signature)
    _showConfirmationMessage.tryEmit(
      getString(R.string.copied_to_clipboard)
    )
  }

  private fun reloadData() {
    transactionResourceConsumer.collectFlow(
      solanaApiRepository.getTransaction(transactionSignature)
    )
  }

  fun onAccountClicked(accountKey: PublicKey) {
    systemClipboard.copy(accountKey.toBase58String())
    _showConfirmationMessage.tryEmit(
      getString(R.string.account_key_copied_to_clipboard)
    )
  }

  companion object {

    fun factory(signature: TransactionSignature): ViewModelProvider.Factory = viewModelFactory {
      initializer {
        val application = this[APPLICATION_KEY] as Application
        TransactionDetailsViewModel(
          application = application,
          transactionSignature = signature,
          publicKeyFormatter = PublicKeyFormatter(application),
          errorMessageFactory = ErrorMessageFactory(application),
          systemClipboard = SystemClipboard(application),
          solanaApiRepository = SolanaApiRepository(
            SolanaApi(Cluster.DEVNET)
          )
        )
      }
    }
  }
}