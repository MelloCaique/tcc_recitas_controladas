package com.example.server

import com.example.flow.FlowCreate.Initiator
import com.example.flow.FlowUpdate.InitiatorUpdate
import com.example.iou.Receita
import com.example.iou.Venda
import com.example.state.IOUState
import com.google.common.collect.ImmutableList
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.MediaType.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpServletRequest

val SERVICE_NAMES = listOf("Notary", "Network Map Service")

/**
 *  A Spring Boot Server API controller for interacting with the node via RPC.
 */

@RestController
@RequestMapping("/api/example/") // The paths for GET and POST requests are relative to this base path.
class MainController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val myLegalName = rpc.proxy.nodeInfo().legalIdentities.first().name
    private val proxy = rpc.proxy

    /**
     * Returns the node's name.
     */
    @GetMapping(value = [ "me" ], produces = [ APPLICATION_JSON_VALUE ])
    fun whoami() = mapOf("me" to myLegalName)

    /**
     * Returns all parties registered with the network map service. These names can be used to look up identities using
     * the identity service.
     */
    @GetMapping(value = [ "peers" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = proxy.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation) })
    }

    /**
     * Displays all IOU states that exist in the node's vault.
     */
    @GetMapping(value = [ "ious" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getIOUs() : ResponseEntity<List<StateAndRef<IOUState>>> {
        return ResponseEntity.ok(proxy.vaultQueryBy<IOUState>().states)
    }

    @PostMapping(value = [ "linearid-check" ], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun CheckLinearId(@RequestBody request: BeanCheck): ResponseEntity<String> {
        val obligations = proxy.vaultQueryBy<IOUState>(QueryCriteria.LinearStateQueryCriteria(
                null,
                ImmutableList.of(request.linearId),
                Vault.StateStatus.UNCONSUMED,
                null)).states
        if(obligations.isNotEmpty() && obligations[0].state.data.linearId == request.linearId && obligations[0].state.data.iouVenda == null ){
            return ResponseEntity.ok("Receita is APPROVED to selling")
        }else{
            return ResponseEntity.badRequest().body("Receita is NOT APPROVED to selling")
        }
    }

    /**
     * Initiates a flow to agree an IOU between two parties.
     *
     * Once the flow finishes it will have written the IOU to ledger. Both the lender and the borrower will be able to
     * see it when calling /spring/api/ious on their respective nodes.
     *
     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
     * in its network map cache, it will return an HTTP bad request.
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */

    @PostMapping(value = [ "create-receita" ], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createIOU(@RequestBody request: BeanReceita): ResponseEntity<String> {

        if(request.dataEmissao == null ) {
            return ResponseEntity.badRequest().body("Query parameter 'dataEmissao' must not be null.\n")
        }
        if(request.numeroReceita <= 0 ) {
            return ResponseEntity.badRequest().body("Query parameter 'numeroReceita' must be non-negative.\n")
        }
        if(request.nomePaciente == null){
            return ResponseEntity.badRequest().body("Query parameter 'nomePaciente' must not be null.\n")
        }
        if(request.enderecoPaciente == null){
            return ResponseEntity.badRequest().body("Query parameter 'enderecoPaciente' must not be null.\n")
        }
        if(request.nomeMedico == null ){
            return ResponseEntity.badRequest().body("Query parameter 'nomeMedico' must not be null.\n")
        }
        if(request.crmMedico <= 0 ){
            return ResponseEntity.badRequest().body("Query parameter 'crmMedico' must be non-negative.\n")
        }
        if(request.nomeMedicamento == null ){
            return ResponseEntity.badRequest().body("Query parameter 'nomeMedicamento' must not be null.\n")
        }
        if(request.quantidadeMedicamento <= 0 ){
            return ResponseEntity.badRequest().body("Query parameter 'quantidadeMedicamento' must be non-negative.\n")
        }
        if(request.formulaMedicamento == null ){
            return ResponseEntity.badRequest().body("Query parameter 'formulaMedicamento' must not be null.\n")
        }
        if(request.doseUnidade == null ){
            return ResponseEntity.badRequest().body("Query parameter 'doseUnidade' must not be null.\n")
        }
        if(request.posologia <= 0 ){
            return ResponseEntity.badRequest().body("Query parameter 'doseUnidade' must be non-negative.\n")
        }

        return try {
            val signedTx = proxy.startTrackedFlow(::Initiator, Receita(
                    request.dataEmissao,
                    request.numeroReceita,
                    request.nomePaciente,
                    request.enderecoPaciente,
                    request.nomeMedico,
                    request.crmMedico,
                    request.nomeMedicamento,
                    request.quantidadeMedicamento,
                    request.formulaMedicamento,
                    request.doseUnidade,
                    request.posologia)).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(value = [ "updade-receita" ], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun UpdateIOU(@RequestBody request: BeanVenda): ResponseEntity<String> {

        var newLinear = request.linearId as UniqueIdentifier

        if(request.linearId == null ) {
            return ResponseEntity.badRequest().body("Query parameter 'linearId' must not be null.\n")
        }
        if(request.comprador == null ) {
            return ResponseEntity.badRequest().body("Query parameter 'linearId' must not be null.\n")
        }
        if(request.enderecoComprador == null ) {
            return ResponseEntity.badRequest().body("Query parameter 'linearId' must not be null.\n")
        }
        if(request.rg <= 0) {
            return ResponseEntity.badRequest().body("Query parameter 'linearId' must not be null.\n")
        }
        if(request.telefone <= 0) {
            return ResponseEntity.badRequest().body("Query parameter 'linearId' must not be null.\n")
        }
        if(request.nomeVendedor == null ) {
            return ResponseEntity.badRequest().body("Query parameter 'linearId' must not be null.\n")
        }
        if(request.cnpj <= 0) {
            return ResponseEntity.badRequest().body("Query parameter 'linearId' must not be null.\n")
        }
        if(request.data == null ) {
            return ResponseEntity.badRequest().body("Query parameter 'linearId' must not be null.\n")
        }

        return try {
            val signedTx = proxy.startTrackedFlow(::InitiatorUpdate,
                    UniqueIdentifier(
                            request.linearId.externalId,
                            request.linearId.id
                            ),
                    Venda(
                            request.comprador,
                            request.enderecoComprador,
                            request.rg,
                            request.telefone,
                            request.nomeVendedor,
                            request.cnpj,
                            request.data)).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    /**
     * Displays all IOU states that only this node has been involved in.
     */
    @GetMapping(value = [ "my-ious" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getMyIOUs(): ResponseEntity<List<StateAndRef<IOUState>>>  {
        val myious = proxy.vaultQueryBy<IOUState>().states.filter { it.state.data.remetente.equals(proxy.nodeInfo().legalIdentities.first()) }
        return ResponseEntity.ok(myious)
    }

}
