/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dao.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Evento;
import dto.Oficina;
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
public class OficinaJpaController implements Serializable {
    
    public OficinaJpaController(){
    }
    public OficinaJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf= Persistence.createEntityManagerFactory("com.mycompany_SistemaAsistenciaPersonal_war_1.0-SNAPSHOTPU");

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    
    public void create(Oficina oficina) {
        if (oficina.getEventoCollection() == null) {
            oficina.setEventoCollection(new ArrayList<Evento>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Evento> attachedEventoCollection = new ArrayList<Evento>();
            for (Evento eventoCollectionEventoToAttach : oficina.getEventoCollection()) {
                eventoCollectionEventoToAttach = em.getReference(eventoCollectionEventoToAttach.getClass(), eventoCollectionEventoToAttach.getIdEvento());
                attachedEventoCollection.add(eventoCollectionEventoToAttach);
            }
            oficina.setEventoCollection(attachedEventoCollection);
            em.persist(oficina);
            for (Evento eventoCollectionEvento : oficina.getEventoCollection()) {
                Oficina oldIdOficinaOfEventoCollectionEvento = eventoCollectionEvento.getIdOficina();
                eventoCollectionEvento.setIdOficina(oficina);
                eventoCollectionEvento = em.merge(eventoCollectionEvento);
                if (oldIdOficinaOfEventoCollectionEvento != null) {
                    oldIdOficinaOfEventoCollectionEvento.getEventoCollection().remove(eventoCollectionEvento);
                    oldIdOficinaOfEventoCollectionEvento = em.merge(oldIdOficinaOfEventoCollectionEvento);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Oficina oficina) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Oficina persistentOficina = em.find(Oficina.class, oficina.getIdOficina());
            Collection<Evento> eventoCollectionOld = persistentOficina.getEventoCollection();
            Collection<Evento> eventoCollectionNew = oficina.getEventoCollection();
            Collection<Evento> attachedEventoCollectionNew = new ArrayList<Evento>();
            for (Evento eventoCollectionNewEventoToAttach : eventoCollectionNew) {
                eventoCollectionNewEventoToAttach = em.getReference(eventoCollectionNewEventoToAttach.getClass(), eventoCollectionNewEventoToAttach.getIdEvento());
                attachedEventoCollectionNew.add(eventoCollectionNewEventoToAttach);
            }
            eventoCollectionNew = attachedEventoCollectionNew;
            oficina.setEventoCollection(eventoCollectionNew);
            oficina = em.merge(oficina);
            for (Evento eventoCollectionOldEvento : eventoCollectionOld) {
                if (!eventoCollectionNew.contains(eventoCollectionOldEvento)) {
                    eventoCollectionOldEvento.setIdOficina(null);
                    eventoCollectionOldEvento = em.merge(eventoCollectionOldEvento);
                }
            }
            for (Evento eventoCollectionNewEvento : eventoCollectionNew) {
                if (!eventoCollectionOld.contains(eventoCollectionNewEvento)) {
                    Oficina oldIdOficinaOfEventoCollectionNewEvento = eventoCollectionNewEvento.getIdOficina();
                    eventoCollectionNewEvento.setIdOficina(oficina);
                    eventoCollectionNewEvento = em.merge(eventoCollectionNewEvento);
                    if (oldIdOficinaOfEventoCollectionNewEvento != null && !oldIdOficinaOfEventoCollectionNewEvento.equals(oficina)) {
                        oldIdOficinaOfEventoCollectionNewEvento.getEventoCollection().remove(eventoCollectionNewEvento);
                        oldIdOficinaOfEventoCollectionNewEvento = em.merge(oldIdOficinaOfEventoCollectionNewEvento);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = oficina.getIdOficina();
                if (findOficina(id) == null) {
                    throw new NonexistentEntityException("The oficina with id " + id + " no longer exists.");
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
            Oficina oficina;
            try {
                oficina = em.getReference(Oficina.class, id);
                oficina.getIdOficina();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The oficina with id " + id + " no longer exists.", enfe);
            }
            Collection<Evento> eventoCollection = oficina.getEventoCollection();
            for (Evento eventoCollectionEvento : eventoCollection) {
                eventoCollectionEvento.setIdOficina(null);
                eventoCollectionEvento = em.merge(eventoCollectionEvento);
            }
            em.remove(oficina);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Oficina> findOficinaEntities() {
        return findOficinaEntities(true, -1, -1);
    }

    public List<Oficina> findOficinaEntities(int maxResults, int firstResult) {
        return findOficinaEntities(false, maxResults, firstResult);
    }

    private List<Oficina> findOficinaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Oficina.class));
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

    public Oficina findOficina(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Oficina.class, id);
        } finally {
            em.close();
        }
    }

    public int getOficinaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Oficina> rt = cq.from(Oficina.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
