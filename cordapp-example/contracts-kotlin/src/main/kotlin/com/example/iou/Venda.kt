package com.example.iou

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
data class Venda(
        val comprador: String,
        val enderecoComprador: String,
        val rg: Int,
        val telefone: Int,
        val nomeVendedor: String,
        val cnpj: Int,
        val data: LocalDateTime = LocalDateTime.now()
)