package com.example.server

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class BeanVenda(
        @JsonProperty("linearId") var linearId: UniqueIdentifier,
        @JsonProperty("comprador") var comprador: String,
        @JsonProperty("enderecoComprador") var enderecoComprador: String,
        @JsonProperty("rg") var rg: Int,
        @JsonProperty("telefone") var telefone: Int,
        @JsonProperty("nomeVendedor") var nomeVendedor: String,
        @JsonProperty("cnpj") var cnpj: Int
)
