package foo.bar;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.dmr.ModelNode;

/**
 * @author Heiko Braun
 * @since 25/04/16
 */
public class Main {
    public static void main(String[] args) throws Exception {

        if(args.length==0 || args.length<3) {
            System.out.println("Usage: Main <artefact> <user> <password>");
            return;
        }

        String artefact = args[0];
        String user = args[1];
        String pass = args[2];

        // the digest auth backend
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope("localhost", 9990),
                new UsernamePasswordCredentials(user, pass));

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();


        HttpPost post = new HttpPost("http://localhost:9990/management-upload");
        //post.addHeader("Content-Type", "multipart/form-data");
        post.addHeader("X-Management-Client-Name", "HAL");

        // the file to be uploaded
        File file = new File(artefact);
        FileBody fileBody = new FileBody(file);

        // the DMR operation
        ModelNode operation = new ModelNode();
        operation.get("address").add("deployment", file.getName());
        operation.get("operation").set("add");
        operation.get("runtime-name").set(file.getName());
        operation.get("enabled").set(true);
        operation.get("content").add().get("input-stream-index").set(0);  // point to the multipart index used

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        operation.writeBase64(bout);

        // the multipart
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("uploadFormElement", fileBody);
        builder.addPart("operation", new ByteArrayBody(bout.toByteArray(), ContentType.create("application/dmr-encoded"), "blob"));
        HttpEntity entity = builder.build();

        //entity.writeTo(System.out);

        post.setEntity(entity);

        HttpResponse response = httpclient.execute(post);

        System.out.println(response);
    }
}
