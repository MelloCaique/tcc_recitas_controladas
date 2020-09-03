# seguradora-reembolso

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

### Teste do fluxo de registro da ocorrência
```
flow start ExampleFlow$Initiator procedimento: 1, valorProcedimento: 1, dataOcorrencia: "1", nome: "1", cpf: 1
```

### Teste de consulta de registro das ocorrências
```
run vaultQuery contractStateType: com.example.state.IOUState
```