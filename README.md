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

JUDDI: 
```
cd juddi-3.3.2_tomcat-7.0.64_9090/bin
        ./startup.sh

```


[2] Criar pasta temporária

```
cd ...
mkdir ...
```


[3] Obter código fonte do projeto (versão entregue)

```
git clone ... 
```
*(colocar aqui comandos git para obter a versão entregue a partir da tag e depois apagar esta linha)*


[4] Instalar módulos de bibliotecas auxiliares

```
cd uddi-naming
mvn clean install
```



-------------------------------------------------------------------------------

### Serviço TRANSPORTER

[1] Construir e executar **servidor**

```
cd transporter-ws
mvn clean install
mvn exec:java
```

[2] Construir **cliente** e executar testes

```
cd transporter-ws-cli
mvn clean install
```

...


-------------------------------------------------------------------------------

### Serviço BROKER

[1] Construir e executar **servidor**

```
cd broker-ws
mvn clean install
mvn exec:java
```


[2] Construir **cliente** e executar testes

```
cd broker-ws-cli
mvn clean install
```

...

-------------------------------------------------------------------------------
**FIM**
