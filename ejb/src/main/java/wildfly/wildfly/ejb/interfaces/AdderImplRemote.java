package wildfly.wildfly.ejb.interfaces;

import javax.ejb.Remote;

@Remote
public interface AdderImplRemote {
    int add (int a, int b);
}
