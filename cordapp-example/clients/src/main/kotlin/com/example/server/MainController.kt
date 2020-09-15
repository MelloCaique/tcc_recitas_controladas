package com.example.server

import com.example.flow.FlowCreate.Initiator
import com.example.iou.Receita
import com.example.state.IOUState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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

    @PostMapping(value = [ "create-iou" ], produces = [ TEXT_PLAIN_VALUE ], headers = [ "Content-Type=application/x-www-form-urlencoded" ])
    fun createIOU(request: HttpServletRequest): ResponseEntity<String> {
        val dataEmissao = request.getParameter("dataEmissao")
        val numeroReceita = request.getParameter("numeroReceita").toInt()
        val nomePaciente = request.getParameter("nomePaciente")
        val enderecoPaciente = request.getParameter("enderecoPaciente")
        val nomeMedico = request.getParameter("nomeMedico")
        val crmMedico = request.getParameter("crmMedico").toInt()
        val nomeMedicamento = request.getParameter("nomeMedicamento")
        val quantidadeMedicamento = request.getParameter("quantidadeMedicamento").toInt()
        val formulaMedicamento = request.getParameter("formulaMedicamento")
        val doseUnidade = request.getParameter("doseUnidade")
        val posologia = request.getParameter("posologia").toInt()
        //val comprador: String? = null,
        //val enderecoComprador: String? = null,
        //val rg: Int? = null,
        //val telefone: Int? = null,
        //val nomeVendedor: String?= null,
        //val cnpj: Int? = null,
        //val data: String? = null

        if(dataEmissao == null ) {
            return ResponseEntity.badRequest().body("Query parameter 'dataEmissao' must not be null.\n")
        }
        if(numeroReceita <= 0 ) {
            return ResponseEntity.badRequest().body("Query parameter 'numeroReceita' must be non-negative.\n")
        }
        if(nomePaciente == null){
            return ResponseEntity.badRequest().body("Query parameter 'nomePaciente' must not be null.\n")
        }
        if(enderecoPaciente == null){
            return ResponseEntity.badRequest().body("Query parameter 'enderecoPaciente' must not be null.\n")
        }
        if(nomeMedico == null ){
            return ResponseEntity.badRequest().body("Query parameter 'nomeMedico' must not be null.\n")
        }
        if(crmMedico <= 0 ){
            return ResponseEntity.badRequest().body("Query parameter 'crmMedico' must be non-negative.\n")
        }
        if(nomeMedicamento == null ){
            return ResponseEntity.badRequest().body("Query parameter 'nomeMedicamento' must not be null.\n")
        }
        if(quantidadeMedicamento <= 0 ){
            return ResponseEntity.badRequest().body("Query parameter 'quantidadeMedicamento' must be non-negative.\n")
        }
        if(formulaMedicamento == null ){
            return ResponseEntity.badRequest().body("Query parameter 'formulaMedicamento' must not be null.\n")
        }
        if(doseUnidade == null ){
            return ResponseEntity.badRequest().body("Query parameter 'doseUnidade' must not be null.\n")
        }
        if(posologia <= 0 ){
            return ResponseEntity.badRequest().body("Query parameter 'doseUnidade' must be non-negative.\n")
        }

        return try {
            val signedTx = proxy.startTrackedFlow(::Initiator, Receita(dataEmissao, numeroReceita, nomePaciente, enderecoPaciente, nomeMedico, crmMedico, nomeMedicamento, quantidadeMedicamento, formulaMedicamento, doseUnidade, posologia)).returnValue.getOrThrow()
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
