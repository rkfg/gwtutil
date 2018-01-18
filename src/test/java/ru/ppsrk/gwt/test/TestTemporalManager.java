package ru.ppsrk.gwt.test;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.AuthServiceImpl;
import ru.ppsrk.gwt.server.HibernateCallback;
import ru.ppsrk.gwt.server.HibernateUtil;
import ru.ppsrk.gwt.server.TemporalManager;
import ru.ppsrk.gwt.test.domain.TestEntity;

public class TestTemporalManager extends HibernateTests {

    private static final String TEST_ENTITY_NAME = "TestEntity";
    private static DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

    private class TestEntityManager extends TemporalManager<TestEntity> {

        public TestEntityManager(Session session) {
            super(session, TestEntity.class, "name");
        }

    }

    @BeforeClass
    public static void init() throws GwtUtilException {
        AuthServiceImpl.startTest("hibernate.gwtutil_testmem.cfg.xml");
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthException {
                try {
                    TestEntity testEntity1 = new TestEntity(TEST_ENTITY_NAME);
                    testEntity1.setStartDate(df.parse("07/03/1987"));
                    session.save(testEntity1);

                    TestEntity testEntity2 = new TestEntity(TEST_ENTITY_NAME);
                    testEntity2.setStartDate(df.parse("20/03/1987"));
                    session.save(testEntity2);

                    TestEntity testEntity3 = new TestEntity(TEST_ENTITY_NAME);
                    testEntity3.setStartDate(df.parse("30/03/1987"));
                    session.save(testEntity3);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    @Test
    public void saveMiddle() throws GwtUtilException, ParseException {
        TestEntity testEntity = makeTestEntity();
        testEntity.setStartDate(df.parse("10/03/1987"));
        getManager().save(testEntity);
        TestEntity testEntity1 = (TestEntity) session.get(TestEntity.class, 1L);
        assertEquals(df.parse("07/03/1987"), testEntity1.getStartDate());

        TestEntity testEntity2 = (TestEntity) session.get(TestEntity.class, 2L);
        assertEquals(df.parse("20/03/1987"), testEntity2.getStartDate());

        assertEquals(df.parse("10/03/1987"), testEntity.getStartDate());
    }

    @Test
    public void saveToEnd() throws GwtUtilException, ParseException {
        TestEntity testEntity = makeTestEntity();
        testEntity.setStartDate(df.parse("15/04/1987"));
        getManager().save(testEntity);
        TestEntity testEntity1 = (TestEntity) session.get(TestEntity.class, 1L);
        assertEquals(df.parse("07/03/1987"), testEntity1.getStartDate());

        assertEquals(df.parse("15/04/1987"), testEntity.getStartDate());

        TestEntity testEntity2 = (TestEntity) session.get(TestEntity.class, 2L);
        assertEquals(df.parse("20/03/1987"), testEntity2.getStartDate());

        TestEntity testEntity3 = (TestEntity) session.get(TestEntity.class, 3L);
        assertEquals(df.parse("30/03/1987"), testEntity3.getStartDate());
    }

    private TestEntity makeTestEntity(int idx) {
        return new TestEntity(TEST_ENTITY_NAME + idx);
    }

    private TestEntity makeTestEntity() {
        return new TestEntity(TEST_ENTITY_NAME);
    }

    @Test
    public void saveFirst() throws GwtUtilException, ParseException {
        TestEntity testEntity = makeTestEntity(1);
        testEntity.setStartDate(df.parse("07/03/1987"));
        getManager().save(testEntity);
        testEntity = getManager().get(df.parse("08/03/1987"), TEST_ENTITY_NAME + 1);
        assertEquals(df.parse("07/03/1987"), testEntity.getStartDate());
    }

    @Test
    public void saveReplaceFirst() throws GwtUtilException, ParseException {
        TestEntity testEntity = makeTestEntity();
        testEntity.setStartDate(df.parse("07/03/1987"));
        getManager().save(testEntity);
        TestEntity testEntity1 = (TestEntity) session.get(TestEntity.class, 1L);
        assertNull(testEntity1);
        testEntity = getManager().get(df.parse("08/03/1987"), TEST_ENTITY_NAME);
        assertEquals(df.parse("07/03/1987"), testEntity.getStartDate());
    }

    @Test
    public void saveBeforeFirst() throws GwtUtilException, ParseException {
        TestEntity testEntity = makeTestEntity();
        testEntity.setStartDate(df.parse("01/03/1987"));
        TestEntityManager manager = getManager();
        manager.save(testEntity);
        TestEntity testEntity1 = manager.get(df.parse("05/03/1987"), testEntity.getUniqValue());
        assertNotNull(testEntity1);
        assertEquals(df.parse("01/03/1987"), testEntity1.getStartDate());
    }

    @Test
    public void saveReplaceJoint() throws GwtUtilException, ParseException {
        TestEntity testEntity = makeTestEntity();
        testEntity.setStartDate(df.parse("20/03/1987"));
        getManager().save(testEntity);
        TestEntity testEntity1 = (TestEntity) session.get(TestEntity.class, 2L);
        assertNull(testEntity1);
        testEntity = getManager().get(df.parse("25/03/1987"), TEST_ENTITY_NAME);
        assertEquals(df.parse("20/03/1987"), testEntity.getStartDate());

    }

    @Test
    public void saveSameDates() throws GwtUtilException, ParseException {
        TestEntity testEntity1 = makeTestEntity(1);
        testEntity1.setStartDate(df.parse("07/03/1987"));
        TestEntity testEntity2 = makeTestEntity(2);
        testEntity2.setStartDate(df.parse("07/03/1987"));
        TestEntityManager manager = getManager();
        manager.save(testEntity1);
        manager.save(testEntity2);
        List<TestEntity> retrievedTestEntitys1 = manager.list(df.parse("07/03/1987"), df.parse("08/03/1987"), "TestEntity1");
        assertNotNull(retrievedTestEntitys1);
        assertEquals(1, retrievedTestEntitys1.size());
        TestEntity retrieved1 = retrievedTestEntitys1.get(0);
        assertEquals(retrieved1.getName(), testEntity1.getName());
        assertEquals(retrieved1.getStartDate(), df.parse("07/03/1987"));

        List<TestEntity> retrievedTestEntitys2 = manager.list(df.parse("07/03/1987"), df.parse("08/03/1987"), "TestEntity2");
        assertNotNull(retrievedTestEntitys2);
        assertEquals(1, retrievedTestEntitys2.size());
        TestEntity retrieved2 = retrievedTestEntitys2.get(0);
        assertEquals(retrieved2.getName(), testEntity2.getName());
        assertEquals(retrieved2.getStartDate(), df.parse("07/03/1987"));
    }

    @Test
    public void deleteMiddle() throws GwtUtilException, ParseException {
        getManager().delete(2L);
        TestEntity testEntity1 = (TestEntity) session.get(TestEntity.class, 1L);
        assertEquals(df.parse("07/03/1987"), testEntity1.getStartDate());

        TestEntity testEntity2 = (TestEntity) session.get(TestEntity.class, 2L);
        assertNull(testEntity2);

        TestEntity testEntity3 = (TestEntity) session.get(TestEntity.class, 3L);
        assertEquals(df.parse("30/03/1987"), testEntity3.getStartDate());
    }

    @Test
    public void deleteFirst() throws GwtUtilException, ParseException {
        getManager().delete(1L);
        TestEntity testEntity1 = (TestEntity) session.get(TestEntity.class, 1L);
        assertNull(testEntity1);

        TestEntity testEntity2 = (TestEntity) session.get(TestEntity.class, 2L);
        assertEquals(df.parse("20/03/1987"), testEntity2.getStartDate());

        TestEntity testEntity3 = (TestEntity) session.get(TestEntity.class, 3L);
        assertEquals(df.parse("30/03/1987"), testEntity3.getStartDate());
    }

    @Test
    public void deleteLast() throws GwtUtilException, ParseException {
        getManager().delete(3L);
        TestEntity testEntity1 = (TestEntity) session.get(TestEntity.class, 1L);
        assertEquals(df.parse("07/03/1987"), testEntity1.getStartDate());

        TestEntity testEntity2 = (TestEntity) session.get(TestEntity.class, 2L);
        assertEquals(df.parse("20/03/1987"), testEntity2.getStartDate());

        TestEntity testEntity3 = (TestEntity) session.get(TestEntity.class, 3L);
        assertNull(testEntity3);
    }

    @Test
    public void listTwo() throws ParseException, LogicException {
        List<TestEntity> result = getManager().list(df.parse("01/03/1987"), df.parse("21/03/1987"), TEST_ENTITY_NAME);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void listMulti() throws GwtUtilException, ParseException {
        TestEntityManager manager = getManager();
        initMultiTestEntity(manager);

        // before times to middle of the first entity
        List<TestEntity> result = manager.list(df.parse("01/03/1987"), df.parse("08/03/1987"), "TestEntity1");
        assertNotNull(result);
        assertEquals(1, result.size());

        // before times to middle of the second entity
        List<TestEntity> result2 = manager.list(df.parse("06/03/1987"), df.parse("21/04/1987"), "TestEntity2");
        assertNotNull(result2);
        assertEquals(2, result2.size());

        // precise first and second
        List<TestEntity> result3 = manager.list(df.parse("06/03/1987"), df.parse("10/03/1987"), "TestEntity1");
        assertNotNull(result3);
        assertEquals(2, result3.size());

        // inside first
        List<TestEntity> result4 = manager.list(df.parse("11/03/1987"), df.parse("15/04/1987"), "TestEntity2");
        assertNotNull(result4);
        assertEquals(1, result4.size());

        // from second to after times
        List<TestEntity> result5 = manager.list(df.parse("25/03/1987"), df.parse("08/12/2015"), "TestEntity2");
        assertNotNull(result5);
        assertEquals(2, result5.size());

        // from middle of first to middle of second
        List<TestEntity> result6 = manager.list(df.parse("10/03/1987"), df.parse("25/03/1987"), TEST_ENTITY_NAME);
        assertNotNull(result6);
        assertEquals(2, result6.size());

        // precise second start to middle
        List<TestEntity> result7 = manager.list(df.parse("20/03/1987"), df.parse("25/03/1987"), TEST_ENTITY_NAME);
        assertNotNull(result7);
        assertEquals(1, result7.size());

        // middle of second to end of second
        List<TestEntity> result8 = manager.list(df.parse("25/03/1987"), df.parse("30/03/1987"), TEST_ENTITY_NAME);
        assertNotNull(result8);
        assertEquals(2, result8.size());
    }

    @Test
    public void gets() throws GwtUtilException, ParseException {
        TestEntityManager manager = getManager();
        // edge of the first entity
        TestEntity testEntity1 = manager.get(df.parse("07/03/1987"), TEST_ENTITY_NAME);
        assertEquals(df.parse("07/03/1987"), testEntity1.getStartDate());

        // middle of the first entity
        TestEntity testEntity2 = manager.get(df.parse("10/03/1987"), TEST_ENTITY_NAME);
        assertEquals(df.parse("07/03/1987"), testEntity2.getStartDate());

        // joint
        TestEntity testEntity3 = manager.get(df.parse("20/03/1987"), TEST_ENTITY_NAME);
        assertEquals(df.parse("20/03/1987"), testEntity3.getStartDate());

        // before times
        TestEntity testEntity4 = manager.get(df.parse("01/03/1987"), TEST_ENTITY_NAME);
        assertNull(testEntity4);

        // after times
        TestEntity testEntity5 = manager.get(df.parse("08/12/2015"), TEST_ENTITY_NAME);
        assertEquals(df.parse("30/03/1987"), testEntity5.getStartDate());
    }

    private void initMultiTestEntity(TestEntityManager manager) throws GwtUtilException, ParseException {
        TestEntity testEntity1 = new TestEntity("TestEntity1");
        testEntity1.setStartDate(df.parse("06/03/1987"));
        manager.save(testEntity1);

        TestEntity testEntity2 = new TestEntity("TestEntity1");
        testEntity2.setStartDate(df.parse("10/03/1987"));
        manager.save(testEntity2);

        TestEntity testEntity3 = new TestEntity("TestEntity2");
        testEntity3.setStartDate(df.parse("09/03/1987"));
        manager.save(testEntity3);

        TestEntity testEntity4 = new TestEntity("TestEntity2");
        testEntity4.setStartDate(df.parse("20/04/1987"));
        manager.save(testEntity4);
    }

    private TestEntityManager getManager() {
        return new TestEntityManager(session);
    }

    @Test
    public void raceConditions() throws InterruptedException, ExecutionException, ParseException, GwtUtilException {
        int procs = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(procs);
        final List<String> dates = Arrays.asList("01/03/1987", "15/03/1987", "22/03/1987", "25/03/1987", "31/03/1987");
        List<Future<Void>> futures = new ArrayList<Future<Void>>(dates.size());
        for (int j = 0; j < 100; j++) {
            final TestEntityManager manager = getManager();
            futures.clear();
            Collections.shuffle(dates);
            for (int i = 0; i < dates.size(); i++) {
                final int idx = i;
                Future<Void> future = pool.submit(new Callable<Void>() {

                    /*
                     * Date formats are not synchronized. It is recommended to create separate format instances for each thread. If multiple
                     * threads access a format concurrently, it must be synchronized externally.
                     */
                    private DateFormat df_local = DateFormat.getDateInstance(DateFormat.SHORT);

                    @Override
                    public Void call() throws Exception {
                        TestEntity testEntity = makeTestEntity();
                        testEntity.setStartDate(df_local.parse(dates.get(idx)));
                        manager.save(testEntity);
                        return null;
                    }
                });
                futures.add(future);
            }
            for (Future<Void> future : futures) {
                future.get();
            }
            TestEntity testEntity1 = manager.get(df.parse("05/03/1987"), TEST_ENTITY_NAME);
            assertNotNull(testEntity1);
            assertEquals(df.parse("01/03/1987"), testEntity1.getStartDate());

            testEntity1 = manager.get(df.parse("09/03/1987"), TEST_ENTITY_NAME);
            assertNotNull(testEntity1);
            assertEquals(df.parse("07/03/1987"), testEntity1.getStartDate());

            testEntity1 = manager.get(df.parse("18/03/1987"), TEST_ENTITY_NAME);
            assertNotNull(testEntity1);
            assertEquals(df.parse("15/03/1987"), testEntity1.getStartDate());

            testEntity1 = manager.get(df.parse("21/03/1987"), TEST_ENTITY_NAME);
            assertNotNull(testEntity1);
            assertEquals(df.parse("20/03/1987"), testEntity1.getStartDate());

            testEntity1 = manager.get(df.parse("24/03/1987"), TEST_ENTITY_NAME);
            assertNotNull(testEntity1);
            assertEquals(df.parse("22/03/1987"), testEntity1.getStartDate());

            testEntity1 = manager.get(df.parse("27/03/1987"), TEST_ENTITY_NAME);
            assertNotNull(testEntity1);
            assertEquals(df.parse("25/03/1987"), testEntity1.getStartDate());

            testEntity1 = manager.get(df.parse("30/03/1987"), TEST_ENTITY_NAME);
            assertNotNull(testEntity1);
            assertEquals(df.parse("30/03/1987"), testEntity1.getStartDate());

            testEntity1 = manager.get(df.parse("31/03/1987"), TEST_ENTITY_NAME);
            assertNotNull(testEntity1);
            assertEquals(df.parse("31/03/1987"), testEntity1.getStartDate());

            cleanup();
            setSession();
        }
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);
    }

}