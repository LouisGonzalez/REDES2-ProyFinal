import java.io.*;
import java.util.*;

 class proyecto {

    public static Executes exe = new Executes();
    public static Data data = new Data();

	public static void main(String[] args){
        try{
        System.out.println("Defina el ISP que va a modificar 1|2");
        Scanner scan = new Scanner(System.in);
        int isp = scan.nextInt();
        String up = "ip addr add "+isp+"0."+isp+"0."+isp+"0.1/24 dev "+data.INTERFACE_DYNAMIC;
        String down = "ip addr add "+isp+"0."+isp+"0."+isp+"0.2/24 dev "+data.INTERFACE_STATIC;
        String in = "filter add dev "+data.INTERFACE_DYNAMIC+" parent 1:0 protocol ip prio 1 u32 match ip dst";
        String out = "filter add dev "+data.INTERFACE_STATIC+" parent 1:0 protocol ip prio 1 u32 match ip src";
        String IP = isp+"0."+isp+"0."+isp+"0.1";
            exe.shellCommands("/usr/sbin/tc qdisc del dev "+data.INTERFACE_DYNAMIC+" root");
            exe.shellCommands("/usr/sbin/tc qdisc del dev "+data.INTERFACE_DYNAMIC+" ingress");
            exe.shellCommands("/usr/sbin/tc qdisc del dev "+data.INTERFACE_STATIC+" root");
            exe.shellCommands("echo hola mundo");
            exe.shellCommands(up);
            exe.shellCommands("modprobe ifb numifbs=1");
            exe.shellCommands("ip link set dev "+data.INTERFACE_STATIC+" up");
            exe.shellCommands("/usr/sbin/tc qdisc del dev "+data.INTERFACE_DYNAMIC+" root 2>/dev/null");
            exe.shellCommands("/usr/sbin/tc qdisc del dev "+data.INTERFACE_DYNAMIC+" ingress 2>/dev/null");
            exe.shellCommands("/usr/sbin/tc qdisc del dev "+data.INTERFACE_STATIC+" root 2>/dev/null");
            exe.shellCommands("/usr/sbin/tc qdisc add dev "+data.INTERFACE_DYNAMIC+" handle ffff: ingress");
            exe.shellCommands("/usr/sbin/tc filter add dev "+data.INTERFACE_DYNAMIC+" parent ffff: protocol ip u32 match u32 0 0 action mirred egress redirect dev "+data.INTERFACE_STATIC);
            
            exe.shellCommands("/usr/sbin/tc qdisc add dev "+data.INTERFACE_DYNAMIC+" root handle 1: htb default 10");
            exe.shellCommands("/usr/sbin/tc class add dev "+data.INTERFACE_DYNAMIC+" parent 1: classid 1:10 htb rate 1000kbit ceil 1000kbit");
            exe.shellCommands("/usr/sbin/tc qdisc add dev "+data.INTERFACE_DYNAMIC+" parent 1:10 handle 10: sfq perturb 10");

            exe.shellCommands("/usr/sbin/tc qdisc add dev "+data.INTERFACE_STATIC+" root handle 1: htb default 10");
            exe.shellCommands("/usr/sbin/tc class add dev "+data.INTERFACE_STATIC+" parent 1: classid 1:10 htb rate 50kbit ceil 50kbit");
            exe.shellCommands("/usr/sbin/tc qdisc add dev "+data.INTERFACE_STATIC+" parent 1:10 handle 10: sfq perturb 10");

            exe.shellCommands("/usr/sbin/tc "+in+" "+IP+" flowid 1:10");
            exe.shellCommands("/usr/sbin/tc "+out+" "+IP+" flowid 1:10");


        } catch(IOException ioe){
            System.out.println(ioe);
        }

    }

}

class Data {

    public static String INTERFACE_DYNAMIC = "enp0s8";
    public static String INTERFACE_STATIC = "ifb0";


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


