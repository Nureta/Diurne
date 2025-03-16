package org.nocturne.sockets

import java.security.KeyStore
import javax.net.ssl.*


object SSLUtil {

    fun createSSLSocket(keystorePass: String, port: Int): SSLServerSocket {

        // Get the keystore
        // System.setProperty("javax.net.debug", "all")
        val keyStore = KeyStore.getInstance("PKCS12")
        val password = keystorePass
        val inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("private/server-certificate.p12")
        keyStore.load(inputStream, password.toCharArray())


        // TrustManagerFactory
        val password2 = keystorePass
        val trustStore = KeyStore.getInstance("PKCS12")
        val trustManagerFactory = TrustManagerFactory.getInstance("PKIX", "SunJSSE")
        val inputStream1 = ClassLoader.getSystemClassLoader().getResourceAsStream("private/client-certificate.p12")
        trustStore.load(inputStream1, password2.toCharArray())
        trustManagerFactory.init(trustStore)
        var x509TrustManager: X509TrustManager? = null
        for (trustManager in trustManagerFactory.trustManagers) {
            if (trustManager is X509TrustManager) {
                x509TrustManager = trustManager
                break
            }
        }

        if (x509TrustManager == null) throw NullPointerException()


        // KeyManagerFactory ()
        val keyManagerFactory = KeyManagerFactory.getInstance("SunX509", "SunJSSE")
        keyManagerFactory.init(keyStore, password.toCharArray())
        var x509KeyManager: X509KeyManager? = null
        for (keyManager in keyManagerFactory.keyManagers) {
            if (keyManager is X509KeyManager) {
                x509KeyManager = keyManager
                break
            }
        }
        if (x509KeyManager == null) throw NullPointerException()


        // set up the SSL Context
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(arrayOf<KeyManager>(x509KeyManager), arrayOf<TrustManager>(x509TrustManager), null)
        val serverSocketFactory = sslContext.serverSocketFactory
        val serverSocket = serverSocketFactory.createServerSocket(port) as SSLServerSocket
        serverSocket.needClientAuth = true
        serverSocket.enabledProtocols = arrayOf("TLSv1.2")
        return serverSocket
    }
}