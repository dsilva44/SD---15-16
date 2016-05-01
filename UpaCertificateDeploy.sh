#!/usr/bin/env bash
# ------------------------------------------------------------------
# [T_27]    Upa deploy certificates
#           Generate key pair and public key digital certificate using java keytool
# ------------------------------------------------------------------

VERSION=0.1.0

# --- UpaCA (Trust Root)--------------------------------------------
# create key pair and new keystore
keytool -genkeypair \
        -alias "UpaCA" \
        -keyalg RSA -keysize 2048 \
        -keypass "passUpaCA" -validity 90 -storepass "passUpaCA" \
        -keystore UpaCA.jks -ext bc=ca:true \
        -dname "CN=UpaCA, OU=T_27, O=IST, L=Lisbon, S=Lisbon, C=PT"
# create self sign certificate
keytool -export \
        -keystore UpaCA.jks \
        -alias "UpaCA" \
        -storepass "passUpaCA" \
        -file UpaCA.cer

# --- UpaBroker ------------------------------------------------------
# create key pair and new keystore
keytool -genkeypair \
        -alias "UpaBroker" \
        -keyalg RSA -keysize 2048 \
        -keypass "passUpaBroker" -validity 90 -storepass "passUpaBroker" \
        -keystore UpaBroker.jks \
        -dname "CN=UpaBroker, OU=T_27, O=IST, L=Lisbon, S=Lisbon, C=PT"
# create certificate request
keytool -certreq \
        -keystore UpaBroker.jks \
        -storepass "passUpaBroker" \
        -alias "UpaBroker" \
        -file UpaBroker.csr

# --- UpaTransporter1 ------------------------------------------------
# create key pair and new keystore
keytool -genkeypair \
        -alias "UpaTransporter1" \
        -keyalg RSA -keysize 2048 \
        -keypass "passUpaTransporter1" -validity 90 -storepass "passUpaTransporter1" \
        -keystore UpaTransporter1.jks \
        -dname "CN=UpaTransporter1, OU=T_27, O=IST, L=Lisbon, S=Lisbon, C=PT"
# create certificate request
keytool -certreq \
        -keystore UpaTransporter1.jks \
        -storepass passUpaTransporter1 \
        -alias UpaTransporter1 \
        -file UpaTransporter1.csr

# --- UpaTransporter2 ------------------------------------------------
# create key pair and new keystore
keytool -genkeypair \
        -alias UpaTransporter2 \
        -keyalg RSA -keysize 2048 \
        -keypass passUpaTransporter2 -validity 90 -storepass passUpaTransporter2 \
        -keystore UpaTransporter2.jks \
        -dname "CN=UpaTransporter2, OU=T_27, O=IST, L=Lisbon, S=Lisbon, C=PT"
# create certificate request
keytool -certreq \
        -keystore UpaTransporter2.jks \
        -storepass passUpaTransporter2 \
        -alias UpaTransporter2 \
        -file UpaTransporter2.csr

# --- Sign certificates ----------------------------------------------
# create UpaBroker certificate sign by UpaCA
keytool -gencert \
        -keystore UpaCA.jks \
        -storepass passUpaCA \
        -alias UpaCA \
        -infile UpaBroker.csr \
        -outfile UpaBroker.cer
# create UpaTransporter1 certificate sign by UpaCA
keytool -gencert \
        -keystore UpaCA.jks \
        -storepass passUpaCA \
        -alias UpaCA \
        -infile UpaTransporter1.csr \
        -outfile UpaTransporter1.cer
# create UpaTransporter2 certificate sign by UpaCA
keytool -gencert \
        -keystore UpaCA.jks \
        -storepass passUpaCA \
        -alias UpaCA \
        -infile UpaTransporter2.csr \
        -outfile UpaTransporter2.cer

# --- Deploy Certificates --------------------------------------------
