# Controle de Receitas

### Deploy Nodes

-Unix/Mac OSX: 
```
./gradlew deployNodes
```
-Windows:
```
gradlew.bat deployNodes
```

### Run Nodes

-Unix/Mac OSX: 
```
workflows-kotlin/build/nodes/runnodes
```
-Windows:
```
call workflows-kotlin\build\nodes\runnodes.bat
```

### Run Spring Boot Server

-Substituir X por: ConsultorioA, FarmaciaA, FarmaciaB.

-Unix/Mac OSX: 
```
./gradlew runXServer
```
-Windows:
```
gradlew.bat runXServer
```

### Campos da receita B2 e seus tipos

```
-dataEmissao: String

-numeroReceita: Int

-nomePaciente: String

-enderecoPaciente: String

-nomeMedico: String

-crmMedico: Int

-nomeMedicamento: String

-quantidadeMedicamento: Int

-formulaMedicamento: String

-doseUnidade: Int

-posologia: String

-comprador: String

-enderecoComprador: String

-rg: Int

-telefone: Int

-nomeVendedor: String

-cnpj: Int

-data: String
```

### Teste do fluxo de registro da receita
```
flow start com.example.flow.FlowCreate$Initiator receita:{numeroReceita: 2, nomePaciente: "caique", enderecoPaciente: "rua", nomeMedico: "Dr", crmMedico: 1, nomeMedicamento: "teste" , quantidadeMedicamento: 1, formulaMedicamento: "ml", doseUnidade: "1", posologia: "1"}
```

### Teste do fluxo de venda do medicamento
```
flow start com.example.flow.FlowUpdate$InitiatorUpdate linearId: "3bc1e864-4628-4b0f-a938-15650c990cf7", vendaFarma:{comprador: "fernanda", enderecoComprador: "rua", rg: 1, telefone: 1, nomeVendedor: "eu", cnpj: 1}
```

### Teste de consulta de registro das ocorrências
```
run vaultQuery contractStateType: com.example.state.IOUState