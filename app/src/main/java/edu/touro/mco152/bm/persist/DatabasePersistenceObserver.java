package edu.touro.mco152.bm.persist;

import edu.touro.mco152.bm.BenchmarkRunObserver;
import jakarta.persistence.EntityManager;

/** This is the database persistence observer. As the hw requires, it is inside the persist package*/
public class DatabasePersistenceObserver implements BenchmarkRunObserver {
    @Override
    public void addRun(DiskRun run) {
        EntityManager em = EM.getEntityManager();
        em.getTransaction().begin();
        em.persist(run);
        em.getTransaction().commit();
    }
}
