package ru.ppsrk.gwt.server.nestedset;

import static ru.ppsrk.gwt.server.ServerUtils.*;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.client.NestedSetManagerException;
import ru.ppsrk.gwt.domain.NestedSetNodeNG;
import ru.ppsrk.gwt.server.ServerUtils;

public class NestedSetManagerNG<T extends NestedSetNodeNG> {

    private static final String LEFT = "left";
    private static final String RIGHTNUM = "rightnum";
    private static final String RIGHT = "right";
    private static final String LEFTNUM = "leftnum";
    private static final String DEPTH = "depth";
    private static final String SHIFT = "shift";
    private static final String UPDATE = "update ";
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Class<T> entityClass;
    private String entityName;
    private final Object lock;
    private Session session;

    public enum AnnotateChildren {
        NONE, DIRECT, RECURSIVE, BOTH
    }

    public NestedSetManagerNG(Class<T> entityClass, Session session, Object lock) {
        this.lock = lock;
        this.session = session;
        this.entityClass = entityClass;
        entityName = entityClass.getSimpleName();
    }

    private void getByParentIdAndInsert(List<? extends T> entities, Long rootId, Long parentNodeId) throws GwtUtilException {
        LinkedList<T> selectedChildren = new LinkedList<>();
        for (T T : entities) {
            if (T.getParent() == null && rootId != null && rootId.equals(0L)
                    || T.getParent() != null && T.getParent().getId().equals(rootId)) {
                selectedChildren.add(T);
            }
        }
        log.debug("Selected for rootId={}: {}", rootId, selectedChildren);
        for (T T : selectedChildren) {
            T newNode = ServerUtils.mapModel(T, entityClass);
            newNode.setId(null);
            T inserted = insertNode(newNode, parentNodeId);
            log.debug("Inserted as: {}", inserted);
            getByParentIdAndInsert(entities, T.getId(), inserted.getId());
        }
    }

    /**
     * Retrieves children by parent node id.
     * 
     * @param parentId
     *            use null for the root node
     * @param orderField
     *            field name by which the results are sorted; several field may be supplied, delimited by comma
     * @param directOnly
     *            retrieve only direct descendants
     * @return list of children nodes
     * @throws LogicException
     * @throws ClientAuthException
     */
    public List<T> getChildrenByParentId(final Long parentId, final String orderField, final boolean directOnly) throws GwtUtilException {
        return getChildrenByParentId(parentId, orderField, directOnly, AnnotateChildren.NONE);
    }

    @SuppressWarnings("unchecked")
    public List<T> getChildrenByParentId(final Long parentId, final String orderField, final boolean directOnly,
            final AnnotateChildren annotateChildren) throws GwtUtilException {
        T parentNode = (T) session.get(entityClass, ensureParentId(parentId));
        if (directOnly) {
            session.enableFilter("depthFilter").setParameter(DEPTH, parentNode.getDepth() + 1);
        }
        List<T> entities = session.createCriteria(entityClass).add(Restrictions.gt(LEFTNUM, parentNode.getLeftNum()))
                .add(Restrictions.lt(RIGHTNUM, parentNode.getRightNum())).addOrder(Order.asc(orderField)).list();
        switch (annotateChildren) {
        case DIRECT:
            annotateChildrenCount(entities, true);
            break;
        case RECURSIVE:
            annotateChildrenCount(entities, false);
            break;
        case BOTH:
            annotateChildrenCount(entities, true);
            annotateChildrenCount(entities, false);
            break;
        default:
            break;
        }
        return entities;
    }

    @SuppressWarnings("unchecked")
    public Long getChildrenCount(final Long parentNodeId, final boolean directOnly) {
        T parentNode = (T) session.get(entityClass, parentNodeId);
        if (directOnly) {
            session.enableFilter("depthFilter").setParameter(DEPTH, parentNode.getDepth() + 1);
        }
        return (Long) session.createCriteria(entityClass).add(Restrictions.gt(LEFTNUM, parentNode.getLeftNum()))
                .add(Restrictions.lt(RIGHTNUM, parentNode.getRightNum())).setProjection(Projections.rowCount()).uniqueResult();
    }

    public void annotateChildrenCount(List<T> entities, boolean directOnly) {
        for (T entity : entities) {
            Long childrenCount = getChildrenCount(entity.getId(), directOnly);
            if (directOnly) {
                entity.setDirectChildrenCount(childrenCount);
            } else {
                entity.setChildrenCount(childrenCount);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public T getNodeById(final Long id) throws GwtUtilException {
        T entity = (T) session.get(entityClass, id);
        if (entity == null) {
            throw new LogicException("No entity of class " + entityClass + " and id " + id);
        }
        return mapModel(entity, entityClass);
    }

    public List<T> getChildrenByParentId(Long parentId, AnnotateChildren annotateChildren) throws GwtUtilException {
        return getChildrenByParent(getNodeById(ensureParentId(parentId)), annotateChildren);
    }

    public List<T> getChildrenByParent(T parent) throws GwtUtilException {
        return getChildrenByParent(parent, AnnotateChildren.NONE);
    }

    public List<T> getChildrenByParent(T parent, AnnotateChildren annotateChildren) throws GwtUtilException {
        return getChildrenByParent(parent, "name", annotateChildren);
    }

    public List<T> getChildrenByParentId(Long parentId, String orderField, AnnotateChildren annotateChildren) throws GwtUtilException {
        return getChildrenByParent(getNodeById(ensureParentId(parentId)), orderField, annotateChildren);
    }

    public List<T> getChildrenByParent(T parent, String orderField) throws GwtUtilException {
        return getChildrenByParent(parent, orderField, AnnotateChildren.NONE);
    }

    public List<T> getChildrenByParent(final T parent, final String orderField, final AnnotateChildren annotateChildren)
            throws GwtUtilException {

        Long parentId = getId(parent, false);
        List<T> dtos = mapArray(getChildrenByParentId(parentId, orderField, true, annotateChildren), entityClass);
        for (T dto : dtos) {
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
    @SuppressWarnings("unchecked")
    public T getParentByChild(final Long childId, final Long depth) throws GwtUtilException {
        T childNode = (T) session.get(entityClass, childId);
        Long parentDepth = null;
        if (depth < 0) {
            parentDepth = childNode.getDepth() + depth;
            if (parentDepth < 0) {
                throw new NestedSetManagerException(
                        "parentDepth < 0, childDepth = " + childNode.getDepth() + " requested depth = " + depth);
            }
        } else {
            parentDepth = depth;
        }
        try {
            return (T) session.createCriteria(entityClass).add(Restrictions.le(LEFTNUM, childNode.getLeftNum()))
                    .add(Restrictions.ge(RIGHTNUM, childNode.getRightNum())).add(Restrictions.eq(DEPTH, parentDepth)).uniqueResult();
        } catch (NonUniqueResultException e) {
            throw new NestedSetManagerException("Non-unique parent: " + e.getMessage());
        }
    }

    public Long getId(T parent, boolean getParent) throws GwtUtilException {
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

    @SuppressWarnings("unchecked")
    public T getRootNode() throws GwtUtilException {
        try {
            return (T) session.createCriteria(entityClass).add(Restrictions.eq(LEFTNUM, 1L)).uniqueResult();
        } catch (NonUniqueResultException e) {
            throw new LogicException("Duplicate root entries, DB is corrupted.");
        }
    }

    public void insertNode(final List<? extends T> entities, final Long parentNodeId) throws GwtUtilException {
        getByParentIdAndInsert(entities, 0L, parentNodeId);
    }

    @SuppressWarnings("unchecked")
    public T insertNode(final T node, Long parentId) throws GwtUtilException {
        synchronized (lock) {
            final Long sureParentId = ensureParentId(parentId);
            T parentNode = (T) session.get(entityClass, sureParentId);
            if (parentNode == null) {
                throw new NestedSetManagerException("Parent node with id=" + sureParentId + " not found.");
            }
            session.refresh(parentNode);
            log.debug("Insert; parent node: {} new node: {} to parentNodeId: {}", parentNode, node, sureParentId);
            node.setLeftNum(parentNode.getRightNum());
            node.setRightNum(node.getLeftNum() + 1);
            node.setDepth(parentNode.getDepth() + 1);
            updateNodes(node.getLeftNum(), 2L);
            return (T) session.merge(node);
        }
    }

    @SuppressWarnings("unchecked")
    public T insertRootNode(final T node) {
        synchronized (lock) {
            node.setLeftNum(1L);
            node.setRightNum(2L);
            node.setDepth(0L);
            return (T) session.merge(node);
        }
    }

    public T saveDTONode(T dto) throws GwtUtilException {
        Long parentId = getId(dto, true);
        return mapModel(insertNode(mapModel(dto, entityClass), parentId), entityClass);
    }

    private void updateNodes(Long left, Long shift) {
        synchronized (lock) {
            session.createQuery(UPDATE + entityName + " node set node.leftnum = node.leftnum + :shift where node.leftnum >= :left")
                    .setLong(LEFT, left).setLong(SHIFT, shift).executeUpdate();
            session.createQuery(UPDATE + entityName + " node set node.rightnum = node.rightnum + :shift where node.rightnum >= :left")
                    .setLong(LEFT, left).setLong(SHIFT, shift).executeUpdate();
        }
    }

    private void shiftNodes(Long left, Long right, Long shift) {
        synchronized (lock) {
            session.createQuery(
                    UPDATE + entityName + " node set node.leftnum = node.leftnum + :shift, node.rightnum = node.rightnum + :shift where "
                            + "node.leftnum >= :left and node.rightnum <= :right")
                    .setLong(LEFT, left).setLong(RIGHT, right).setLong(SHIFT, shift).executeUpdate();
        }
    }

    private Long ensureParentId(Long parentId) throws GwtUtilException {
        if (parentId == null) {
            T rootNode = getRootNode();
            if (rootNode == null) {
                try {
                    rootNode = insertRootNode(entityClass.newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new LogicException("Can't instantiate new entity.");
                }
            }
            parentId = rootNode.getId();
        }
        return parentId;
    }

    @SuppressWarnings("unchecked")
    public void deleteNode(final Long nodeId, final boolean withChildren) throws NestedSetManagerException {
        synchronized (lock) {
            T node = (T) session.get(entityClass, nodeId);
            if (node.getRightNum() - node.getLeftNum() > 1 && !withChildren) {
                throw new NestedSetManagerException("Need to delete more than one node but children deleting was explicitly prohibited.");
            }
            session.createQuery("delete from " + entityName + " node where node.leftnum >= :left and node.rightnum <= :right")
                    .setLong(LEFT, node.getLeftNum()).setLong(RIGHT, node.getRightNum()).executeUpdate();
            updateNodes(node.getLeftNum(), node.getLeftNum() - node.getRightNum() - 1);
        }
    }

    public void move(Long nodeId, Long newParentId) throws GwtUtilException {
        synchronized (lock) {
            T node = getNodeById(nodeId);
            T parentNode = getNodeById(newParentId);
            if (parentNode.getLeftNum() >= node.getLeftNum() && parentNode.getRightNum() <= node.getRightNum()) {
                throw new NestedSetManagerException("Can't move node to its own child or itself.");
            }
            long width = node.getRightNum() - node.getLeftNum() + 1;
            long depthDiff = parentNode.getDepth() - node.getDepth() + 1;
            for (T child : getChildrenByParentId(node.getId(), "id", false)) {
                child.setDepth(child.getDepth() + depthDiff);
            }
            node.setDepth(node.getDepth() + depthDiff);
            session.merge(node);
            updateNodes(parentNode.getRightNum(), width); // expand parent
            session.flush();
            session.clear();
            session.refresh(node);
            session.refresh(parentNode);
            long moveDistance = parentNode.getRightNum() - node.getRightNum() - 1;
            shiftNodes(node.getLeftNum(), node.getRightNum(), moveDistance); // move nodes
            updateNodes(node.getLeftNum() + 1, -width); // collapse empty space
        }
    }

}