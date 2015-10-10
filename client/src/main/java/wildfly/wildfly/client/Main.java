package wildfly.wildfly.client;

import wildfly.wildfly.ejb.interfaces.AdderImplRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Main {
    public static void main(String[] args) throws NamingException {
        Context context = new InitialContext();
        AdderImplRemote remote=(AdderImplRemote)context.lookup("st1");
        System.out.println(remote.add(32,32));
    }
}
