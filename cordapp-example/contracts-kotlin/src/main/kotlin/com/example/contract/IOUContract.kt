package com.example.contract

import com.example.state.IOUState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters


/**
 * A implementation of a basic smart contract in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [IOUState], which in turn encapsulates an [IOUState].
 *
 * For a new [IOUState] to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [IOUState].
 * - An Create() command with the public keys of both the lender and the borrower.
 *
 * All contracts must sub-class the [Contract] interface.
 */
class IOUContract : Contract {
    companion object {
        @JvmStatic
        val ID = "com.example.contract.IOUContract"
    }

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        val setOfSigners = command.signers.toSet()
        when (command.value) {
            is Commands.Create -> verifyCreate(tx, setOfSigners)
            is Commands.Update -> verifyUpdate(tx, setOfSigners)
            else -> throw IllegalArgumentException("Unrecognised command")
        }
    }

    private fun verifyCreate(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        "Nenhuma receita deve ser usada como input." using (tx.inputs.isEmpty())
        "Apenas uma receita deve ser gerada como output." using (tx.outputs.size == 1)
        val out = tx.outputsOfType<IOUState>().single()
        "Todos os participantes devem ser signatários." using (signers.containsAll(out.participants.map { it.owningKey }))
        "Dose diária não pode ultrapassar 15mg" using (out.iouReceita.receita.doseUnidade <= 15)
    }

    private fun verifyUpdate(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        "Uma receita deve ser usada como input." using (tx.inputs.isNotEmpty())
        "Apenas uma receita deve ser gerada como output." using (tx.outputs.size == 1)
        val out = tx.outputsOfType<IOUState>().single()
       "Todos os participantes devem ser signatários" using (signers.containsAll(out.participants.map { it.owningKey }))
        val input = tx.inputsOfType<IOUState>().single()
        "Código da receita consumida deve ser igual da receita gerada." using (input.linearId == out.linearId)
        "A quantitade total do medicamento já foi vendida" using (input.iouVenda == null)
        val firstIntervalDate = LocalDate.of(input.dataEmissao.year,input.dataEmissao.month, input.dataEmissao.dayOfMonth).atStartOfDay()
        val secondIntervalDate = firstIntervalDate.with(TemporalAdjusters.lastDayOfMonth()).plusDays(30)
           "Receita está fora da data limite de validade: 30 dias" using (out.iouVenda?.venda?.data!! <= secondIntervalDate)
    }

    /**
     * This contract only implements one command, Create.
     */
    interface Commands : CommandData {
        class Create : Commands
        class Update : Commands
    }
}
