
package severhttp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SeverHTTP {

    public static void main(String[] args) {
        int puerto = 8081;
        //Se crea un ServerSocket
        ServerSocket server = getSS(puerto);
        System.out.println("Servidor en marcha");
        //Se deja en bucle infinito recibiendo peticiones
        while(true){
            Socket peticion = getSocket(server);
            //Por cada peticion lanza un hilo que dara servicio al cliente
            Thread hiloServer = new Thread(new HiloHTTP(peticion));
            hiloServer.start();
        }                
    }
    //Los siguientes metodos permiten limpiar el codigo de try catch
    private static ServerSocket getSS(int puerto){
        ServerSocket s = null;
        try {
            s = new ServerSocket(puerto);
        } catch (IOException ex) {
            Logger.getLogger(SeverHTTP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }
    
    private static Socket getSocket(ServerSocket ss){
        Socket s = null;
        try {
            s = ss.accept();
        } catch (IOException ex) {
            Logger.getLogger(SeverHTTP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }
}
