package com.example.iou

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime
import javax.print.attribute.standard.DateTimeAtCreation

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