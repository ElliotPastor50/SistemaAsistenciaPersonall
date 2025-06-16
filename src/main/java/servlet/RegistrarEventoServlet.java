package servlet;

import dao.EmpleadoJpaController;
import dao.EventoJpaController;
import dao.OficinaJpaController;
import dto.Empleado;
import dto.Evento;
import dto.Oficina;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

@WebServlet("/registrarEvento")
public class RegistrarEventoServlet extends HttpServlet {

    private final EventoJpaController eventoController = new EventoJpaController();
    private final OficinaJpaController oficinaController = new OficinaJpaController();
    private final EmpleadoJpaController empleadoController = new EmpleadoJpaController();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            response.setContentType("application/json");

            // Leer JSON del request
            BufferedReader reader = request.getReader();
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject jsonRequest = new JSONObject(jsonBuilder.toString());

            int idOficina = jsonRequest.getInt("idOficina");
            int idEmpleado = jsonRequest.getInt("idEmpleado");

            Date ahora = new Date();

            // 1. Obtener oficina que genera el evento
            Oficina oficinaActual = oficinaController.findOficina(idOficina);

            // 2. Incrementar su reloj lógico local
            int nuevoReloj = oficinaActual.getRelojLogico() + 1;
            oficinaActual.setRelojLogico(nuevoReloj);
            oficinaController.edit(oficinaActual);

            // 3. Determinar tipo de evento
            Evento ultimoEvento = eventoController.ultimoEvento(idEmpleado);
            String tipo = (ultimoEvento == null || "SALIDA".equals(ultimoEvento.getTipoEvento()))
                    ? "ENTRADA" : "SALIDA";

            // 4. Crear evento
            Empleado empleado = empleadoController.findEmpleado(idEmpleado);

            Evento evento = new Evento();
            evento.setTipoEvento(tipo);
            evento.setFecha(ahora);
            evento.setHora(ahora);
            evento.setRelojLogico(nuevoReloj); // solo de esa oficina
            evento.setIdOficina(oficinaActual);
            evento.setIdEmpleado(empleado);

            eventoController.create(evento);

            // 5. Respuesta
            JSONObject json = new JSONObject();
            json.put("status", "ok");
            json.put("mensaje", tipo + " registrada correctamente.");
            json.put("tipo", tipo);
            json.put("relojLogico", nuevoReloj);
            response.getWriter().write(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject error = new JSONObject();
            error.put("status", "error");
            error.put("mensaje", "Error al registrar evento: " + e.getMessage());
            response.getWriter().write(error.toString());
        }
    }

}
