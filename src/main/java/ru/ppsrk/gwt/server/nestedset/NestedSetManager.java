package ru.ppsrk.gwt.server.nestedset;

import static ru.ppsrk.gwt.server.ServerUtils.*;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.Hierarchic;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.client.NestedSetManagerException;
import ru.ppsrk.gwt.client.SettableParent;
import ru.ppsrk.gwt.server.HibernateCallback;
import ru.ppsrk.gwt.server.HibernateUtil;
import ru.ppsrk.gwt.server.ServerUtils;

public class NestedSetManager<T extends NestedSetNode, D extends SettableParent> {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Class<T> entityClass;
    private Class<D> dtoClass;
    private String entityName;

    public NestedSetManager(Class<T> entityClass, Class<D> dtoClass) {
        super();
        this.entityClass = entityClass;
        this.dtoClass = dtoClass;
        entityName = entityClass.getSimpleName();
    }

    public void deleteNode(final Long nodeId, final boolean withChildren) throws LogicException, ClientAuthException {
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthException {
                @SuppressWarnings("unchecked")
                T node = (T) session.get(entityClass, nodeId);
                if (node.getRightNum() - node.getLeftNum() > 1 && !withChildren) {
                    throw new NestedSetManagerException(
                            "Need to delete more than one node but children deleting was explicitly prohibited.");
                }
                session.createQuery("delete from " + entityName + " node where node.leftnum >= :left and node.rightnum <= :right")
                        .setLong("left", node.getLeftNum()).setLong("right", node.getRightNum()).executeUpdate();
                updateNodes(node.getLeftNum(), node.getLeftNum() - node.getRightNum() - 1, session);
                return null;
            }
        });
    }

    private void getByParentIdAndInsert(List<? extends Hierarchic> hierarchics, Long hierarchicRootId, Long parentNodeId, Session session)
            throws LogicException, ClientAuthException {
        LinkedList<Hierarchic> selectedChildren = new LinkedList<Hierarchic>();
        for (Hierarchic hierarchic : hierarchics) {
            if (hierarchic.getParent() == null && hierarchicRootId != null && hierarchicRootId.equals(0L) || hierarchic.getParent() != null
                    && hierarchic.getParent().getId().equals(hierarchicRootId)) {
                selectedChildren.add(hierarchic);
            }
        }
        log.debug("Selected for rootId=" + hierarchicRootId + ": " + selectedChildren);
        for (Hierarchic hierarchic : selectedChildren) {
            T newNode = ServerUtils.mapModel(hierarchic, entityClass);
            newNode.setId(null);
            T inserted = insertNode(newNode, parentNodeId);
            log.debug("Inserted as: " + inserted);
            getByParentIdAndInsert(hierarchics, hierarchic.getId(), inserted.getId(), session);
        }
    }

    /**
     * Retrieves children by parent node id.
     * 
     * @param parentNodeId
     *            use null for the root node
     * @param orderField
     *            field name by which the results are sorted; several field may be supplied, delimited by comma
     * @param directOnly
     *            retrieve only direct descendants
     * @return list of children nodes
     * @throws LogicException
     * @throws ClientAuthException
     */
    public List<T> getChildren(final Long parentNodeId, final String orderField, final boolean directOnly) throws LogicException,
            ClientAuthException {
        return HibernateUtil.exec(new HibernateCallback<List<T>>() {

            @SuppressWarnings("unchecked")
            @Override
            public List<T> run(Session session) throws LogicException, ClientAuthException {
                T parentNode = (T) session.get(entityClass, parentNodeId);
                if (directOnly) {
                    session.enableFilter("depthFilter").setParameter("depth", parentNode.getDepth() + 1);
                }
                return session
                        .createQuery(
                                "from " + entityName + " node where node.leftnum > :left and node.rightnum < :right order by node."
                                        + orderField).setLong("left", parentNode.getLeftNum()).setLong("right", parentNode.getRightNum())
                        .list();
            }
        });
    }

    public List<D> getHierarchicByParent(D parent) throws LogicException, ClientAuthException {
        return getHierarchicByParent(parent, "name");
    }

    public List<D> getHierarchicByParent(D parent, String orderField) throws LogicException, ClientAuthException {
        Long parentId = getId(parent, false);
        List<D> dtos = mapArray(getChildren(parentId, orderField, true), dtoClass);
        for (D dto : dtos) {
            dto.setParent(parent);
        }
        return dtos;
    }

    /**
     * Retrieves parent node by child node id.
     * 
     * @param childId
     * @param depth
     *            negative values mean parent by Math.abs(depth) levels above, non-negative values mean absolute depth.
     * @return parent node
     * @throws LogicException
     * @throws ClientAuthException
     */
    public T getParentByChild(final Long childId, final Long depth) throws LogicException, ClientAuthException {
        return HibernateUtil.exec(new HibernateCallback<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T run(Session session) throws LogicException, ClientAuthException {
                T childNode = (T) session.get(entityClass, childId);
                Long parentDepth = null;
                if (depth < 0) {
                    parentDepth = childNode.getDepth() + depth;
                    if (parentDepth < 0) {
                        throw new NestedSetManagerException("parentDepth < 0, childDepth = " + childNode.getDepth() + " requested depth = "
                                + depth);
                    }
                } else {
                    parentDepth = depth;
                }
                try {
                    return (T) session
                            .createQuery(
                                    "from " + entityName
                                            + " node where node.leftnum <= :left and node.rightnum >= :right and depth = :depth")
                            .setLong("left", childNode.getLeftNum()).setLong("right", childNode.getRightNum())
                            .setLong("depth", parentDepth).uniqueResult();
                } catch (NonUniqueResultException e) {
                    throw new NestedSetManagerException("Non-unique parent: " + e.getMessage());
                }
            }
        });
    }

    public Long getId(D parent, boolean getParent) throws LogicException, ClientAuthException {
        if (parent != null) {
            if (getParent) {
                if (parent.getParent() != null) {
                    return parent.getParent().getId();
                }
            } else {
                return parent.getId();
            }
        }
        T rootNode = getRootNode();
        if (rootNode == null) {
            rootNode = insertRootNode(mapModel(parent, entityClass));
        }
        return rootNode.getId();
    }

    public T getRootNode() throws LogicException, ClientAuthException {
        return HibernateUtil.exec(new HibernateCallback<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T run(Session session) throws LogicException, ClientAuthException {
                try {
                    return (T) session.createQuery("from " + entityName + " where leftnum = 1").uniqueResult();
                } catch (NonUniqueResultException e) {
                    throw new LogicException("Duplicate root entries, DB is corrupted.");
                }
            }
        });
    }

    public void insertHierarchic(final List<? extends Hierarchic> hierarchics, final Long parentNodeId) throws LogicException,
            ClientAuthException {
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthException {
                getByParentIdAndInsert(hierarchics, 0L, parentNodeId, session);
                return null;
            }
        });
    }

    public T insertNode(final T node, final Long parentId) throws LogicException, ClientAuthException {
        return HibernateUtil.exec(new HibernateCallback<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T run(Session session) throws LogicException, ClientAuthException {
                if (parentId == null) {
                    throw new NestedSetManagerException("Parent can't be null. Insert a root node if you have no records yet.");
                }
                T parentNode = (T) session.get(entityClass, parentId);
                if (parentNode == null) {
                    throw new NestedSetManagerException("Parent node with id=" + parentId + " not found.");
                }
                log.debug("Insert; parent node: " + parentNode + " new node: " + node + " to parentNodeId: " + parentId);
                node.setLeftNum(parentNode.getRightNum());
                node.setRightNum(node.getLeftNum() + 1);
                node.setDepth(parentNode.getDepth() + 1);
                updateNodes(node.getLeftNum(), 2L, session);
                return (T) session.merge(node);
            }
        });
    }

    public T insertRootNode(final T node) throws LogicException, ClientAuthException {
        return HibernateUtil.exec(new HibernateCallback<T>() {

            @Override
            public T run(Session session) throws LogicException, ClientAuthException {
                node.setLeftNum(1L);
                node.setRightNum(2L);
                node.setDepth(0L);
                Long id = (Long) session.save(node.getClass().getSimpleName(), node);
                node.setId(id);
                return node;
            }
        });
    }

    public D saveDTONode(D dto) throws LogicException, ClientAuthException {
        Long parentId = getId(dto, true);
        return mapModel(insertNode(mapModel(dto, entityClass), parentId), dtoClass);
    }

    private void updateNodes(Long left, Long shift, Session session) {
        session.createQuery("update " + entityName + " node set node.leftnum = node.leftnum + :shift where node.leftnum >= :left")
                .setLong("left", left).setLong("shift", shift).executeUpdate();
        session.createQuery("update " + entityName + " node set node.rightnum = node.rightnum + :shift where node.rightnum >= :left")
                .setLong("left", left).setLong("shift", shift).executeUpdate();
    }

}
