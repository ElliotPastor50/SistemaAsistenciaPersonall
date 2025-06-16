package servlet;

import dao.EventoJpaController;
import dto.Empleado;
import dto.Evento;
import dto.Oficina;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/listarEventos")
public class ListarEventosServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EventoJpaController eventoDAO = new EventoJpaController();
        List<Evento> eventos = eventoDAO.ordenarPorRelojLogico();

        JSONArray arregloJson = new JSONArray();

        // Formateadores de fecha y hora
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm");

        if (!eventos.isEmpty()) {
            for (Evento evento : eventos) {
                JSONObject obj = new JSONObject();

                Empleado emp = evento.getIdEmpleado();
                Oficina oficina = evento.getIdOficina();

                obj.put("nombreEmpleado", emp != null ? emp.getNombre() : "Sin nombre");
                obj.put("nombreOficina", oficina != null ? oficina.getNombre() : "Sin oficina");
                obj.put("tipoEvento", evento.getTipoEvento());

                // Aplicar formato de fecha y hora
                String fechaFormateada = evento.getFecha() != null ? formatoFecha.format(evento.getFecha()) : "";
                String horaFormateada = evento.getHora() != null ? formatoHora.format(evento.getHora()) : "";

                obj.put("fecha", fechaFormateada);
                obj.put("hora", horaFormateada);
                obj.put("relojLogico", evento.getRelojLogico());

                arregloJson.put(obj);
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(arregloJson.toString());

        }
    }
}
