package com.example.state

import com.example.contract.IOUContract
import com.example.schema.IOUSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

/**
 * The state object recording IOU agreements between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 *
 * @param value the value of the IOU.
 * @param lender the party issuing the IOU.
 * @param borrower the party receiving and approving the IOU.
 */
@BelongsToContract(IOUContract::class)
data class IOUState(val remetente: Party,
                    val dataEmissao: String,
                    val numeroReceita: Int,
                    val nomePaciente: String,
                    val enderecoPaciente: String,
                    val nomeMedico: String,
                    val crmMedico: Int,
                    val nomeMedicamento: String,
                    val quantidadeMedicamento: Int,
                    val formulaMedicamento: String,
                    val doseUnidade: String,
                    val posologia: Int,
                    override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState, QueryableState {
    /** The public keys of the involved parties. */
    override val participants: List<AbstractParty> get() = listOf(remetente)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is IOUSchemaV1 -> IOUSchemaV1.PersistentIOU(
                    this.remetente.name.toString(),
                    this.dataEmissao,
                    this.numeroReceita,
                    this.nomePaciente,
                    this.enderecoPaciente,
                    this.nomeMedico,
                    this.crmMedico,
                    this.nomeMedicamento,
                    this.quantidadeMedicamento,
                    this.formulaMedicamento,
                    this.doseUnidade,
                    this.posologia,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(IOUSchemaV1)
}