package servlet;

import dao.EventoJpaController;
import dto.Evento;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/eventos") 
public class EventosServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaAsistenciaPersonal_war_1.0-SNAPSHOTPU");
            EventoJpaController eventoDAO = new EventoJpaController(emf);
            List<Evento> eventos = eventoDAO.findEventoEntities();

            SimpleDateFormat sdfFecha = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm:ss");

            out.print("[");
            for (int i = 0; i < eventos.size(); i++) {
                Evento e = eventos.get(i);
                out.print("{");
                out.print("\"id\":" + e.getIdEvento() + ",");
                out.print("\"empleado\":\"" + escaparJson(e.getIdEmpleado() != null ? e.getIdEmpleado().getNombre() : "Sin asignar") + "\",");
                out.print("\"oficina\":\"" + escaparJson(e.getIdOficina() != null ? e.getIdOficina().getNombre() : "Sin oficina") + "\",");
                out.print("\"tipoEvento\":\"" + escaparJson(e.getTipoEvento()) + "\",");
                out.print("\"fecha\":\"" + sdfFecha.format(e.getFecha()) + "\",");
                out.print("\"hora\":\"" + sdfHora.format(e.getHora()) + "\",");
                out.print("\"relojLogico\":" + e.getRelojLogico());
                out.print("}");
                if (i < eventos.size() - 1) {
                    out.print(",");
                }
            }
            out.print("]");

        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Error al obtener los eventos.\"}");
            ex.printStackTrace();
        } finally {
            out.close();
        }
    }

    private String escaparJson(String texto) {
        if (texto == null) return "";
        return texto.replace("\"", "\\\"").replace("\n", "").replace("\r", "");
    }
}
