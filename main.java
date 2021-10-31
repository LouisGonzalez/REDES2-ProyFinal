import java.io.*;
import java.util.*;

 class proyecto {

    public static Executes exe = new Executes();

	public static void main(String[] args){
        System.out.println("Defina el ISP que va a modificar 1|2");
        Scanner scan = new Scanner(System.in);
        int isp = scan.nextInt();
        
        try{
            exe.shellCommands("asd");
        } catch(IOException ioe){
            System.out.println(ioe);
        }

    }

}

class Executes {

    void shellCommands(String command) throws IOException{
        Process cmd = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
            String line = "";
            while((line = reader.readLine()) != null){
                System.out.println(line);
            }
    }

}


