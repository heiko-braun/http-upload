

# Simple demo for uploading deployments using the apache http client

See `src` directory for further details.

## Prepare WildFly:
Set the allowed origins to enable CORS in `standalone.xml`
```
<management-interfaces>
  <http-interface allowed-origins="http://localhost"
      security-realm="ManagementRealm" http-upgrade-enabled="true">
      <socket-binding http="management-http"/>
  </http-interface>
</management-interfaces>
```                
## Prepare Client:

Upload to : http://localhost:9990/management-upload

Send a multipart POST, consisting of:

1) binary content
2) A form field 'operation' with a base64 encoded DMR operation (and content type "application/dmr-encoded"  ),
 like this one:

```
ModelNode operation = new ModelNode();
operation.get("address").add("deployment", file.getName());
operation.get("operation").set("add");
operation.get("runtime-name").set(file.getName());
operation.get("enabled").set(true);
operation.get("content").add().get("input-stream-index").set(0);
```

4) An overall header 'X-Management-Client-Name','HAL' (xss)
