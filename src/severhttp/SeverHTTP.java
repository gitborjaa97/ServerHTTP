
package severhttp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SeverHTTP {

    public static void main(String[] args) {
        int puerto = 8081;
        ServerSocket server = getSS(puerto);
        System.out.println("Servidor en marcha");
        while(true){
            Socket peticion = getSocket(server);
            System.out.println("Peticion recibida");
            Thread hiloServer = new Thread(new HiloHTTP(peticion));
            hiloServer.start();
        }                
    }
    
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
