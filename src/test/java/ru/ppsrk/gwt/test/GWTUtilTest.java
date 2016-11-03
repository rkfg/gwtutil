package ru.ppsrk.gwt.test;

import static org.junit.Assert.*;
import static ru.ppsrk.gwt.server.ServerUtils.*;

import java.util.List;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.client.NestedSetManagerException;
import ru.ppsrk.gwt.server.HibernateCallback;
import ru.ppsrk.gwt.server.HibernateUtil;
import ru.ppsrk.gwt.server.ServerUtils;
import ru.ppsrk.gwt.server.nestedset.NestedSetManager;
import ru.ppsrk.gwt.server.nestedset.NestedSetManagerNG;
import ru.ppsrk.gwt.test.domain.Dept;
import ru.ppsrk.gwt.test.domain.DeptHier;
import ru.ppsrk.gwt.test.domain.DeptNG;
import ru.ppsrk.gwt.test.dto.DeptHierDTO;

public class GWTUtilTest {
    NestedSetManager<Dept, DeptHierDTO> nsm = new NestedSetManager<Dept, DeptHierDTO>(Dept.class, DeptHierDTO.class);
    private Object lock = new Object();

    @BeforeClass
    public static void login() throws LogicException, ClientAuthException {
        HibernateUtil.initSessionFactory("hibernate.gwtutil_testmem.cfg.xml");
        ServerUtils.importSQL("depthier.sql");

    }

    @Before
    public void init() throws LogicException, ClientAuthException {
        ServerUtils.resetTables(new String[] { "terrdepts" });
        Dept rootNode = nsm.insertRootNode(new Dept());
        assertEquals(rootNode.getId().longValue(), 1L);
        assertEquals(rootNode.getLeftNum().longValue(), 1L);
        assertEquals(rootNode.getRightNum().longValue(), 2L);
        Dept sq11 = nsm.insertNode(new Dept("11 Отряд", "Краснозатонский"), rootNode.getId());
        Dept pch111 = nsm.insertNode(new Dept("111 ПЧ", "Краснозатонский"), sq11.getId());
        assertEquals(pch111.getId().longValue(), 3L);
        assertEquals(pch111.getLeftNum().longValue(), 3L);
        assertEquals(pch111.getRightNum().longValue(), 4L);
        rootNode = nsm.getRootNode();
        assertEquals(rootNode.getId().longValue(), 1L);
        assertEquals(rootNode.getLeftNum().longValue(), 1L);
        assertEquals(rootNode.getRightNum().longValue(), 6L);
        Dept sq12 = nsm.insertNode(new Dept("12 Отряд", "Микунь"), rootNode.getId());
        Dept pch121 = nsm.insertNode(new Dept("121 ПЧ", "Микунь"), sq12.getId());
        nsm.insertNode(new Dept("1 ОП 121 ПЧ", "Кожмудор"), pch121.getId());
    }

    @Before
    public void initNG() throws LogicException, ClientAuthException {
        ServerUtils.resetTables(new String[] { "terrdeptsNG" });
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthException {
                NestedSetManagerNG<DeptNG> nsmNG = new NestedSetManagerNG<>(DeptNG.class, session, lock);
                Long rootId = nsmNG.insertRootNode(new DeptNG()).getId();
                DeptNG sq11 = nsmNG.insertNode(new DeptNG("11 Отряд", "Краснозатонский"), rootId);
                nsmNG.insertNode(new DeptNG("111 ПЧ", "Краснозатонский"), sq11.getId());
                DeptNG sq12 = nsmNG.insertNode(new DeptNG("12 Отряд", "Микунь"), rootId);
                DeptNG pch121 = nsmNG.insertNode(new DeptNG("121 ПЧ", "Микунь"), sq12.getId());
                nsmNG.insertNode(new DeptNG("1 ОП 121 ПЧ", "Кожмудор"), pch121.getId());
                DeptNG rootNode = nsmNG.getRootNode();
                session.refresh(rootNode);
                assertEquals(1L, rootNode.getId().longValue());
                assertEquals(1L, rootNode.getLeftNum().longValue());
                assertEquals(12L, rootNode.getRightNum().longValue());
                return null;
            }
        });

    }

    @Test
    public void testNestedSetInsert() throws LogicException, ClientAuthException {
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @SuppressWarnings("unchecked")
            @Override
            public Void run(Session session) throws LogicException, ClientAuthenticationException {
                List<Dept> depts = session.createQuery("from Dept order by id").list();
                assertEquals(6, depts.size());
                // root
                assertEquals(1, depts.get(0).getLeftNum().longValue());
                assertEquals(12, depts.get(0).getRightNum().longValue());
                // sq11
                assertEquals(2, depts.get(1).getLeftNum().longValue());
                assertEquals(5, depts.get(1).getRightNum().longValue());
                // pch111
                assertEquals(3, depts.get(2).getLeftNum().longValue());
                assertEquals(4, depts.get(2).getRightNum().longValue());
                // sq12
                assertEquals(6, depts.get(3).getLeftNum().longValue());
                assertEquals(11, depts.get(3).getRightNum().longValue());
                // pch121
                assertEquals(7, depts.get(4).getLeftNum().longValue());
                assertEquals(10, depts.get(4).getRightNum().longValue());
                // op1pch121
                assertEquals(8, depts.get(5).getLeftNum().longValue());
                assertEquals(9, depts.get(5).getRightNum().longValue());
                return null;
            }

        });
    }

    @Test
    public void testChildrenByParent() throws LogicException, ClientAuthException {
        List<Dept> depts = nsm.getChildren(1L, "id", true);
        assertEquals(2L, depts.size());
        assertEquals("11 Отряд", depts.get(0).getName());
        assertEquals("12 Отряд", depts.get(1).getName());
        List<Dept> pchs = nsm.getChildren(depts.get(1).getId(), "id", true);
        assertEquals(1L, pchs.size());
        assertEquals("121 ПЧ", pchs.get(0).getName());
        List<Dept> ops = nsm.getChildren(pchs.get(0).getId(), "id", true);
        assertEquals(1L, ops.size());
        assertEquals("1 ОП 121 ПЧ", ops.get(0).getName());
        List<Dept> allDepts = nsm.getChildren(1L, "id", false);
        assertEquals(5, allDepts.size());
    }

    @Test
    public void testParentByChild() throws LogicException, ClientAuthException {
        Dept parent = nsm.getParentByChild(6L, 0L);
        assertEquals(1L, parent.getId().longValue());
        parent = nsm.getParentByChild(6L, -1L);
        assertEquals("121 ПЧ", parent.getName());
        parent = nsm.getParentByChild(6L, -2L);
        assertEquals("12 Отряд", parent.getName());
    }

    @Test
    public void testImportHier() throws LogicException, ClientAuthException {
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthException {
                @SuppressWarnings("unchecked")
                List<DeptHier> depts = session.createQuery("from DeptHier d where d.name != 'Default' order by d.id").list();
                nsm.insertHierarchic(mapArray(depts, DeptHierDTO.class), 1L);
                @SuppressWarnings("unchecked")
                List<Dept> insertedDepts = session
                        .createQuery(
                                "from Dept d where d.name in ('1 ОП 123 ПЧ', '13 Отряд', '1 ОП 131 ПЧ', '14 Отряд', '142 ПЧ') order by d.leftnum")
                        .list();
                assertEquals(52, insertedDepts.get(0).getLeftNum().longValue());
                assertEquals(53, insertedDepts.get(0).getRightNum().longValue());
                assertEquals(60, insertedDepts.get(1).getLeftNum().longValue());
                assertEquals(75, insertedDepts.get(1).getRightNum().longValue());
                assertEquals(64, insertedDepts.get(2).getLeftNum().longValue());
                assertEquals(65, insertedDepts.get(2).getRightNum().longValue());
                assertEquals(76, insertedDepts.get(3).getLeftNum().longValue());
                assertEquals(105, insertedDepts.get(3).getRightNum().longValue());
                assertEquals(89, insertedDepts.get(4).getLeftNum().longValue());
                assertEquals(90, insertedDepts.get(4).getRightNum().longValue());
                return null;
            }
        });
    }

    @Test
    public void testDeleteNode() throws LogicException, ClientAuthException {
        try {
            nsm.deleteNode(2L, false);
            fail();
        } catch (NestedSetManagerException e) {
            assertEquals("Need to delete more than one node but children deleting was explicitly prohibited.", e.getMessage());
        }
        List<Dept> depts = nsm.getChildren(1L, "id", false);
        nsm.deleteNode(2L, true);
        depts = nsm.getChildren(1L, "id", false);
        assertEquals(2, depts.get(0).getLeftNum().longValue());
        assertEquals(7, depts.get(0).getRightNum().longValue());
        assertEquals(3, depts.get(1).getLeftNum().longValue());
        assertEquals(6, depts.get(1).getRightNum().longValue());
        assertEquals(4, depts.get(2).getLeftNum().longValue());
        assertEquals(5, depts.get(2).getRightNum().longValue());
        init();
        nsm.deleteNode(3L, true);
        depts = nsm.getChildren(1L, "id", false);
        assertEquals(2, depts.get(0).getLeftNum().longValue());
        assertEquals(3, depts.get(0).getRightNum().longValue());
        assertEquals(4, depts.get(1).getLeftNum().longValue());
        assertEquals(9, depts.get(1).getRightNum().longValue());
        assertEquals(5, depts.get(2).getLeftNum().longValue());
        assertEquals(8, depts.get(2).getRightNum().longValue());
        assertEquals(6, depts.get(3).getLeftNum().longValue());
        assertEquals(7, depts.get(3).getRightNum().longValue());
        init();
        nsm.deleteNode(5L, true);
        depts = nsm.getChildren(1L, "id", false);
        assertEquals(2, depts.get(0).getLeftNum().longValue());
        assertEquals(5, depts.get(0).getRightNum().longValue());
        assertEquals(3, depts.get(1).getLeftNum().longValue());
        assertEquals(4, depts.get(1).getRightNum().longValue());
        assertEquals(6, depts.get(2).getLeftNum().longValue());
        assertEquals(7, depts.get(2).getRightNum().longValue());
    }

    @Test
    public void testDeleteNodeNG() throws LogicException, ClientAuthException {
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthException {
                NestedSetManagerNG<DeptNG> nsmNG = new NestedSetManagerNG<>(DeptNG.class, session, lock);
                try {
                    nsmNG.deleteNode(2L, false);
                    fail();
                } catch (NestedSetManagerException e) {
                    assertEquals("Need to delete more than one node but children deleting was explicitly prohibited.", e.getMessage());
                }
                List<DeptNG> depts = nsmNG.getChildren(1L, "id", false);
                nsmNG.deleteNode(2L, true);
                session.clear();
                depts = nsmNG.getChildren(1L, "id", false);
                assertEquals(2, depts.get(0).getLeftNum().longValue());
                assertEquals(7, depts.get(0).getRightNum().longValue());
                assertEquals(3, depts.get(1).getLeftNum().longValue());
                assertEquals(6, depts.get(1).getRightNum().longValue());
                assertEquals(4, depts.get(2).getLeftNum().longValue());
                assertEquals(5, depts.get(2).getRightNum().longValue());
                return null;
            }
        });
        initNG();
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthException {
                NestedSetManagerNG<DeptNG> nsmNG = new NestedSetManagerNG<>(DeptNG.class, session, lock);
                DeptNG rootNode = nsmNG.getRootNode();
                assertEquals(1L, rootNode.getLeftNum().longValue());
                assertEquals(12L, rootNode.getRightNum().longValue());
                nsmNG.deleteNode(3L, true);
                session.clear();
                List<DeptNG> depts = nsmNG.getChildren(1L, "id", false);
                assertEquals(2, depts.get(0).getLeftNum().longValue());
                assertEquals(3, depts.get(0).getRightNum().longValue());
                assertEquals(4, depts.get(1).getLeftNum().longValue());
                assertEquals(9, depts.get(1).getRightNum().longValue());
                assertEquals(5, depts.get(2).getLeftNum().longValue());
                assertEquals(8, depts.get(2).getRightNum().longValue());
                assertEquals(6, depts.get(3).getLeftNum().longValue());
                assertEquals(7, depts.get(3).getRightNum().longValue());
                return null;
            }
        });
        initNG();
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthException {
                NestedSetManagerNG<DeptNG> nsmNG = new NestedSetManagerNG<>(DeptNG.class, session, lock);
                nsmNG.deleteNode(5L, true);
                session.clear();
                List<DeptNG> depts = nsmNG.getChildren(1L, "id", false);
                assertEquals(2, depts.get(0).getLeftNum().longValue());
                assertEquals(5, depts.get(0).getRightNum().longValue());
                assertEquals(3, depts.get(1).getLeftNum().longValue());
                assertEquals(4, depts.get(1).getRightNum().longValue());
                assertEquals(6, depts.get(2).getLeftNum().longValue());
                assertEquals(7, depts.get(2).getRightNum().longValue());
                return null;
            }
        });
    }

    @Test
    public void testMoveNG() throws LogicException, ClientAuthException {
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthException {
                NestedSetManagerNG<DeptNG> nsmNG = new NestedSetManagerNG<>(DeptNG.class, session, lock);
                List<DeptNG> depts = nsmNG.getChildren(1L, "id", false);
                System.out.println("Before: " + depts);
                nsmNG.move(5L, 2L); // move to sq11
                session.clear();
                depts = nsmNG.getChildren(1L, "id", false);
                System.out.println("After: " + depts);
                assertEquals(2, depts.get(0).getLeftNum().longValue());
                assertEquals(9, depts.get(0).getRightNum().longValue());
                assertEquals(1, depts.get(0).getDepth().longValue());

                assertEquals(3, depts.get(1).getLeftNum().longValue());
                assertEquals(4, depts.get(1).getRightNum().longValue());
                assertEquals(2, depts.get(1).getDepth().longValue());

                assertEquals(10, depts.get(2).getLeftNum().longValue());
                assertEquals(11, depts.get(2).getRightNum().longValue());
                assertEquals(1, depts.get(2).getDepth().longValue());

                assertEquals(5, depts.get(3).getLeftNum().longValue());
                assertEquals(8, depts.get(3).getRightNum().longValue());
                assertEquals(2, depts.get(3).getDepth().longValue());

                assertEquals(6, depts.get(4).getLeftNum().longValue());
                assertEquals(7, depts.get(4).getRightNum().longValue());
                assertEquals(3, depts.get(4).getDepth().longValue());
                return null;
            }
        });

    }

    @Test
    public void testMoveNGToChild() throws LogicException, ClientAuthException {
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthException {
                NestedSetManagerNG<DeptNG> nsmNG = new NestedSetManagerNG<>(DeptNG.class, session, lock);
                try {
                    nsmNG.move(2L, 4L); // move to sq11
                } catch (NestedSetManagerException e) {
                    assertEquals("Can't move node to its own child or itself.", e.getMessage());
                }
                return null;
            }
        });
    }

    @Test
    public void testMoveNGToItself() throws LogicException, ClientAuthException {
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthException {
                NestedSetManagerNG<DeptNG> nsmNG = new NestedSetManagerNG<>(DeptNG.class, session, lock);
                try {
                    nsmNG.move(2L, 2L);
                } catch (NestedSetManagerException e) {
                    assertEquals("Can't move node to its own child or itself.", e.getMessage());
                }
                return null;
            }
        });
    }

    @Test
    public void testMoveNG2() throws LogicException, ClientAuthException {
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthException {
                NestedSetManagerNG<DeptNG> nsmNG = new NestedSetManagerNG<>(DeptNG.class, session, lock);
                List<DeptNG> depts = nsmNG.getChildren(1L, "id", false);
                System.out.println("Before: " + depts);
                nsmNG.move(2L, 4L); // move to sq12
                session.clear();
                depts = nsmNG.getChildren(1L, "id", false);
                System.out.println("After: " + depts);
                assertEquals(7, depts.get(0).getLeftNum().longValue());
                assertEquals(10, depts.get(0).getRightNum().longValue());
                assertEquals(2, depts.get(0).getDepth().longValue());

                assertEquals(8, depts.get(1).getLeftNum().longValue());
                assertEquals(9, depts.get(1).getRightNum().longValue());
                assertEquals(3, depts.get(1).getDepth().longValue());

                assertEquals(2, depts.get(2).getLeftNum().longValue());
                assertEquals(11, depts.get(2).getRightNum().longValue());
                assertEquals(1, depts.get(2).getDepth().longValue());

                assertEquals(3, depts.get(3).getLeftNum().longValue());
                assertEquals(6, depts.get(3).getRightNum().longValue());
                assertEquals(2, depts.get(3).getDepth().longValue());

                assertEquals(4, depts.get(4).getLeftNum().longValue());
                assertEquals(5, depts.get(4).getRightNum().longValue());
                assertEquals(3, depts.get(4).getDepth().longValue());
                return null;
            }
        });
    }

    @Test
    public void testMoveNG3() throws LogicException, ClientAuthException {
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthException {
                NestedSetManagerNG<DeptNG> nsmNG = new NestedSetManagerNG<>(DeptNG.class, session, lock);
                List<DeptNG> depts = nsmNG.getChildren(1L, "id", false);
                System.out.println("Before: " + depts);
                nsmNG.move(6L, 1L); // move to root
                session.clear();
                depts = nsmNG.getChildren(1L, "id", false);
                System.out.println("After: " + depts);
                assertEquals(2, depts.get(0).getLeftNum().longValue());
                assertEquals(5, depts.get(0).getRightNum().longValue());
                assertEquals(1, depts.get(0).getDepth().longValue());

                assertEquals(3, depts.get(1).getLeftNum().longValue());
                assertEquals(4, depts.get(1).getRightNum().longValue());
                assertEquals(2, depts.get(1).getDepth().longValue());

                assertEquals(6, depts.get(2).getLeftNum().longValue());
                assertEquals(9, depts.get(2).getRightNum().longValue());
                assertEquals(1, depts.get(2).getDepth().longValue());

                assertEquals(7, depts.get(3).getLeftNum().longValue());
                assertEquals(8, depts.get(3).getRightNum().longValue());
                assertEquals(2, depts.get(3).getDepth().longValue());

                assertEquals(10, depts.get(4).getLeftNum().longValue());
                assertEquals(11, depts.get(4).getRightNum().longValue());
                assertEquals(1, depts.get(4).getDepth().longValue());
                return null;
            }
        });
    }
}
