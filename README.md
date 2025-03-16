

### Generate Client/Server Certificates
*Generate Certs & Keys*

`openssl req -newkey rsa:2048 -nodes -keyout client-key.pem -x509 -days 365 -out client-certificate.pem`

`openssl req -newkey rsa:2048 -nodes -keyout server-key.pem -x509 -days 365 -out server-certificate.pem`

*Generate Keystore p12*

```openssl pkcs12 -inkey client-key.pem -in client-certificate.pem -export -out client-certificate.p12```

`openssl pkcs12 -inkey server-key.pem -in server-certificate.pem -export -out server-certificate.p12`


