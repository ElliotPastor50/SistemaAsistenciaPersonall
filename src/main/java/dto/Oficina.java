package dto;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


@Entity
@Table(name = "oficina")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Oficina.findAll", query = "SELECT o FROM Oficina o"),
    @NamedQuery(name = "Oficina.findByIdOficina", query = "SELECT o FROM Oficina o WHERE o.idOficina = :idOficina"),
    @NamedQuery(name = "Oficina.findByNombre", query = "SELECT o FROM Oficina o WHERE o.nombre = :nombre"),
    @NamedQuery(name = "Oficina.findByRelojLogico", query = "SELECT o FROM Oficina o WHERE o.relojLogico = :relojLogico")})
public class Oficina implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_oficina")
    private Integer idOficina;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "nombre")
    private String nombre;
    @Basic(optional = false)
    @NotNull
    @Column(name = "reloj_logico")
    private int relojLogico;
    @OneToMany(mappedBy = "idOficina")
    private Collection<Evento> eventoCollection;

    public Oficina() {
    }

    public Oficina(Integer idOficina) {
        this.idOficina = idOficina;
    }

    public Oficina(Integer idOficina, String nombre, int relojLogico) {
        this.idOficina = idOficina;
        this.nombre = nombre;
        this.relojLogico = relojLogico;
    }

    public Integer getIdOficina() {
        return idOficina;
    }

    public void setIdOficina(Integer idOficina) {
        this.idOficina = idOficina;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getRelojLogico() {
        return relojLogico;
    }

    public void setRelojLogico(int relojLogico) {
        this.relojLogico = relojLogico;
    }

    @XmlTransient
    public Collection<Evento> getEventoCollection() {
        return eventoCollection;
    }

    public void setEventoCollection(Collection<Evento> eventoCollection) {
        this.eventoCollection = eventoCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idOficina != null ? idOficina.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Oficina)) {
            return false;
        }
        Oficina other = (Oficina) object;
        if ((this.idOficina == null && other.idOficina != null) || (this.idOficina != null && !this.idOficina.equals(other.idOficina))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "dto.Oficina[ idOficina=" + idOficina + " ]";
    }
    
}
