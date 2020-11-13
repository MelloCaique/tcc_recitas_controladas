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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

val SERVICE_NAMES = listOf("Notary", "Network Map Service")

/**
 *  A Spring Boot Server API controller for interacting with the node via RPC.
 */
@CrossOrigin
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
    @CrossOrigin
    @GetMapping(value = [ "me" ], produces = [ APPLICATION_JSON_VALUE ])
    fun whoami() = mapOf("me" to myLegalName)

    /**
     * Returns all parties registered with the network map service. These names can be used to look up identities using
     * the identity service.
     */
    @CrossOrigin
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
    @CrossOrigin
    @GetMapping(value = [ "receitas" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getIOUs() : ResponseEntity<List<StateAndRef<IOUState>>> {
        return ResponseEntity.ok(proxy.vaultQueryBy<IOUState>().states)
    }
    @CrossOrigin
    @PostMapping(value = [ "check-receita" ], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun CheckLinearId(@RequestBody request: BeanCheck): ResponseEntity<String> {
        val obligations = proxy.vaultQueryBy<IOUState>(QueryCriteria.LinearStateQueryCriteria(
                null,
                ImmutableList.of(request.linearId),
                Vault.StateStatus.UNCONSUMED,
                null)).states
        if(obligations.isNotEmpty()){
            if(obligations[0].state.data.linearId == request.linearId && ((obligations[0].state.data.iouVenda == null
                    && obligations[0].state.data.totalMedicamentoVendido == null) || ((obligations[0].state.data.iouVenda != null
                            && obligations[0].state.data.totalMedicamentoVendido != null)
                            && obligations[0].state.data.totalMedicamentoVendido!! < obligations[0].state.data.iouReceita.receita.quantidadeMedicamento))){
                val firstIntervalDate = LocalDate.of(
                        obligations[0].state.data.dataEmissao.year,
                        obligations[0].state.data.dataEmissao.month,
                        obligations[0].state.data.dataEmissao.dayOfMonth).atStartOfDay()
                val secondIntervalDate = firstIntervalDate.with(TemporalAdjusters.lastDayOfMonth()).plusDays(30)
                if(LocalDateTime.now() <= secondIntervalDate){
                    return ResponseEntity.ok("Receita está disponível para venda."+
                            "Quantidade disponível para venda: ${obligations[0].state.data.iouReceita.receita.quantidadeMedicamento - 
                                    (obligations[0].state.data.totalMedicamentoVendido ?: 0)}")
                }else{
                    return ResponseEntity.badRequest().body("Erro: Receita não está disponível para venda. " +
                            "Código da Validade da receita expirada")
                }
            }
            else{
                return ResponseEntity.badRequest().body("Erro: Receita não está disponível para venda. " +
                        "Receita já foi vendida completamente")
            }
        }else{
            return ResponseEntity.badRequest().body("Erro: Receita não está disponível para venda. " +
                    "Código da receita inválido")
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
    @CrossOrigin
    @PostMapping(value = [ "create-receita" ], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createIOU(@RequestBody request: BeanReceita): ResponseEntity<String> {

        return try {
            val signedTx = proxy.startTrackedFlow(::Initiator, Receita(
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
            val size = proxy.vaultQueryBy<IOUState>().states.size
            val codTracking = proxy.vaultQueryBy<IOUState>().states.get(size-1).state.data.linearId
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n" +
                    "${codTracking}")
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @CrossOrigin
    @PostMapping(value = [ "venda-receita" ], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun UpdateIOU(@RequestBody request: BeanVenda): ResponseEntity<String> {


        return try {
            val signedTx = proxy.startTrackedFlow(::InitiatorUpdate,
                    UniqueIdentifier(
                            request.linearId.externalId,
                            request.linearId.id
                            ),
                    Venda(
                            request.quantidadeMedVendida,
                            request.comprador,
                            request.enderecoComprador,
                            request.rg,
                            request.telefone,
                            request.nomeVendedor,
                            request.cnpj
                    )).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    /**
     * Displays all IOU states that only this node has been involved in.
     */
    @CrossOrigin
    @GetMapping(value = [ "my-receitas" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getMyIOUs(): ResponseEntity<List<StateAndRef<IOUState>>>  {
        val myious = proxy.vaultQueryBy<IOUState>().states.filter {
            it.state.data.allParticipants.contains(proxy.nodeInfo().legalIdentities.first().name.organisation) }
        return ResponseEntity.ok(myious)
    }

}
