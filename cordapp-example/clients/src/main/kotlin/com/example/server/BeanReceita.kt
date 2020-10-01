package com.example.server

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.serialization.CordaSerializable


@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class BeanReceita(
        @JsonProperty("numeroReceita") var numeroReceita: Int,
        @JsonProperty("nomePaciente") var nomePaciente: String,
        @JsonProperty("enderecoPaciente") var enderecoPaciente: String,
        @JsonProperty("nomeMedico") var nomeMedico: String,
        @JsonProperty("crmMedico") var crmMedico: Int,
        @JsonProperty("nomeMedicamento") var nomeMedicamento: String,
        @JsonProperty("quantidadeMedicamento") var quantidadeMedicamento: Int,
        @JsonProperty("formulaMedicamento") var formulaMedicamento: String,
        @JsonProperty("doseUnidade") var doseUnidade: Int,
        @JsonProperty("posologia") var posologia: String
)