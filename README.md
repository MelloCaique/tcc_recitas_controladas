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

### Campos da receita B2 e seus tipos
-dataEmissao: String
-numeroReceita: Int
-nomePaciente: String
-enderecoPaciente: String
-nomeMedico: String
-crmMedico: Int
-nomeMedicamento: String
-quantidadeMedicamento: Int
-formulaMedicamento: String
-doseUnidade: String
-posologia: Int
-comprador: String
-enderecoComprador: String
-rg: Int
-telefone: Int
-nomeVendedor: String
-cnpj: Int
-data: String

### Teste de Request na API:
```
curl -i -X POST 'http://localhost:50005/api/example/create-iou?procedimento=1&valorProcedimento=1&dataOcorrencia=13&nome=1&cpf=1' -H 'Content-Type: application/x-www-form-urlencoded'
```

### Teste do fluxo de registro da receita
```
flow start com.example.flow.ExampleFlow$Initiator receita:{dataEmissao: "diadia", numeroReceita: 1, nomePaciente: "caique", enderecoPaciente: "rua", nomeMedico: "Dr", crmMedico: 1, nomeMedicamento: "oie", quantidadeMedicamento: 1, formulaMedicamento: "ml", doseUnidade: "1", posologia: 1}
```

### Teste do fluxo de venda do medicamento
```
flow start com.example.flow.ExampleFlowUpdate$Initiator linearId: "3882b07e-d3fb-4b6a-b7dd-19708090ec0c", vendaFarma:{comprador: "fernanda", enderecoComprador: "rua", rg: 1, telefone: 1, nomeVendedor: "eu", cnpj: 1, data: "hoje"}
```

### Teste de consulta de registro das ocorrências
```
run vaultQuery contractStateType: com.example.state.IOUState
```
