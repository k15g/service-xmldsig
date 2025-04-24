# XML Signature Service

A simple HTTP server that verifies XML signatures and provides the signing certificate in the response headers.

May suit your needs if you want to verify XML signatures in an application written in a programming language that does not have a suitable library for XML signature verification.


## Using the service

The server will start and listen for incoming HTTP requests on port 8080. 

You can send requests to the server with the target URI specified as query parameters, for example:

```shell
curl -i "http://localhost:8080/?uri=http://example.com"
```

You may also provide a local document for verification:

```shell
curl -i -X POST "http://localhost:8080/" --data-binary '@example.xml'
``` 

The certificate used for signing is available as "X-Signing-Certificate" in the response headers.
When signature validation fails or is not available will the service return a 500 status code.


## Environment variables

`PORT` (default: 8080)
: The port on which the server will listen for incoming requests.