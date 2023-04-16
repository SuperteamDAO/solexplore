package com.dgsd.android.solexplore.details

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.dgsd.android.solexplore.R
import com.dgsd.android.solexplore.common.fragment.BaseBottomSheetFragment
import com.dgsd.android.solexplore.extensions.*
import com.dgsd.android.solexplore.extensions.onEach
import com.dgsd.android.solexplore.extensions.showSnackbar
import com.dgsd.android.solexplore.modalsheet.extensions.showModalFromErrorMessage
import com.dgsd.ksol.core.model.TransactionSignature
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class TransactionDetailsFragment : BaseBottomSheetFragment() {

  private val viewModel: TransactionDetailsViewModel by viewModels {
    TransactionDetailsViewModel.factory(
      checkNotNull(requireArguments().getString(ARG_TRANSACTION_SIGNATURE))
    )
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
    dialog.behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    return dialog
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.frag_transaction_details, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val toolbar = view.requireViewById<Toolbar>(R.id.toolbar)
    val scrollView = view.requireViewById<NestedScrollView>(R.id.scroll_view)
    val transactionSignatureHeader =
      view.requireViewById<TextView>(R.id.transaction_signatures_header)
    val transactionSignatureContainer =
      view.requireViewById<LinearLayout>(R.id.transaction_signatures_container)
    val blockTimeHeader = view.requireViewById<TextView>(R.id.block_time_header)
    val blockTime = view.requireViewById<TextView>(R.id.block_time)
    val slotNumber = view.requireViewById<TextView>(R.id.slot)
    val recentBlockHash = view.requireViewById<TextView>(R.id.recent_blockhash)
    val feeHeader = view.requireViewById<TextView>(R.id.fee_header)
    val fee = view.requireViewById<TextView>(R.id.fee)
    val memoHeader = view.requireViewById<TextView>(R.id.memo_header)
    val memo = view.requireViewById<TextView>(R.id.memo)
    val logsHeader = view.requireViewById<TextView>(R.id.logs_header)
    val logsContainer = view.requireViewById<LinearLayout>(R.id.logs_container)
    val accountsHeader = view.requireViewById<TextView>(R.id.accounts_header)
    val accountsContainer = view.requireViewById<LinearLayout>(R.id.accounts_container)
    val loadingIndicator = view.requireViewById<ProgressBar>(R.id.loading_indicator)

    toolbar.setNavigationOnClickListener {
      dismissAllowingStateLoss()
    }

    scrollView.outlineProvider = ViewOutlineProvider.BOUNDS
    scrollView.clipToOutline = true

    recentBlockHash.setOnClickListener {
      viewModel.onRecentBlockhasClicked()
    }

    onEach(viewModel.showLoadingState) {
      loadingIndicator.isVisible = it
      scrollView.isInvisible = it
    }

    onEach(viewModel.errorMessage) {
      showModalFromErrorMessage(it)
    }

    onEach(viewModel.transactionSignatureHeaderText) {
      transactionSignatureHeader.text = it
    }

    onEach(viewModel.transactionSignatures) {
      transactionSignatureContainer.bindSignatures(it)
    }

    onEach(viewModel.feeText) {
      if (it.isNullOrEmpty()) {
        feeHeader.isVisible = false
        fee.isVisible = false
      } else {
        feeHeader.isVisible = true
        fee.isVisible = true
        fee.text = it
      }
    }

    onEach(viewModel.memoText) {
      if (it.isNullOrEmpty()) {
        memoHeader.isVisible = false
        memo.isVisible = false
      } else {
        memoHeader.isVisible = true
        memo.isVisible = true
        memo.text = it
      }
    }

    onEach(viewModel.showConfirmationMessage) {
      showSnackbar(it)
    }

    onEach(viewModel.blockTimeText) {
      if (it.isNullOrEmpty()) {
        blockTime.isVisible = false
        blockTimeHeader.isVisible = false
      } else {
        blockTime.isVisible = true
        blockTimeHeader.isVisible = true

        blockTime.text = it
      }
    }

    onEach(viewModel.recentBlockHashText) {
      recentBlockHash.text = it
    }

    onEach(viewModel.slotNumber) {
      slotNumber.text = it
    }

    onEach(viewModel.logMessages) {
      logsHeader.isVisible = it.isNotEmpty()
      logsContainer.isVisible = it.isNotEmpty()

      logsContainer.bindLogs(it)
    }

    onEach(viewModel.accountDetails) { accountDetails ->
      accountsHeader.isVisible = accountDetails.isNotEmpty()
      accountsContainer.isVisible = accountDetails.isNotEmpty()

      accountsContainer.bindAccounts(accountDetails)
    }

    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
      viewModel.onCreate()
    }
  }

  private fun LinearLayout.bindSignatures(signatures: List<TransactionSignature>) {
    ensureViewCount(signatures.size) {
      LayoutInflater.from(context).inflate(
        R.layout.view_transaction_details_transaction_signature,
        this,
        true
      )
    }

    children.toList().zip(signatures) { view, signature ->
      (view as TextView).text = signature
      view.setOnClickListener {
        viewModel.onSignatureClicked(signature)
      }
    }
  }

  private fun LinearLayout.bindLogs(logEntries: List<String>) {
    ensureViewCount(logEntries.size) {
      LayoutInflater.from(context).inflate(
        R.layout.view_transaction_details_log_row, this, true
      )
    }

    children.toList().zip(logEntries) { view, logMessage ->
      (view as TextView).text = logMessage
    }
  }

  private fun LinearLayout.bindAccounts(accounts: List<TransactionAccountViewState>) {
    ensureViewCount(accounts.size) {
      LayoutInflater.from(context).inflate(
        R.layout.view_transaction_details_account, this, true
      )
    }

    children.toList().zip(accounts) { view, account ->
      val accountKey = view.requireViewById<TextView>(R.id.account_key)
      val balanceAfter = view.requireViewById<TextView>(R.id.balance_after)
      val changeInBalance = view.requireViewById<TextView>(R.id.change_in_balance)
      val writerBadge = view.requireViewById<View>(R.id.writer_badge)
      val signerBadge = view.requireViewById<View>(R.id.signer_badge)
      val feePayerBadge = view.requireViewById<View>(R.id.fee_payer_badge)
      val programBadge = view.requireViewById<View>(R.id.program_badge)

      arrayOf(
        writerBadge,
        signerBadge,
        feePayerBadge,
        programBadge,
      ).forEach {
        it.roundedCorners(it.context.dpToPx(8))
      }

      accountKey.text = account.accountDisplayText
      balanceAfter.text = account.balanceAfterText
      balanceAfter.isVisible = !account.balanceAfterText.isNullOrEmpty()

      changeInBalance.text = account.changeInBalanceText
      changeInBalance.isVisible = !account.changeInBalanceText.isNullOrEmpty()

      writerBadge.isVisible = account.isWriter
      signerBadge.isVisible = account.isSigner
      feePayerBadge.isVisible = account.isFeePayer
      programBadge.isVisible = account.isProgram

      view.setOnClickListener {
        viewModel.onAccountClicked(account.accountKey)
      }
    }
  }

  companion object {

    private const val ARG_TRANSACTION_SIGNATURE = "transaction_signature"

    fun newInstance(transactionSignature: TransactionSignature): TransactionDetailsFragment {
      return TransactionDetailsFragment().apply {
        arguments = bundleOf(
          ARG_TRANSACTION_SIGNATURE to transactionSignature
        )
      }
    }
  }
}