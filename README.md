# Projeto de Sistemas Distribuídos 2015-2016 #

Grupo de SD 27 - Campus Tagus Parque

77928 Frederico Teixeira frederico.teixeira@tecnico.ulisboa.pt

78015 David Silva d.silva44@hotmail.com

78052 Miguel Carvalho migueloak59@hotmail.com

Repositório:
[tecnico-distsys/T_27-project](https://github.com/tecnico-distsys/T_27-project/)

-------------------------------------------------------------------------------

## Instruções de instalação 


### Ambiente

[0] Iniciar sistema operativo

```
Linux
```

[1] Iniciar servidores de apoio

Generate keys: 
```
./gen_keys.sh

```

[2] Iniciar servidores de apoio

JUDDI: 
```
cd juddi-3.3.2_tomcat-7.0.64_9090/bin
        ./startup.sh

```


[3] Criar pasta temporária

```
mkdir proj
cd proj
```


[4] Obter código fonte do projeto (versão entregue)

```
git clone -b SD_R2 https://github.com/tecnico-distsys/T_27-project.git

```


[5] Instalar módulos de bibliotecas auxiliares

[Obter o código da biblioteca UDDINaming](http://disciplinas.tecnico.ulisboa.pt/leic-sod/2015-2016/labs/05-ws1/uddi-naming.zip)
```
cd uddi-naming
mvn clean install
```
```
cd ws-handlers
mvn clean install
```

-------------------------------------------------------------------------------

### Serviço CA

[1] Construir e executar **servidor**

```
cd ca-ws
mvn clean install exec:java

```

[2] Construir **cliente** e executar testes

```
cd ca-ws-cli
mvn clean install
```

-------------------------------------------------------------------------------

### Serviço TRANSPORTER

[1] Construir e executar **servidor**

```
cd transporter-ws
mvn clean install -Dws.i=1 exec:java
mvn -Dws.i=2 exec:java
```

[2] Construir **cliente** e executar testes

```
cd transporter-ws-cli
mvn clean install
```

-------------------------------------------------------------------------------

### Serviço BROKER-Primario e BROKER-Backup

[1] Construir e executar **servidores** 

```
cd broker-ws
mvn clean install -Dws.i=1 exec:java
mvn -Dws.i=2 exec:java

```

-------------------------------------------------------------------------------

### Testes de integração

```
cd broker-ws-cli
mvn clean verify
```


**FIM**
