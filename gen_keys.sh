#!/usr/bin/env bash
# ------------------------------------------------------------------
# [T_27] Generate signed X509 certificates
#        Script to generate signed X509 certificates, key pair using java keyTool
#        Note: KeyStore and private key in the form pass<server_name>
# ------------------------------------------------------------------

# --- Init ---------------------------------------------------------
declare -A SERVERS # works only on bash-4.0
caClientFolder=./ca-ws-cli/src/test/resources/
brokerFolder=./broker-ws/src/main/resources/
transporterFolder=./transporter-ws/src/main/resources/

SERVERS=( ["UpaCAClient"]=${caClientFolder} ["UpaBroker"]=${brokerFolder} ["UpaTransporter1"]=${transporterFolder} ["UpaTransporter2"]=${transporterFolder})

caFolder=./ca-ws/src/main/resources/
# CA alias
ca_alias=UpaCA
# CA keyStore
ca_jks=${ca_alias}.jks
# CA password (keyStore pass = private key pass)
ca_pass=pass${ca_alias}
# CA certificate
ca_cer=${ca_alias}.cer

# --- Trust Root----------------------------------------------------
echo "Generating the $ca_alias keyStore and key pair..."
keytool -genkeypair -alias ${ca_alias} \
        -keyalg RSA -keysize 2048 \
        -keypass ${ca_pass} -validity 90 -storepass ${ca_pass} \
        -keystore ${ca_jks} -ext bc=ca:true \
        -dname "CN=$ca_alias, OU=T_27, O=IST, L=Lisbon, S=Lisbon, C=PT"
echo "$ca_alias keyStore and key pair generated."

echo "Generating the $ca_alias Self sign certificate..."
keytool -export \
        -keystore ${ca_jks} \
        -alias ${ca_alias} \
        -storepass ${ca_pass} \
        -file ${ca_cer}
echo "$ca_alias Self sign certificate generated."

# --- Sign Certificates---------------------------------------------

for server_name in ${!SERVERS[@]}
do
    echo "################################################################################"
    echo "Generating the $server_name keyStore and key pair..."
    keytool -genkeypair \
            -alias ${server_name} \
            -keyalg RSA -keysize 2048 \
            -keypass pass${server_name} -validity 90 -storepass pass${server_name} \
            -keystore ${server_name}.jks \
            -dname "CN=$server_name, OU=T_27, O=IST, L=Lisbon, S=Lisbon, C=PT"
    echo "$server_name keyStore and key pair generated."

    echo "Generating the $server_name certificate request..."
    keytool -certreq \
            -keystore ${server_name}.jks \
            -storepass pass${server_name} \
            -alias ${server_name} \
            -file ${server_name}.csr
    echo "$server_name certificate request generated."

    echo "Generating the $server_name sign certificate..."
    keytool -gencert \
            -keystore ${ca_jks} \
            -storepass ${ca_pass} \
            -alias ${ca_alias} \
            -infile ${server_name}.csr \
            -outfile ${server_name}.cer
    echo "$server_name sign certificate generated."

    echo "Import $ca_alias and $server_name certificates to $server_name keyStore..."
    keytool -importcert \
            -keystore ${server_name}.jks -storepass pass${server_name} \
            -file ${ca_cer} \
            -alias ${ca_alias} \
            -noprompt
    keytool -importcert \
            -keystore ${server_name}.jks -storepass pass${server_name} \
            -file ${server_name}.cer \
            -alias ${server_name} \
            -noprompt
    echo "Certificates imported."

    echo "Import $server_name certificates to $ca_alias keyStore..."
        keytool -importcert \
        -keystore ${ca_jks} -storepass ${ca_pass} \
        -file ${server_name}.cer \
        -alias ${server_name}
    echo "Deploying to ${SERVERS[$server_name]} ..."
    cp -f ${server_name}.cer ${caFolder}/${server_name}.cer # copy server_name .cer to CA folder
    cp -f ${ca_alias}.cer ${SERVERS[$server_name]}/${ca_alias}.cer # copy CA .cer to server_name folder
    mv -f ${server_name}.jks ${SERVERS[$server_name]}/${server_name}.jks
    mv -f ${server_name}.cer ${SERVERS[$server_name]}/${server_name}.cer
    echo "Deployment completed."

    echo "Removing $server_name Certificate Signing Request (.csr file)..."
    rm ${server_name}.csr
done

echo "################################################################################"
echo "Last deployment ..."
mv -f ${ca_jks} ${caFolder}/${ca_jks}
mv -f ${ca_alias}.cer ${caFolder}/${ca_alias}.cer
echo "Deployment completed."
echo "################################################################################"
echo "All Done!!!"