package com.example.iou

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Receita(
        @JsonProperty("data_emissao")
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
        val comprador: String? = null,
        val enderecoComprador: String? = null,
        val rg: Int? = null,
        val telefone: Int? = null,
        val nomeVendedor: String?= null,
        val cnpj: Int? = null,
        val data: String? = null
)