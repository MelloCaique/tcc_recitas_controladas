package com.example.iou


import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
data class Venda(
        val quantidadeMedVendida: Int,
        val comprador: String,
        val enderecoComprador: String,
        val rg: Long,
        val telefone: Long,
        val nomeVendedor: String,
        val cnpj: Long,
        val data: LocalDateTime = LocalDateTime.now()
)