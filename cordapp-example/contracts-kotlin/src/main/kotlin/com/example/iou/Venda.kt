package com.example.iou


import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@CordaSerializable
data class Venda(
        val quantidadeMedVendida: Int,
        val comprador: String,
        val enderecoComprador: String,
        val rg: Int,
        val telefone: Int,
        val nomeVendedor: String,
        val cnpj: Int,
        val data: LocalDateTime = LocalDateTime.now()
)