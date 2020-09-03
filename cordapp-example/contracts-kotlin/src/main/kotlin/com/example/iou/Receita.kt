package com.example.iou

import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class Receita(
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
        val rg: Number? = null,
        val telefone: Number? = null,
        val nomeVendedor: String?= null,
        val cnpj: Number? = null,
        val data: String? = null
)