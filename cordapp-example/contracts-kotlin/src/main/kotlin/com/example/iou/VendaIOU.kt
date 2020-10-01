package com.example.iou

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class VendaIOU (val venda: List<Venda>)