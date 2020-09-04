package com.example.iou

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class ReceitaIOU (val receita: Receita)