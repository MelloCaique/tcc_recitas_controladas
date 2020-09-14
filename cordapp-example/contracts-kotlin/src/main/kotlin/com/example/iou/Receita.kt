package com.example.iou

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
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
        val posologia: Int
)