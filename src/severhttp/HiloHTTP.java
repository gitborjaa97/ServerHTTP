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
        //Se generan los canales de envio y recepcion
        BufferedReader recibir = getReader(conexion);
        OutputStream mandar = getOutput(conexion);
        System.out.println("S inicia: " + Thread.currentThread().getName());
        boolean salir = false;
        //Entra en un bucle del que solo saldra cuando termine la conexion
        while (!salir) {
            //Se recibe la primera linea de la peticion que contiene 
            //el archivo a enviar
            String peticion = leerCabecera(recibir);
            //Si este no es null(la conexion sigue activa) se enviara 
            //el recurso solicitado
            if (peticion != null) {
                //Siempre y cuando la peticion no sea vacia 
                //(por algun motivo hay ocasiones en las que firefox manda peticiones 
                //completamente vacias) enviara el recurso
                if (!peticion.equals("")) {
                    //Se separa por espacios
                    String archivo = peticion.split(" ")[1];
                    //Se otorga una extension por defecto, para enviar el inidice 
                    //en caso de que se pida /
                    String extension = "html";
                    if (archivo.length() > 1) {
                        //Si no se coge la extension del archivo
                        extension = archivo.split("\\.")[1];
                    }
                    //Se le envian la extension, el nombre del archivo y el socket 
                    //para enviarlo
                    tipoDeMensaje(extension, archivo, mandar);
                }
            }//Si no se muere el hilo 
            else {
                salir = true;
            }
        }
        System.out.println("Muere el hilo: " + Thread.currentThread().getName());
    }

    //Espera hasta que reciba el mensaje y cuando lo recibe guarda la primera linea
    //y descarta el resto
    private String leerCabecera(BufferedReader br) {
        String mensaje;
        String peticion = "";
        int cont = 0;
        try {
            mensaje = br.readLine();
            //Lee hasta que termine la cabecera siempre y cuando la cabecera 
            //no sea nula
            while (!mensaje.equals("") && mensaje != null) {
                if (cont == 0) {
                    peticion = mensaje;
                }
                cont++;
                mensaje = br.readLine();
            }
        //Captura las execpciones posibles pra asegurarse de que si se cierra la 
        //conexion el hilo muera de manera ordenanda
        } catch (NullPointerException e) {
            peticion = null;
        } catch (SocketException s) {
            peticion = null;
        } catch (IOException j) {
            System.out.println(j);
        }
        //Devuelve la primera linea de la cabecera recibida
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

    //Dependiendo de el archivo que reciba compone la cabecera, obtine el array de bytes
    //del archivo y lo manda
    private void tipoDeMensaje(String extension, String archivo, OutputStream osw) {
        String type = "";
        //Obtiene el recurso en bytes
        byte[] recurso = obtenerRecurso(archivo);
        //Elige el typo dependiendo de la extension del archivo
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
        //Se lo envia como parametro para que componga la cabecera y lo envie
        mandarMensaje(osw, extension, recurso, type);
    }

    private void mandarMensaje(OutputStream osw, String extension, byte[] recurso, String tipo) {
        try {
            //Compone la cabecera con los datos recibidos
            osw.write("HTTP/1.1 200 OK".getBytes());
            String type = "Content-Type: " + tipo + "/" + extension + "\r\n";
            osw.write(type.getBytes());
            String longitud = "Content-Length: " + recurso.length + "\r\n";
            osw.write(longitud.getBytes());
            osw.write("\r\n".getBytes());
            //Envia el recurso
            osw.write(recurso);
            osw.flush();
        }//Captura las excepciones necesarias para asegurarse de que el cliente 
        //mantenga la conexion
        catch (SocketException s) {
        } catch (IOException ex) {
            Logger.getLogger(HiloHTTP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Recibe como parametro el nombre del archivo y devuelve este en array de bytes
    private byte[] obtenerRecurso(String archivo) {
        if (archivo.equals("/")) {
            archivo = "index.html";
        } else {
            archivo = archivo.substring(1);
        }
        byte[] recurso = null;
        try {
            //Files.readAllBytes nos devuelve el contenido de cualquier tipo de
            //archivo en bytes
            recurso = Files.readAllBytes(Paths.get(archivo));
        } catch (IOException ex) {
            Logger.getLogger(HiloHTTP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return recurso;
    }

}
