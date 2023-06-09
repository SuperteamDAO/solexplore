package com.dgsd.android.solexplore.extensions

import com.dgsd.ksol.core.model.PublicKey
import com.dgsd.ksol.core.model.TransactionMessage
import com.dgsd.ksol.core.programs.memo.MemoProgram
import com.dgsd.ksol.core.programs.system.SystemProgram
import com.dgsd.ksol.core.programs.system.SystemProgramInstructionData

fun TransactionMessage.getSystemProgramInstruction(): SystemProgramInstructionData? {
  return runCatching {
    val instruction =
      instructions.firstOrNull { it.programAccount == SystemProgram.PROGRAM_ID }
    if (instruction == null) {
      null
    } else {
      SystemProgram.decodeInstruction(instruction.inputData)
    }
  }.getOrNull()
}

fun TransactionMessage.getMemoMessage(): String? {
  return runCatching {
    val memoInstruction = instructions.firstOrNull { it.programAccount == MemoProgram.PROGRAM_ID }
    if (memoInstruction == null) {
      null
    } else {
      MemoProgram.decodeInstruction(memoInstruction.inputData)
    }
  }.getOrNull()
}

fun TransactionMessage.extractBestDisplayRecipient(currentSession: PublicKey): PublicKey? {
  return accountKeys
    .firstOrNull { account -> account.isWritable && account.publicKey != currentSession }
    ?.publicKey
}