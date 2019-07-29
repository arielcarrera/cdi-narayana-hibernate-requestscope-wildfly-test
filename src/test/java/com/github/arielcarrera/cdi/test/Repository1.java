package com.github.arielcarrera.cdi.test;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import com.github.arielcarrera.cdi.test.entities.TestEntity;

/**
 * 
 * @author Ariel Carrera
 *
 */
public class Repository1 {

    @Inject
    private EntityManager entityManager;
    
    @Inject
    private Repository2 repo2;

    public List<TestEntity> findAll() {
        assert entityManager != null;
        List<TestEntity> entities = entityManager
                .createQuery("select qe from TestEntity qe", TestEntity.class).getResultList();
        return entities;
    }

    @Transactional
    public Integer save(TestEntity TestEntity) {
        assert entityManager != null;
        if (TestEntity.getId() == null) {
            entityManager.persist(TestEntity);
        } else {
            entityManager.merge(TestEntity);
        }
        System.out.println("Saved entity: " + TestEntity);
        return TestEntity.getId();
    }

    @Transactional
    public void suspendAndRollback_NewTxFirst(TestEntity TestEntity, TestEntity TestEntity2) {
        assert entityManager != null;
        
        repo2.save(TestEntity);
        System.out.println("Saved 1 entity: " + TestEntity);
        if (TestEntity2.getId() == null) {
            entityManager.persist(TestEntity2);
        } else {
            entityManager.merge(TestEntity2);
        }
        System.out.println("Saved 2 entity: " + TestEntity2);
        
        throw new RuntimeException("for rollback");
    }
    
    @Transactional
    public void suspendAndRollback_MergeFirst(TestEntity TestEntity, TestEntity TestEntity2) {
        assert entityManager != null;
        
        if (TestEntity.getId() == null) {
            entityManager.persist(TestEntity);
        } else {
            entityManager.merge(TestEntity);
        }
        System.out.println("Saved 1 entity: " + TestEntity);

        repo2.save(TestEntity2);
        
        System.out.println("Saved 2 entity: " + TestEntity2);
        
        throw new RuntimeException("for rollback");
    }
    
    @Transactional
    public void clear() {
        assert entityManager != null;
        findAll().forEach(entityManager::remove);
        repo2.clear();
    }

}
