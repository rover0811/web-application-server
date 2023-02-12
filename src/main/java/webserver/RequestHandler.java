package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ch.qos.logback.core.boolex.Matcher;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(in));

            String line = bufferedReader.readLine();

            if(line == null) {
                return;
            }
            String[] firstline=line.split(" ");
            String url=firstline[1];

            if (url.equals("/index.html"))
                getIndex(url,out);
            else if (url.equals("/user/form.html")) {
                getForm(url,out);
            }
            else if (url.startsWith("/user/create")) {
                int index=url.indexOf("?");
                String requestPath=url.substring(0,index);
                String params=url.substring((index+1));
                Map<String, String> map = HttpRequestUtils.parseQueryString(params);
                User newuser=new User(map.get("userId"),map.get("password"),map.get("name"),map.get("email"));
                String a=newuser.toString();
                log.debug(a);
                byte[] body=a.getBytes();
                DataOutputStream dos = new DataOutputStream(out);
                response200Header(dos, body.length);
                responseBody(dos, body);
            }


            //log 찍기
//            while(!"".equals(line)) {
//                log.info(line);
//                line = bufferedReader.readLine();
//            };



//            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
//            DataOutputStream dos = new DataOutputStream(out);
//
//            response200Header(dos, body.length);
//            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private void getIndex(String url,OutputStream out) throws IOException {

        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        DataOutputStream dos = new DataOutputStream(out);

        response200Header(dos, body.length);
        responseBody(dos, body);
    }
    private void getForm(String url,OutputStream out) throws IOException {

        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        DataOutputStream dos = new DataOutputStream(out);

        response200Header(dos, body.length);
        responseBody(dos, body);
    }


    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
