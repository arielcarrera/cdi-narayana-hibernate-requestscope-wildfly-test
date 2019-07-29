package com.github.arielcarrera.cdi.test;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.arielcarrera.cdi.test.entities.TestEntity;

/**
 * 
 * @author Ariel Carrera
 *
 */
@RunWith(Arquillian.class)
public class TransactionalImplTest {

    @Inject
    UserTransaction userTransaction;

    @Inject
    private Repository1 repo1;
    
    @Resource(lookup = "java:/MyDS")
    private DataSource myDS;
    
    @Deployment
    public static WebArchive createTestArchive() {

        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, TransactionalImplTest.class.getPackage())
                .addPackage(TestEntity.class.getPackage())
                .addAsResource("META-INF/persistence.xml","META-INF/persistence.xml")
                .addAsWebInfResource("datasource-ds.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @After
    public void tearDown() {

        try {
            System.out.println("Starting CLEAR() method");
            repo1.clear();
        } catch (Exception e) {
            // do nothing
        }
    }
    
    private static int count = 0;

    private TestEntity getNewEntity() {
	return new TestEntity(null, ++count, count);
    }
    
    private void assertEntities(TestEntity... expected) throws Exception {
	System.out.println("Starting assertEntities() method");
	assertEquals(Arrays.asList(expected), getEntitiesFromTheDatabase());
    }

    private List<TestEntity> getEntitiesFromTheDatabase() throws Exception {
	Connection connection = myDS.getConnection("sa","");
	Statement statement = connection.createStatement();
	ResultSet resultSet = statement.executeQuery("SELECT `id`,`value`,`uniqueValue` FROM `TestEntity`");
	List<TestEntity> entities = new LinkedList<>();
	while (resultSet.next()) {
	    entities.add(
		    new TestEntity(resultSet.getInt("id"), resultSet.getInt("value"), resultSet.getInt("uniqueValue")));
	}
	resultSet.close();
	statement.close();
	connection.close();
	return entities;
    }
    
    //FAILED TEST
    @Test(expected=RuntimeException.class)
    public void testSuspendMergeFirst() throws Exception {
	System.out.println("Starting testSuspendMergeFirst() method");
	TestEntity firstEntity = getNewEntity();
	TestEntity secondEntity = getNewEntity();
	try {
	    repo1.suspendAndRollback_MergeFirst(firstEntity, secondEntity);
	} catch (Exception e) {
	    assertEntities(secondEntity);
	    throw e;
	}
    }
    
    
    @Test(expected=RuntimeException.class)
    public void testSuspendNewTxFirst() throws Exception {
	System.out.println("Starting testSuspendNewTxFirst() method");
	TestEntity firstEntity = getNewEntity();
	TestEntity secondEntity = getNewEntity();
	try {
	    repo1.suspendAndRollback_NewTxFirst(firstEntity, secondEntity);
	} catch (Exception e) {
	    assertEntities(firstEntity);
	    throw e;
	}
    }

    /**
     * Adds two entries to the database and commits the transaction. At the end of the test two entries should be in the
     * database.
     *
     * @throws Exception
     */
    @Test
    public void testCommit() throws Exception {
	System.out.println("Starting testCommit() method");
        TestEntity firstEntity = getNewEntity();
        TestEntity secondEntity = getNewEntity();
        userTransaction.begin();
        repo1.save(firstEntity);
        repo1.save(secondEntity);
        userTransaction.commit();
        assertEntities(firstEntity, secondEntity);
    }

    /**
     * Adds two entries to the database and rolls back the transaction. At the end of the test no entries should be in the
     * database.
     * 
     * @throws Exception
     */
    @Test
    public void testRollback() throws Exception {
	System.out.println("Starting testRollback() method");
	TestEntity firstEntity = getNewEntity();
	TestEntity secondEntity = getNewEntity();
	userTransaction.begin();
        repo1.save(firstEntity);
        repo1.save(secondEntity);
        userTransaction.rollback();
        assertEntities();
    }

}