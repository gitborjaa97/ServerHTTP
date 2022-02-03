
package severhttp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HiloHTTP implements Runnable {
    
    private Socket conexion;

    public HiloHTTP(Socket conexion) {
        this.conexion = conexion;
    }

    @Override
    public void run() {
        BufferedReader recibir = getReader(conexion);
        OutputStream mandar = getOutput(conexion);
        try {   
            String peticion = leerCabecera(recibir);
            System.out.println(peticion);
            mandarMensaje(peticion, mandar);
        }catch (SocketException s){
            System.out.println("Conexion cerrada");
        } catch (IOException ex) {
            Logger.getLogger(HiloHTTP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String leerCabecera(BufferedReader br) throws IOException {
        String mensaje = br.readLine();
        System.out.println(mensaje);
        return mensaje.split(" ")[1];
    }
    
    private BufferedReader getReader(Socket s){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(HiloHTTP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return br;
    }
    
    private OutputStream getOutput(Socket s){
        OutputStream fos = null;
        try {
            fos =s.getOutputStream();
        } catch (IOException ex) {
            Logger.getLogger(HiloHTTP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fos;
    }
    
    private void mandarMensaje(String archivo, OutputStream osw){ 
        String recurso = leerHTML(archivo);
        int tamanio = "<title>Pagina 1</title>".length();
        try {
            osw.write("Content-Type: text/html\r\n".getBytes());
            String longitud = "Content-Length: "+tamanio+"\r\n";
            osw.write(longitud.getBytes());
            osw.write("\r\n".getBytes());
            osw.write("<title>Pagina 1</title>".getBytes());
            System.out.println("Enviado");
        } catch (IOException ex) {
            Logger.getLogger(HiloHTTP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String leerHTML(String archivo){
        String html = "";
        if(archivo.equals("/") || archivo.equals("/index.html")){
            try {
                BufferedReader br = new BufferedReader(new FileReader("index.html"));
                String buffer ;
                while((buffer = br.readLine())!= null){
                    html += buffer;
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(HiloHTTP.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(HiloHTTP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            System.out.println("Todavia no esta programado");
        }
        return html;
    }
    
}
