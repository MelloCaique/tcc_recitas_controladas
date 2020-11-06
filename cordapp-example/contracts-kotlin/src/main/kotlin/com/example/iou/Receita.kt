package com.example.iou


import net.corda.core.serialization.CordaSerializable


@CordaSerializable
data class Receita(
        val numeroReceita: Long,
        val nomePaciente: String,
        val enderecoPaciente: String,
        val nomeMedico: String,
        val crmMedico: Long,
        val nomeMedicamento: String,
        val quantidadeMedicamento: Int,
        val formulaMedicamento: String,
        val doseUnidade: Int,
        val posologia: String
)