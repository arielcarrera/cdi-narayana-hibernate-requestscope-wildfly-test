package com.github.arielcarrera.cdi.test;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.github.arielcarrera.cdi.test.entities.TestEntity;

/**
 * 
 * @author Ariel Carrera
 *
 */
public class Repository2 {

    @Inject
    private EntityManager entityManager;

    public List<TestEntity> findAll() {
        assert entityManager != null;
        List<TestEntity> entities = entityManager
        	.createQuery("select qe from TestEntity qe", TestEntity.class).getResultList();
        return entities;
    }

    @Transactional(TxType.REQUIRES_NEW)
    public Integer save(TestEntity quickstartEntity) {
        assert entityManager != null;
        if (quickstartEntity.getId() == null) {
            entityManager.persist(quickstartEntity);
        } else {
            entityManager.merge(quickstartEntity);
        }
        return quickstartEntity.getId();
    }

    @Transactional
    public void clear() {
        assert entityManager != null;
        findAll().forEach(entityManager::remove);
    }

}
