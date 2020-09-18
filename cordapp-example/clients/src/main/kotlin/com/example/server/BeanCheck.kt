package com.example.server

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class BeanCheck(
        @JsonProperty("linearId") var linearId: UniqueIdentifier
)
