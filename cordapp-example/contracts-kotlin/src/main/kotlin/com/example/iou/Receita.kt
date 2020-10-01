package com.example.iou


import net.corda.core.serialization.CordaSerializable


@CordaSerializable
data class Receita(
        val numeroReceita: Int,
        val nomePaciente: String,
        val enderecoPaciente: String,
        val nomeMedico: String,
        val crmMedico: Int,
        val nomeMedicamento: String,
        val quantidadeMedicamento: Int,
        val formulaMedicamento: String,
        val doseUnidade: Int,
        val posologia: String
)