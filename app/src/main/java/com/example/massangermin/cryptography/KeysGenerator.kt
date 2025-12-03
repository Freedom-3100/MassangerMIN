package com.example.massangermin.cryptography

import java.security.KeyPairGenerator
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PrivateKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement

class KeysGenerator {

    fun generate_key_pair(): KeyPair
    {
        val keyPairGenerator = KeyPairGenerator.getInstance("EC")
        keyPairGenerator.initialize(256)
        return keyPairGenerator.generateKeyPair()
    }

    fun caclc_shared_secret(myPrivateKey: PrivateKey, otherPublicKey: ByteArray): ByteArray
    {
        val keyFactory = KeyFactory.getInstance("EC")
        val publicKeySpec = X509EncodedKeySpec(otherPublicKey)
        val otherPartyPublicKey = keyFactory.generatePublic(publicKeySpec)

        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(myPrivateKey)
        keyAgreement.doPhase(otherPartyPublicKey, true)

        return keyAgreement.generateSecret()
    }
}

