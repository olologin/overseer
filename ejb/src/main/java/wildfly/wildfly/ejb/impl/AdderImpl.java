package wildfly.wildfly.ejb.impl;

import wildfly.wildfly.ejb.interfaces.AdderImplRemote;

import javax.ejb.Stateless;

@Stateless
public class AdderImpl implements AdderImplRemote {
    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
