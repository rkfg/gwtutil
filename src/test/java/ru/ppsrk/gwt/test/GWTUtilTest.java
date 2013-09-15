package ru.ppsrk.gwt.test;

import static org.junit.Assert.*;
import static ru.ppsrk.gwt.server.ServerUtils.*;

import java.util.List;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.client.NestedSetManagerException;
import ru.ppsrk.gwt.server.HibernateCallback;
import ru.ppsrk.gwt.server.HibernateUtil;
import ru.ppsrk.gwt.server.ServerUtils;
import ru.ppsrk.gwt.server.nestedset.NestedSetManager;
import ru.ppsrk.gwt.test.domain.Dept;
import ru.ppsrk.gwt.test.domain.DeptHier;
import ru.ppsrk.gwt.test.dto.DeptHierDTO;

public class GWTUtilTest {
    NestedSetManager<Dept> nsm = new NestedSetManager<Dept>(Dept.class);

    @BeforeClass
    public static void login() throws LogicException, ClientAuthenticationException {
        HibernateUtil.initSessionFactory("hibernate.gwtutil_testmem.cfg.xml");
        ServerUtils.importSQL("depthier.sql");

    }

    @Before
    public void init() throws LogicException, ClientAuthenticationException {
        ServerUtils.resetTables(new String[] { "terrdepts" });
        Dept rootNode = nsm.insertRootNode(new Dept());
        Dept sq11 = nsm.insertNode(new Dept("11 Отряд", "Краснозатонский"), rootNode.getId());
        nsm.insertNode(new Dept("111 ПЧ", "Краснозатонский"), sq11.getId());
        Dept sq12 = nsm.insertNode(new Dept("12 Отряд", "Микунь"), rootNode.getId());
        Dept pch121 = nsm.insertNode(new Dept("121 ПЧ", "Микунь"), sq12.getId());
        nsm.insertNode(new Dept("1 ОП 121 ПЧ", "Кожмудор"), pch121.getId());
    }

    @Test
    public void testNestedSetInsert() throws LogicException, ClientAuthenticationException {
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
    public void testChildrenByParent() throws LogicException, ClientAuthenticationException {
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
    public void testParentByChild() throws LogicException, ClientAuthenticationException {
        Dept parent = nsm.getParentByChild(6L, 0L);
        assertEquals(1L, parent.getId().longValue());
        parent = nsm.getParentByChild(6L, -1L);
        assertEquals("121 ПЧ", parent.getName());
        parent = nsm.getParentByChild(6L, -2L);
        assertEquals("12 Отряд", parent.getName());
    }

    @Test
    public void testImportHier() throws LogicException, ClientAuthenticationException {
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthenticationException {
                @SuppressWarnings("unchecked")
                List<DeptHier> depts = session.createQuery("from DeptHier d where d.name != 'Default' order by d.id").list();
                nsm.insertHierarchic(mapArray(depts, DeptHierDTO.class), 1L);
                @SuppressWarnings("unchecked")
                List<Dept> insertedDepts = session.createQuery(
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
    public void testDeleteNode() throws LogicException, ClientAuthenticationException {
        try {
            nsm.deleteNode(2L, false);
            fail();
        } catch (NestedSetManagerException e) {
            assertEquals("Need to delete more than one node but children deleting was explicitly prohibited.", e.getMessage());
        }
        nsm.deleteNode(2L, true);
        List<Dept> depts = nsm.getChildren(1L, "id", false);
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
}
