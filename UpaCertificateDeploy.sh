#!/usr/bin/env bash
# ------------------------------------------------------------------
# [T_27]    Upa deploy certificates
#           Generate key pair and public key digital certificate using java keytool
# ------------------------------------------------------------------

# --- Variables ----------------------------------------------------
# alias
ca_alias=UpaCA
ca_cli_alias=UpaCAClient
broker_alias=UpaBroker
t1_alias=UpaTransporter1
t2_alias=UpaTransporter2

# keystore
ca_jks=${ca_alias}.jks
ca_cli_jks=${ca_cli_alias}.jks
broker_jks=${broker_alias}.jks
t1_jks=${t1_alias}.jks
t2_jks=${t2_alias}.jks

# keystore password
ca_pass=pass${ca_alias}
ca_cli_pass=pass${ca_cli_alias}
broker_pass=pass${broker_alias}
t1_pass=pass${t1_alias}
t2_pass=pass${t2_alias}

# certificates request
ca_cli_csr=${ca_cli_alias}.csr
broker_csr=${broker_alias}.csr
t1_csr=${t1_alias}.csr
t2_csr=${t2_alias}.csr

# certificates
ca_cer=${ca_alias}.cer
ca_cli_cer=${ca_cli_alias}.cer
broker_cer=${broker_alias}.cer
t1_cer=${t1_alias}.cer
t2_cer=${t2_alias}.cer

# --- UpaCA (Trust Root)--------------------------------------------
# create key pair and new keystore
keytool -genkeypair \
        -alias ${ca_alias} \
        -keyalg RSA -keysize 2048 \
        -keypass ${ca_pass} -validity 90 -storepass ${ca_pass} \
        -keystore ${ca_jks} -ext bc=ca:true \
        -dname "CN=$ca_alias, OU=T_27, O=IST, L=Lisbon, S=Lisbon, C=PT"
# create self sign certificate
keytool -export \
        -keystore ${ca_jks} \
        -alias ${ca_alias} \
        -storepass ${ca_pass} \
        -file ${ca_cer}

# --- UpaCAClient (just for tests)--------------------------------------------
# create key pair and new keystore
keytool -genkeypair \
        -alias ${ca_cli_alias} \
        -keyalg RSA -keysize 2048 \
        -keypass ${ca_cli_pass} -validity 90 -storepass ${ca_cli_pass} \
        -keystore ${ca_cli_jks} \
        -dname "CN=$ca_cli_alias, OU=T_27, O=IST, L=Lisbon, S=Lisbon, C=PT"
# create certificate request
keytool -certreq \
        -keystore ${ca_cli_jks} \
        -storepass ${ca_cli_pass} \
        -alias ${ca_cli_alias} \
        -file ${ca_cli_csr}
# import certificate to keyStore
keytool -importcert \
        -keystore ${ca_cli_jks} -storepass ${ca_cli_pass} \
        -file ${ca_cer} \
        -alias ${ca_alias} \
        -noprompt

# --- UpaBroker ------------------------------------------------------
# create key pair and new keystore
keytool -genkeypair \
        -alias ${broker_alias} \
        -keyalg RSA -keysize 2048 \
        -keypass ${broker_pass} -validity 90 -storepass ${broker_pass} \
        -keystore ${broker_jks} \
        -dname "CN=$broker_alias, OU=T_27, O=IST, L=Lisbon, S=Lisbon, C=PT"
# create certificate request
keytool -certreq \
        -keystore ${broker_jks} \
        -storepass ${broker_pass} \
        -alias ${broker_alias} \
        -file ${broker_csr}
# import certificate to keyStore
keytool -importcert \
        -keystore ${broker_jks} -storepass ${broker_pass} \
        -file ${ca_cer} \
        -alias ${ca_alias} \
        -noprompt

# --- UpaTransporter1 ------------------------------------------------
# create key pair and new keystore
keytool -genkeypair \
        -alias ${t1_alias} \
        -keyalg RSA -keysize 2048 \
        -keypass ${t1_pass} -validity 90 -storepass ${t1_pass} \
        -keystore ${t1_jks} \
        -dname "CN=$t1_alias, OU=T_27, O=IST, L=Lisbon, S=Lisbon, C=PT"
# create certificate request
keytool -certreq \
        -keystore ${t1_jks} \
        -storepass ${t1_pass} \
        -alias ${t1_alias} \
        -file ${t1_csr}
# import certificate to keyStore
keytool -importcert \
        -keystore ${t1_jks} -storepass ${t1_pass} \
        -file ${ca_cer} \
        -alias ${ca_alias} \
        -noprompt

# --- UpaTransporter2 ------------------------------------------------
# create key pair and new keystore
keytool -genkeypair \
        -alias ${t2_alias} \
        -keyalg RSA -keysize 2048 \
        -keypass ${t2_pass} -validity 90 -storepass ${t2_pass} \
        -keystore ${t2_jks} \
        -dname "CN=$t2_alias, OU=T_27, O=IST, L=Lisbon, S=Lisbon, C=PT"
# create certificate request
keytool -certreq \
        -keystore ${t2_jks} \
        -storepass ${t2_pass} \
        -alias ${t2_alias} \
        -file ${t2_csr}
# import certificate to keyStore
keytool -importcert \
        -keystore ${t2_jks} -storepass ${t2_pass} \
        -file ${ca_cer} \
        -alias ${ca_alias} \
        -noprompt

# --- Sign certificates ----------------------------------------------
# create CAClient certificate sign by UpaCA
keytool -gencert \
        -keystore ${ca_jks} \
        -storepass ${ca_pass} \
        -alias ${ca_alias} \
        -infile ${ca_cli_csr} \
        -outfile ${ca_cli_cer}
# create UpaBroker certificate sign by UpaCA
keytool -gencert \
        -keystore ${ca_jks} \
        -storepass ${ca_pass} \
        -alias ${ca_alias} \
        -infile ${broker_csr} \
        -outfile ${broker_cer}
# create UpaTransporter1 certificate sign by UpaCA
keytool -gencert \
        -keystore ${ca_jks} \
        -storepass ${ca_pass} \
        -alias ${ca_alias} \
        -infile ${t1_csr} \
        -outfile $t1_cer
# create UpaTransporter2 certificate sign by UpaCA
keytool -gencert \
        -keystore ${ca_jks} \
        -storepass ${ca_pass} \
        -alias ${ca_alias} \
        -infile ${t2_csr} \
        -outfile ${t2_cer}

# --- import Certificate to UpaCA KeyStore ---------------------------
# CAClient
keytool -importcert \
        -keystore ${ca_jks} -storepass ${ca_pass} \
        -file ${ca_cli_cer} \
        -alias ${ca_cli_alias}
# Broker
keytool -importcert \
        -keystore ${ca_jks} -storepass ${ca_pass} \
        -file ${broker_cer} \
        -alias ${broker_alias}
# Transporter1
keytool -importcert \
        -keystore ${ca_jks} -storepass ${ca_pass} \
        -file ${t1_cer} \
        -alias ${t1_alias}
# Transporter2
keytool -importcert \
        -keystore ${ca_jks} -storepass ${ca_pass} \
        -file ${t2_cer} \
        -alias ${t2_alias}

# --- Deploy Certificates --------------------------------------------
caFolder=./ca-ws/src/main/resources/
caClientFolder=./ca-ws-cli/src/test/resources/
brokerFolder=./broker-ws/src/main/resources/
transporterFolder=./transporter-ws/src/main/resources/

# Deploy CAClient
mv -f ${ca_cli_jks} ${caClientFolder}/${ca_cli_jks}
mv -f ${ca_cli_cer} ${caClientFolder}/${ca_cli_cer}
cp -f ${ca_cer} ${transporterFolder}/${ca_cer}

# Deploy Broker
mv -f ${broker_jks} ${brokerFolder}/${broker_jks}
cp -f ${ca_cer} ${brokerFolder}/${ca_cer}

# Deploy Transporters
mv -f ${t1_jks} ${transporterFolder}/${t1_jks}
mv -f ${t2_jks} ${transporterFolder}/${t2_jks}
cp -f ${ca_cer} ${transporterFolder}/${ca_cer}

# Deploy CA
mv -f ${ca_jks} ${caFolder}/${ca_jks}
mv -f ${ca_cer} ${caFolder}/${ca_cer}
mv -f ${broker_cer} ${caFolder}/${broker_cer}
mv -f ${t1_cer} ${caFolder}/${t1_cer}
mv -f ${t2_cer} ${caFolder}/${t2_cer}

# --- Remove leftovers -----------------------------------------------
rm -rf *.csr
