package com.example.state

import com.example.contract.IOUContract
import com.example.iou.ReceitaIOU
import com.example.iou.VendaIOU
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
import java.time.LocalDateTime

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
data class IOUState(val dataEmissao: LocalDateTime,
                    val iouReceita: ReceitaIOU,
                    val iouVenda: VendaIOU? = null,
                    val remetente: Party,
                    override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState, QueryableState {
    /** The public keys of the involved parties. */
    override val participants: List<AbstractParty> get() = listOf(remetente)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is IOUSchemaV1 -> IOUSchemaV1.PersistentIOU(
                    this.dataEmissao,
                    this.iouReceita.receita.numeroReceita,
                    this.iouReceita.receita.nomePaciente,
                    this.iouReceita.receita.enderecoPaciente,
                    this.iouReceita.receita.nomeMedico,
                    this.iouReceita.receita.crmMedico,
                    this.iouReceita.receita.nomeMedicamento,
                    this.iouReceita.receita.quantidadeMedicamento,
                    this.iouReceita.receita.formulaMedicamento,
                    this.iouReceita.receita.doseUnidade,
                    this.iouReceita.receita.posologia,
                    this.iouVenda?.venda?.comprador,
                    this.iouVenda?.venda?.enderecoComprador,
                    this.iouVenda?.venda?.rg,
                    this.iouVenda?.venda?.telefone,
                    this.iouVenda?.venda?.nomeVendedor,
                    this.iouVenda?.venda?.cnpj,
                    this.iouVenda?.venda?.data,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(IOUSchemaV1)
}
