import java.io.*;
import java.util.*;

 class proyecto {

    public static Executes exe = new Executes();
    public static Data data = new Data();

	public static void main(String[] args){
        try{
        Scanner scan = new Scanner(System.in);
        System.out.println("1. ISP(1/2)\n"+
                            "2. Balanceador");
        int script = scan.nextInt();
        if(script == 1){
            configurationISP(scan);
        } else {
            confBalanceador(scan);
        }


        } catch(IOException ioe){
            System.out.println(ioe);
        }

    }


    public static void configurationISP(Scanner scan) throws IOException{
        System.out.println("Defina el ISP que va a modificar 1|2");
        int isp = scan.nextInt();
        int ipInterfaz = isp+1;
        String up = "ip addr add 10.10."+ipInterfaz+".1/24 dev "+data.INTERFACE_DYNAMIC;
        String down = "ip addr add "+isp+"0."+isp+"0."+isp+"0.2/24 dev "+data.INTERFACE_STATIC;
        String in = "filter add dev "+data.INTERFACE_DYNAMIC+" parent 1:0 protocol ip prio 1 u32 match ip dst";
        String out = "filter add dev "+data.INTERFACE_STATIC+" parent 1:0 protocol ip prio 1 u32 match ip src";
        String IP = "10.10."+ipInterfaz+".1";
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
            exe.shellCommands("/usr/sbin/tc class add dev "+data.INTERFACE_STATIC+" parent 1: classid 1:10 htb rate 500kbit ceil 500kbit");
            exe.shellCommands("/usr/sbin/tc qdisc add dev "+data.INTERFACE_STATIC+" parent 1:10 handle 10: sfq perturb 10");

            exe.shellCommands("/usr/sbin/tc "+in+" "+IP+" flowid 1:10");
            exe.shellCommands("/usr/sbin/tc "+out+" "+IP+" flowid 1:10");

    }

    public static void confBalanceador(Scanner scan) throws IOException{
        System.out.println("A que interfaz quiere ser redirigido:\n"+
                            "1. ISP1\n"+
                            "2. ISP2\n"+
                            "3. AMBOS\n");
        int dist = scan.nextInt();
        double paquete = 0;
        if(dist == 3){                   
            System.out.println("Porcentaje de paquetes:");
            paquete = scan.nextDouble();
        }
        //borrando iniciales
        exe.shellCommands("iptables -t mangle -F");
        exe.shellCommands("iptables -t nat -F");

        //Inicializaciones
        exe.shellCommands("ip route add 10.10.2.0/24 dev enp0s3 src 10.10.2.2 table isp1");
        exe.shellCommands("ip route add 10.10.3.0/24 dev enp0s8 src 10.10.3.2 table isp2");
        exe.shellCommands("ip route add default via 10.10.2.1 table isp1");
        exe.shellCommands("ip route add default via 10.10.3.1 table isp2");
        //alfinal hacer un ip route show table isp1/isp2
        exe.shellCommands("iptables -t mangle -A PREROUTING -j CONNMARK --restore-mark");
        exe.shellCommands("iptables -t mangle -A PREROUTING -m mark ! --mark 0 -j ACCEPT"); 
        exe.shellCommands("iptables -t mangle -A PREROUTING -j MARK --set-mark 3");
        if(dist == 1){
            exe.shellCommands("iptables -t mangle -A PREROUTING -m statistic --mode random --probability 0 -j MARK --set-mark 4");
        } else if(dist == 2){
            exe.shellCommands("iptables -t mangle -A PREROUTING -m statistic --mode random --probability 0 -j MARK --set-mark 3");
        } else if(dist == 3){
            exe.shellCommands("iptables -t mangle -A PREROUTING -m statistic --mode random --probability "+paquete+" -j MARK --set-mark 4");
        }
        exe.shellCommands("iptables -t mangle -A PREROUTING -j CONNMARK --save-mark");
        //alfinal hacer un iptables -t mangle -S
        exe.shellCommands("iptables -t nat -A POSTROUTING -j MASQUERADE");
        //alfinal hacer un iptables -t nat -S
        exe.shellCommands("ip rule add fwmark 3 table isp1 prio 33000");
        exe.shellCommands("ip rule add fwmark 4 table isp2 prio 33000");
        //alfinal hacer un ip rule show
        //si hay default en un ip route show borrarlas 




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


