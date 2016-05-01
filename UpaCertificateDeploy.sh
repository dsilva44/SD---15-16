#!/usr/bin/env bash
# ------------------------------------------------------------------
# [T_27]    Upa deploy certificates
#           Generate key pair and public key digital certificate using java keytool
# ------------------------------------------------------------------

# --- Variables ----------------------------------------------------
# alias
ca_alias=UpaCA
broker_alias=UpaBroker
t1_alias=UpaTransporter1
t2_alias=UpaTransporter2

# keystore
ca_jks=UpaCA.jks
broker_jks=UpaBroker.jks
t1_jks=UpaTransporter1.jks
t2_jks=UpaTransporter2.jks

# keystore password
ca_pass=passUpaCA
broker_pass=passUpaBroker
t1_pass=passUpaTransporter1
t2_pass=passUpaTransporter2

# certificates request
broker_csr=UpaBroker.csr
t1_csr=UpaTransporter1.csr
t2_csr=UpaTransporter2.csr

# certificates
ca_cer=UpaCA.cer
broker_cer=UpaBroker.cer
t1_cer=UpaTransporter1.cer
t2_cer=UpaTransporter2.cer

# --- UpaCA (Trust Root)--------------------------------------------
# create key pair and new keystore
keytool -genkeypair \
        -alias ${ca_alias} \
        -keyalg RSA -keysize 2048 \
        -keypass ${ca_pass} -validity 90 -storepass ${ca_pass} \
        -keystore ${ca_jks} -ext bc=ca:true \
        -dname "CN=UpaCA, OU=T_27, O=IST, L=Lisbon, S=Lisbon, C=PT"
# create self sign certificate
keytool -export \
        -keystore ${ca_jks} \
        -alias ${ca_alias} \
        -storepass ${ca_pass} \
        -file ${ca_cer}

# --- UpaBroker ------------------------------------------------------
# create key pair and new keystore
keytool -genkeypair \
        -alias ${broker_alias} \
        -keyalg RSA -keysize 2048 \
        -keypass ${broker_pass} -validity 90 -storepass ${broker_pass} \
        -keystore ${broker_jks} \
        -dname "CN=UpaBroker, OU=T_27, O=IST, L=Lisbon, S=Lisbon, C=PT"
# create certificate request
keytool -certreq \
        -keystore ${broker_jks} \
        -storepass ${broker_pass} \
        -alias ${broker_alias} \
        -file ${broker_csr}

# --- UpaTransporter1 ------------------------------------------------
# create key pair and new keystore
keytool -genkeypair \
        -alias ${t1_alias} \
        -keyalg RSA -keysize 2048 \
        -keypass ${t1_pass} -validity 90 -storepass ${t1_pass} \
        -keystore ${t1_jks} \
        -dname "CN=UpaTransporter1, OU=T_27, O=IST, L=Lisbon, S=Lisbon, C=PT"
# create certificate request
keytool -certreq \
        -keystore ${t1_jks} \
        -storepass ${t1_pass} \
        -alias ${t1_alias} \
        -file ${t1_csr}

# --- UpaTransporter2 ------------------------------------------------
# create key pair and new keystore
keytool -genkeypair \
        -alias ${t2_alias} \
        -keyalg RSA -keysize 2048 \
        -keypass ${t2_pass} -validity 90 -storepass ${t2_pass} \
        -keystore ${t2_jks} \
        -dname "CN=UpaTransporter2, OU=T_27, O=IST, L=Lisbon, S=Lisbon, C=PT"
# create certificate request
keytool -certreq \
        -keystore ${t2_jks} \
        -storepass ${t2_pass} \
        -alias ${t2_alias} \
        -file ${t2_csr}

# --- Sign certificates ----------------------------------------------
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

# --- Deploy Certificates --------------------------------------------
caFolder=./ca-ws/src/main/resources/
brokerFolder=./broker-ws/src/main/resources/
transporterFolder=./transporter-ws/src/main/resources/

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
