package org.zaproxy.addon.commonlib.db;

import org.datanucleus.enhancement.Persistable;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

public class DbUtils {
    
    private DbUtils() {}
    
    public static void persistEntity(String persistenceUnitName, Persistable entity) {
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(persistenceUnitName);
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            pm.makePersistent(entity);
            tx.commit();
        }
        finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }
}
