package com.example.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for IOUState.
 */
object IOUSchema

/**
 * An IOUState schema.
 */
object IOUSchemaV1 : MappedSchema(
        schemaFamily = IOUSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentIOU::class.java)) {
    @Entity
    @Table(name = "iou_states")
    class PersistentIOU(
            @Column(name = "dataEmissao") //Data da emissão da receita
            var dataEmissao: String,

            @Column(name = "numeroReceita") //Número de indentificação da receita
            var numeroReceita: Int,

            @Column(name = "nomePaciente") //Nome do paciente
            var nomePaciente: String,

            @Column(name = "enderecoPaciente") //Endereço do paciente
            var enderecoPaciente: String,

            @Column(name = "nomeMedico") //Nome do médico
            var nomeMedico: String,

            @Column(name = "crmMedico") //CRM do médico
            var crmMedico: Int,

            @Column(name = "nomeMedicamento") //Nome do medicamento
            var nomeMedicamento: String,

            @Column(name = "quantidadeMedicamento") //Quantidade do medicamento
            var quantidadeMedicamento: Int,

            @Column(name = "formulaMedicamento") //Fórmula do medicamento
            var formulaMedicamento: String,

            @Column(name = "doseUnidade") //Dose por unidade do medicamento
            var doseUnidade: String,

            @Column(name = "posologia") //Dosagem do medicamenento
            var posologia: Int,

            @Column(name = "comprador")
            var comprador: String? = null,

            @Column(name = "endereco_comprador")
            var endereco: String? = null,

            @Column(name = "rg")
            var rg: Int? = null,

            @Column(name = "telefone")
            var telefone: Int? = null,

            @Column(name = "nome_vendedor")
            var nome_vendedor: String? = null,

            @Column(name = "cnpj")
            var cnpj: Int? = null,

            @Column(name = "data")
            var data: String? = null,

            @Column(name = "linear_id")
            var linearId: UUID


    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor() : this(
                "",
                0,
                "",
                "",
                "",
                0,
                "",
                0,
                "",
                "",
                0,
                "",
                "",
                0,
                0,
                "",
                0,
                "",
                UUID.randomUUID())
    }
}