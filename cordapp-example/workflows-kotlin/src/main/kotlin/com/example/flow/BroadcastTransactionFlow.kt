package com.example.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction

@InitiatingFlow
class BroadcastTransactionFlow(private val stx: SignedTransaction) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // Get a list of all identities from the network map cache.
        val everyone = serviceHub.networkMapCache.allNodes.flatMap { it.legalIdentities }

        // Filter out the notary identities and remove our identity.
        val everyoneButMeAndNotary = everyone.filter { serviceHub.networkMapCache.isNotary(it).not() }

        // Create a session for each remaining party.
        val sessions = everyoneButMeAndNotary.map { initiateFlow(it) }

        // Send the transaction to all the remaining parties.
        sessions.forEach { subFlow(SendTransactionFlow(it, stx)) }
    }
}

@InitiatedBy(BroadcastTransactionFlow::class)
class BraodcastTransactionResponder(private val session: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        subFlow(ReceiveTransactionFlow(session, statesToRecord = StatesToRecord.ALL_VISIBLE))
    }
}
