package com.example.flow

import co.paralleluniverse.fibers.Suspendable
import com.example.contract.IOUContract
import com.example.flow.FlowUpdate.Acceptor
import com.example.flow.FlowUpdate.InitiatorUpdate
import com.example.iou.ReceitaIOU
import com.example.iou.Venda
import com.example.iou.VendaIOU
import com.example.state.IOUState
import com.google.common.collect.ImmutableList
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step


/**
 * This flow allows two parties (the [InitiatorUpdate] and the [Acceptor]) to come to an agreement about the IOU encapsulated
 * within an [IOUState].
 *
 * In our simple example, the [Acceptor] always accepts a valid IOU.
 *
 * These flows have deliberately been implemented by using only the call() method for ease of understanding. In
 * practice we would recommend splitting up the various stages of the flow into sub-routines.
 *
 * All methods called within the [FlowLogic] sub-class need to be annotated with the @Suspendable annotation.
 */
object FlowUpdate {
    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    @InitiatingFlow
    @StartableByRPC
    class InitiatorUpdate(private val linearId: UniqueIdentifier, private val vendaFarma: Venda) : FlowLogic<SignedTransaction>() {
        /**
         * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
         * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
         */
        companion object {
            object GENERATING_TRANSACTION : Step("Generating transaction based on new IOU.")
            object VERIFYING_OBLIGATION_NOT_FOUND : Step("Obligation with id inputed not found.")
            object VERIFYING_TRANSACTION : Step("Verifying contract constraints.")
            object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
            object GATHERING_SIGS : Step("Gathering the counterparty's signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    GENERATING_TRANSACTION,
                    VERIFYING_OBLIGATION_NOT_FOUND,
                    VERIFYING_TRANSACTION,
                    SIGNING_TRANSACTION,
                    GATHERING_SIGS,
                    FINALISING_TRANSACTION
            )
        }

        override val progressTracker = tracker()

        /**
         * The flow logic is encapsulated within the call() method.
         */
        @Suspendable
        override fun call(): SignedTransaction {

            // Obtain a reference from a notary we wish to use.
            /**
             *  METHOD 1: Take first notary on network, WARNING: use for test, non-prod environments, and single-notary networks only!*
             *  METHOD 2: Explicit selection of notary by CordaX500Name - argument can by coded in flow or parsed from config (Preferred)
             *
             *  * - For production you always want to use Method 2 as it guarantees the expected notary is returned.
             */
            val notary = serviceHub.networkMapCache.notaryIdentities.single() // METHOD 1
            // val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")) // METHOD 2

            // Stage 1.
            progressTracker.currentStep = GENERATING_TRANSACTION

            // Obtein reference to the node
            val myIdentitie = serviceHub.myInfo.legalIdentities.first()

            //Participantes da receita
            val participantes: List<String>

            //Lista de vendedores
            val listVenda: List<Venda>

            //Quantidade de medicamentos vendida
            var quantidadeTotalVendida: Int = 0

            // Generate an unsigned transaction.
            val queryCriteria = QueryCriteria.LinearStateQueryCriteria(
                    null,
                    ImmutableList.of(linearId),
                    Vault.StateStatus.UNCONSUMED,
                    null)
            val obligations = serviceHub.vaultService.queryBy(IOUState::class.java, queryCriteria).states
            if (obligations.isEmpty())
            {
                progressTracker.currentStep = VERIFYING_OBLIGATION_NOT_FOUND
                throw FlowException(String.format("Receita não encontrada no sistema. Código: %s inválido", linearId.toString()))
            }
            val inputStateAndRef = obligations[0]
            participantes = obligations[0].state.data.allParticipants.plus(myIdentitie.name.organisation)
            if(obligations[0].state.data.iouVenda?.venda == null){
                listVenda = listOf(vendaFarma)
                quantidadeTotalVendida = vendaFarma.quantidadeMedVendida
//                if(quantidadeTotalVendida > obligations[0].state.data.iouReceita.receita.quantidadeMedicamento){
//                    throw FlowException(String.format("Quantidade vendida maior que a receitada. Código: %s inválido", quantidadeTotalVendida.toString()))
//                }
            }else {
                listVenda = obligations[0].state.data.iouVenda?.venda!!.plus(vendaFarma)
                for (i in 0 until listVenda.size){
                    quantidadeTotalVendida = quantidadeTotalVendida + listVenda[i].quantidadeMedVendida
                }
//                if(quantidadeTotalVendida > obligations[0].state.data.iouReceita.receita.quantidadeMedicamento){
//                    throw FlowException(String.format("Quantidade vendida ultrapassa soma de medicamento receitado. Código: %s inválido", quantidadeTotalVendida.toString()))
//                }
            }
            val input = inputStateAndRef.state.data
                val iouState = IOUState(
                        input.dataEmissao,
                        ReceitaIOU(
                                input.iouReceita.receita
                        ),
                        VendaIOU(
                               listVenda
                        ),
                        quantidadeTotalVendida,
                        participantes,
                        myIdentitie
                )
            val output = IOUState(input.dataEmissao,input.iouReceita,iouState.iouVenda,quantidadeTotalVendida,participantes,myIdentitie,linearId)
            val txCommand = Command(IOUContract.Commands.Update(), output.participants.map { it.owningKey })
            val txBuilder = TransactionBuilder(notary)
                    .addInputState(inputStateAndRef.referenced().stateAndRef)
                    .addOutputState(output, IOUContract.ID)
                    .addCommand(txCommand)

            // Stage 2.
            progressTracker.currentStep = VERIFYING_TRANSACTION
            // Verify that the transaction is valid.
            txBuilder.verify(serviceHub)

            // Stage 3.
            progressTracker.currentStep = SIGNING_TRANSACTION
            // Sign the transaction.
            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            // Stage 4.
            progressTracker.currentStep = GATHERING_SIGS
            // Send the state to the counterparty, and receive it back with their signature.
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, emptySet(), GATHERING_SIGS.childProgressTracker()))

            // Stage 5.
            progressTracker.currentStep = FINALISING_TRANSACTION
            // Notarise and record the transaction in both parties' vaults.
            val notarizedTx = subFlow(FinalityFlow(fullySignedTx, FINALISING_TRANSACTION.childProgressTracker()))
            subFlow(BroadcastTransactionFlow(notarizedTx))
            return notarizedTx
        }
    }

    @InitiatedBy(InitiatorUpdate::class)
    class Acceptor(val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be an IOU transaction." using (output is IOUState)
                    val iou = output as IOUState
                    "I won't accept IOUs with negative values." using (iou.iouReceita.receita.numeroReceita >= 0)
                    }
            }
            val txId = subFlow(signTransactionFlow).id

            return subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txId))
        }
    }
}
