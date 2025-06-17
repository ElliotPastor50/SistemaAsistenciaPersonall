package servlet;

import dao.EventoJpaController;
import dto.Empleado;
import dto.Evento;
import dto.Oficina;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@WebServlet("/eventos")
public class ListarEventosServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            String accion = request.getParameter("accion");
            String idEmpleadoStr = request.getParameter("idEmpleado");

            EventoJpaController eventoDAO = new EventoJpaController();

            // responder a accion=ultimo
            if ("ultimo".equalsIgnoreCase(accion) && idEmpleadoStr != null) {
                String idOficinaStr = request.getParameter("idOficina");

                if (idOficinaStr == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().print("{\"error\":\"Falta el ID de oficina.\"}");
                    return;
                }

                int idEmpleado = Integer.parseInt(idEmpleadoStr);
                int idOficina = Integer.parseInt(idOficinaStr);

                Evento ultimo = eventoDAO.ultimoEvento(idEmpleado, idOficina);

                JSONObject json = new JSONObject();

                if (ultimo != null) {
                    json.put("tipo", ultimo.getTipoEvento());
                    json.put("hora", new SimpleDateFormat("HH:mm").format(ultimo.getHora()));
                } else {
                    json.put("tipo", "NINGUNO");
                }

                response.getWriter().print(json.toString());
                return;
            }

            // === Lógica existente para listar eventos ===
            List<Evento> eventos = eventoDAO.ordenarPorRelojLogico();

            Collections.sort(eventos, new Comparator<Evento>() {
                @Override
                public int compare(Evento e1, Evento e2) {
                    // 1. Comparar por reloj lógico
                    int cmpReloj = Integer.compare(e1.getRelojLogico(), e2.getRelojLogico());
                    if (cmpReloj != 0) {
                        return cmpReloj;
                    }

                    // 2. Comparar por tipo de evento: ENTRADA (0) antes que SALIDA (1)
                    int tipo1 = "ENTRADA".equalsIgnoreCase(e1.getTipoEvento()) ? 0 : 1;
                    int tipo2 = "ENTRADA".equalsIgnoreCase(e2.getTipoEvento()) ? 0 : 1;
                    int cmpTipo = Integer.compare(tipo1, tipo2);
                    if (cmpTipo != 0) {
                        return cmpTipo;
                    }

                    // 3. Comparar por nombre de empleado (alfabéticamente)
                    String nombre1 = e1.getIdEmpleado() != null ? e1.getIdEmpleado().getNombre().toLowerCase() : "";
                    String nombre2 = e2.getIdEmpleado() != null ? e2.getIdEmpleado().getNombre().toLowerCase() : "";
                    return nombre1.compareTo(nombre2);
                }
            });

            String oficinaParam = request.getParameter("oficina");
            Integer idOficinaFiltro = null;
            if (oficinaParam != null && !oficinaParam.isEmpty()) {
                try {
                    idOficinaFiltro = Integer.valueOf(oficinaParam);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().print("{\"error\":\"ID de oficina inválido.\"}");
                    return;
                }
            }

            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm");

            JSONArray arregloJson = new JSONArray();

            for (Evento evento : eventos) {
                if (idOficinaFiltro != null) {
                    Oficina oficina = evento.getIdOficina();
                    if (oficina == null || !oficina.getIdOficina().equals(idOficinaFiltro)) {
                        continue;
                    }
                }

                JSONObject obj = new JSONObject();
                Empleado emp = evento.getIdEmpleado();
                Oficina oficina = evento.getIdOficina();

                obj.put("nombreEmpleado", emp != null ? emp.getNombre() : "Sin nombre");
                obj.put("nombreOficina", oficina != null ? oficina.getNombre() : "Sin oficina");
                obj.put("tipo", evento.getTipoEvento());
                obj.put("fecha", evento.getFecha() != null ? formatoFecha.format(evento.getFecha()) : "");
                obj.put("hora", evento.getHora() != null ? formatoHora.format(evento.getHora()) : "");
                obj.put("relojLamport", evento.getRelojLogico());

                arregloJson.put(obj);
            }

            response.getWriter().print(arregloJson.toString());

        } catch (IOException | NumberFormatException | JSONException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\"Error al obtener los eventos.\"}");
            ex.printStackTrace();
        }
    }
}
