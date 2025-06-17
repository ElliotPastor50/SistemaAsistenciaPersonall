package dao;

import dao.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Empleado;
import dto.Evento;
import dto.Oficina;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

public class EventoJpaController implements Serializable {

    public EventoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaAsistenciaPersonal_war_1.0-SNAPSHOTPU");

    public EventoJpaController() {
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Evento evento) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Empleado idEmpleado = evento.getIdEmpleado();
            if (idEmpleado != null) {
                idEmpleado = em.getReference(idEmpleado.getClass(), idEmpleado.getIdEmpleado());
                evento.setIdEmpleado(idEmpleado);
            }
            Oficina idOficina = evento.getIdOficina();
            if (idOficina != null) {
                idOficina = em.getReference(idOficina.getClass(), idOficina.getIdOficina());
                evento.setIdOficina(idOficina);
            }
            em.persist(evento);
            if (idEmpleado != null) {
                idEmpleado.getEventoCollection().add(evento);
                idEmpleado = em.merge(idEmpleado);
            }
            if (idOficina != null) {
                idOficina.getEventoCollection().add(evento);
                idOficina = em.merge(idOficina);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Evento evento) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Evento persistentEvento = em.find(Evento.class, evento.getIdEvento());
            Empleado idEmpleadoOld = persistentEvento.getIdEmpleado();
            Empleado idEmpleadoNew = evento.getIdEmpleado();
            Oficina idOficinaOld = persistentEvento.getIdOficina();
            Oficina idOficinaNew = evento.getIdOficina();
            if (idEmpleadoNew != null) {
                idEmpleadoNew = em.getReference(idEmpleadoNew.getClass(), idEmpleadoNew.getIdEmpleado());
                evento.setIdEmpleado(idEmpleadoNew);
            }
            if (idOficinaNew != null) {
                idOficinaNew = em.getReference(idOficinaNew.getClass(), idOficinaNew.getIdOficina());
                evento.setIdOficina(idOficinaNew);
            }
            evento = em.merge(evento);
            if (idEmpleadoOld != null && !idEmpleadoOld.equals(idEmpleadoNew)) {
                idEmpleadoOld.getEventoCollection().remove(evento);
                idEmpleadoOld = em.merge(idEmpleadoOld);
            }
            if (idEmpleadoNew != null && !idEmpleadoNew.equals(idEmpleadoOld)) {
                idEmpleadoNew.getEventoCollection().add(evento);
                idEmpleadoNew = em.merge(idEmpleadoNew);
            }
            if (idOficinaOld != null && !idOficinaOld.equals(idOficinaNew)) {
                idOficinaOld.getEventoCollection().remove(evento);
                idOficinaOld = em.merge(idOficinaOld);
            }
            if (idOficinaNew != null && !idOficinaNew.equals(idOficinaOld)) {
                idOficinaNew.getEventoCollection().add(evento);
                idOficinaNew = em.merge(idOficinaNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = evento.getIdEvento();
                if (findEvento(id) == null) {
                    throw new NonexistentEntityException("The evento with id " + id + " no longer exists.");
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
            Evento evento;
            try {
                evento = em.getReference(Evento.class, id);
                evento.getIdEvento();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The evento with id " + id + " no longer exists.", enfe);
            }
            Empleado idEmpleado = evento.getIdEmpleado();
            if (idEmpleado != null) {
                idEmpleado.getEventoCollection().remove(evento);
                idEmpleado = em.merge(idEmpleado);
            }
            Oficina idOficina = evento.getIdOficina();
            if (idOficina != null) {
                idOficina.getEventoCollection().remove(evento);
                idOficina = em.merge(idOficina);
            }
            em.remove(evento);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Evento> findEventoEntities() {
        return findEventoEntities(true, -1, -1);
    }

    public List<Evento> findEventoEntities(int maxResults, int firstResult) {
        return findEventoEntities(false, maxResults, firstResult);
    }

    private List<Evento> findEventoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Evento.class));
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

    public Evento findEvento(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Evento.class, id);
        } finally {
            em.close();
        }
    }

    public int getEventoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Evento> rt = cq.from(Evento.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public List<Evento> ordenarPorRelojLogico() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createNamedQuery("Evento.findAllOrderRL");
            List<Evento> eventos = q.getResultList();
            return eventos;
        } finally {
            em.close();
        }
    }

    public Evento ultimoEvento(int idEmpleado, int idOficina) {
        EntityManager em = getEntityManager();
        try {
            List<Evento> resultados = em.createQuery(
                    "SELECT e FROM Evento e WHERE e.idEmpleado.idEmpleado = :idEmpleado AND e.idOficina.idOficina = :idOficina ORDER BY e.relojLogico DESC"
            )
                    .setParameter("idEmpleado", idEmpleado)
                    .setParameter("idOficina", idOficina)
                    .setMaxResults(1)
                    .getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);

        } finally {
            em.close();
        }
    }

}
