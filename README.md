# Controle de Receitas

### Campos e seus tipos
- procedimento: Int
- valorProcedimento: Int
- dataOcorrência: String
- nome: String
- cpf: Int

### Teste de Request na API:
```
curl -i -X POST 'http://localhost:50005/api/example/create-iou?procedimento=1&valorProcedimento=1&dataOcorrencia=13&nome=1&cpf=1' -H 'Content-Type: application/x-www-form-urlencoded'
```

### Teste do fluxo de registro da receita
```
flow start com.example.flow.ExampleFlow$Initiator receita:{dataEmissao: "diadia", numeroReceita: 1, nomePaciente: "caique", enderecoPaciente: "rua", nomeMedico: "Dr", crmMedico: 1, nomeMedicamento: "oie", quantidadeMedicamento: 1, formulaMedicamento: "ml", doseUnidade: "1", posologia: 1}
```

### Teste de consulta de registro das ocorrências
```
run vaultQuery contractStateType: com.example.state.IOUState
```
