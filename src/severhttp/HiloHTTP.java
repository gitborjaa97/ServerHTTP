package severhttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        System.out.println("Recibe peticion: "+ Thread.currentThread().getName());
        boolean salir = false;
        try {
            while (!salir) {
                String peticion = leerCabecera(recibir);
                if (peticion != null) {
                    String archivo = peticion.split(" ")[1];
                    String extension = "html";
                    if (archivo.length() > 1) {
                        extension = archivo.split("\\.")[1];
                    }

                    tipoDeMensaje(extension, archivo, mandar);
                }else{
                    salir = true;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(HiloHTTP.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Termina servicio: "+ Thread.currentThread().getName());
    }

    private String leerCabecera(BufferedReader br) throws SocketException, IOException {
        String mensaje;
        String peticion = "";
        int cont = 0;
        mensaje = br.readLine();
        try {
            while (!mensaje.equals("")) {
                if (cont == 0) {
                    peticion = mensaje;
                }
                cont++;
                mensaje = br.readLine();
            }
        } catch (NullPointerException e) {
            peticion = null;
        }
        System.out.println("Peticion del hilo "
                + Thread.currentThread().getName()+": "+
                peticion);
        return peticion;
    }

    private BufferedReader getReader(Socket s) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(HiloHTTP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return br;
    }

    private OutputStream getOutput(Socket s) {
        OutputStream fos = null;
        try {
            fos = s.getOutputStream();
        } catch (IOException ex) {
            Logger.getLogger(HiloHTTP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fos;
    }

    private void tipoDeMensaje(String extension, String archivo, OutputStream osw) {
        String type = "";
        byte[] recurso = obtenerRecurso(archivo);
        switch (extension) {
            case "ico":
                type = "icon";
                break;
            case "png":
                type = "image";
                break;
            default:
                type = "text";
                break;
        }
        mandarMensaje(osw, extension, recurso, type);
    }

    private void mandarMensaje(OutputStream osw, String extension, byte[] recurso, String tipo) {
        int tamanio = recurso.length;
        try {
            osw.write("HTTP/1.1 200 OK".getBytes());
            String type = "Content-Type: " + tipo + "/" + extension + "\r\n";
            osw.write(type.getBytes());
            String longitud = "Content-Length: " + tamanio + "\r\n";
            osw.write(longitud.getBytes());
            osw.write("\r\n".getBytes());
            osw.write(recurso);
        } catch (IOException ex) {
            Logger.getLogger(HiloHTTP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private byte[] obtenerRecurso(String archivo) {
        if (archivo.equals("/")) {
            archivo = "index.html";
        } else {
            archivo = archivo.substring(1);
        }
        byte[] recurso = null;
        try {
            recurso = Files.readAllBytes(Paths.get(archivo));
        } catch (IOException ex) {
            Logger.getLogger(HiloHTTP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return recurso;
    }

}
