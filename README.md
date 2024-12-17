## Description
This repository provides an example of an issue that occurs with the `Content-Length` header in the response and an interrupted or failed stream when using the `tapir-vertx-server` library.

For testing:
 - http://localhost:8080/test - `Server works` response
 - http://localhost:8080/download - Body will never be delivered
 - http://localhost:8080/download-fail - Body will never be delivered on a fail