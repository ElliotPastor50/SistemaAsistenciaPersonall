/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dao.exceptions.NonexistentEntityException;
import dto.Empleado;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Evento;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author Naomi Alejandra Vega
 */
public class EmpleadoJpaController implements Serializable {

    public EmpleadoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaAsistenciaPersonal_war_1.0-SNAPSHOTPU");

    public EmpleadoJpaController() {
        
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Empleado empleado) {
        if (empleado.getEventoCollection() == null) {
            empleado.setEventoCollection(new ArrayList<Evento>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Evento> attachedEventoCollection = new ArrayList<Evento>();
            for (Evento eventoCollectionEventoToAttach : empleado.getEventoCollection()) {
                eventoCollectionEventoToAttach = em.getReference(eventoCollectionEventoToAttach.getClass(), eventoCollectionEventoToAttach.getIdEvento());
                attachedEventoCollection.add(eventoCollectionEventoToAttach);
            }
            empleado.setEventoCollection(attachedEventoCollection);
            em.persist(empleado);
            for (Evento eventoCollectionEvento : empleado.getEventoCollection()) {
                Empleado oldIdEmpleadoOfEventoCollectionEvento = eventoCollectionEvento.getIdEmpleado();
                eventoCollectionEvento.setIdEmpleado(empleado);
                eventoCollectionEvento = em.merge(eventoCollectionEvento);
                if (oldIdEmpleadoOfEventoCollectionEvento != null) {
                    oldIdEmpleadoOfEventoCollectionEvento.getEventoCollection().remove(eventoCollectionEvento);
                    oldIdEmpleadoOfEventoCollectionEvento = em.merge(oldIdEmpleadoOfEventoCollectionEvento);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Empleado empleado) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Empleado persistentEmpleado = em.find(Empleado.class, empleado.getIdEmpleado());
            Collection<Evento> eventoCollectionOld = persistentEmpleado.getEventoCollection();
            Collection<Evento> eventoCollectionNew = empleado.getEventoCollection();
            Collection<Evento> attachedEventoCollectionNew = new ArrayList<Evento>();
            for (Evento eventoCollectionNewEventoToAttach : eventoCollectionNew) {
                eventoCollectionNewEventoToAttach = em.getReference(eventoCollectionNewEventoToAttach.getClass(), eventoCollectionNewEventoToAttach.getIdEvento());
                attachedEventoCollectionNew.add(eventoCollectionNewEventoToAttach);
            }
            eventoCollectionNew = attachedEventoCollectionNew;
            empleado.setEventoCollection(eventoCollectionNew);
            empleado = em.merge(empleado);
            for (Evento eventoCollectionOldEvento : eventoCollectionOld) {
                if (!eventoCollectionNew.contains(eventoCollectionOldEvento)) {
                    eventoCollectionOldEvento.setIdEmpleado(null);
                    eventoCollectionOldEvento = em.merge(eventoCollectionOldEvento);
                }
            }
            for (Evento eventoCollectionNewEvento : eventoCollectionNew) {
                if (!eventoCollectionOld.contains(eventoCollectionNewEvento)) {
                    Empleado oldIdEmpleadoOfEventoCollectionNewEvento = eventoCollectionNewEvento.getIdEmpleado();
                    eventoCollectionNewEvento.setIdEmpleado(empleado);
                    eventoCollectionNewEvento = em.merge(eventoCollectionNewEvento);
                    if (oldIdEmpleadoOfEventoCollectionNewEvento != null && !oldIdEmpleadoOfEventoCollectionNewEvento.equals(empleado)) {
                        oldIdEmpleadoOfEventoCollectionNewEvento.getEventoCollection().remove(eventoCollectionNewEvento);
                        oldIdEmpleadoOfEventoCollectionNewEvento = em.merge(oldIdEmpleadoOfEventoCollectionNewEvento);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = empleado.getIdEmpleado();
                if (findEmpleado(id) == null) {
                    throw new NonexistentEntityException("The empleado with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Empleado empleado;
            try {
                empleado = em.getReference(Empleado.class, id);
                empleado.getIdEmpleado();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The empleado with id " + id + " no longer exists.", enfe);
            }
            Collection<Evento> eventoCollection = empleado.getEventoCollection();
            for (Evento eventoCollectionEvento : eventoCollection) {
                eventoCollectionEvento.setIdEmpleado(null);
                eventoCollectionEvento = em.merge(eventoCollectionEvento);
            }
            em.remove(empleado);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Empleado> findEmpleadoEntities() {
        return findEmpleadoEntities(true, -1, -1);
    }

    public List<Empleado> findEmpleadoEntities(int maxResults, int firstResult) {
        return findEmpleadoEntities(false, maxResults, firstResult);
    }

    private List<Empleado> findEmpleadoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Empleado.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Empleado findEmpleado(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Empleado.class, id);
        } finally {
            em.close();
        }
    }

    public int getEmpleadoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Empleado> rt = cq.from(Empleado.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
