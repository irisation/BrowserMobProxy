import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public class FileDownloader implements HttpResponseInterceptor {

    private Set<String> contentTypes = new HashSet<String>();
    private File tempDir = null;
    private File tempFile = null;
    private String fileName = "";

    public FileDownloader addContentType(String contentType) {
        contentTypes.add(contentType);

        return this;
    }

    public FileDownloader setFileName(String fileName) {
        this.fileName = fileName;

        return this;
    }

    @Override
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        String contentType = response.getFirstHeader("Content-Type").getValue();
//        context.getAttribute()
        System.out.println(((RequestWrapper) context.getAttribute("http.request")).getURI().getPath() + " - " + contentType);
        if (contentTypes.contains(contentType)) {// && ((RequestWrapper) context.getAttribute("http.request")).getURI().getPath().contains(fileName)) {
            System.out.println("!");


            String postfix = contentType.substring(contentType.indexOf('/') + 1);
            tempFile = File.createTempFile("downloaded", "." + postfix, tempDir);
            tempFile.deleteOnExit();

            FileOutputStream outputStream = new FileOutputStream(tempFile);
            outputStream.write(EntityUtils.toByteArray(response.getEntity()));
            outputStream.close();

            response.removeHeaders("Content-Type");
            response.removeHeaders("Content-Encoding");
            response.removeHeaders("Content-Disposition");
            response.removeHeaders("Content-Length");

            response.addHeader("Content-Type", "text/html");
            response.addHeader("Content-Length", "" + tempFile.getAbsolutePath().length());
            response.setEntity(new StringEntity(tempFile.getAbsolutePath()));
        }
    }
}
