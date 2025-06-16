package servlet;

import dao.EmpleadoJpaController;
import dao.EventoJpaController;
import dao.OficinaJpaController;
import dto.Empleado;
import dto.Evento;
import dto.Oficina;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
            
            //Procesar request
            BufferedReader reader = request.getReader();
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            
            System.out.println("Datos procesados");
            //Instanciarlo en objeto json
            JSONObject jsonRequest = new JSONObject(jsonBuilder.toString());
            
            //Obtener valores del Request
            String tipo = jsonRequest.getString("tipo"); // "ENTRADA" o "SALIDA"
            String fechaStr = jsonRequest.getString("fecha");
            String horaStr = jsonRequest.getString("hora"); // puede venir vacío
            int idOficina = Integer.parseInt(jsonRequest.getString("idOficina"));
            int idEmpleado = Integer.parseInt(jsonRequest.getString("idEmpleado"));

            System.out.println("Se obtuvo datos exitosamente");
            // Procesar fecha y hora
            SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm");
            Date fecha = formatoFecha.parse(fechaStr);
            Date hora = (horaStr != null && !horaStr.isEmpty()) ? formatoHora.parse(horaStr) : new Date();

            // Obtener oficina y actualizar su reloj lógico
            Oficina oficinaActual = oficinaController.findOficina(idOficina);
            int relojActual = oficinaActual.getRelojLogico();
            int nuevoReloj = relojActual + 1;
            oficinaActual.setRelojLogico(nuevoReloj);
            oficinaController.edit(oficinaActual);

            // Sincronizar otras oficinas
            List<Oficina> todasLasOficinas = oficinaController.findOficinaEntities();
            for (Oficina otraOficina : todasLasOficinas) {
                if (!otraOficina.getIdOficina().equals(oficinaActual.getIdOficina())) {
                    int sincronizado = Math.max(otraOficina.getRelojLogico(), nuevoReloj) + 1;
                    otraOficina.setRelojLogico(sincronizado);
                    oficinaController.edit(otraOficina);
                }
            }

            // Crear y registrar el evento
            Empleado empleado = empleadoController.findEmpleado(idEmpleado);
            Evento evento = new Evento();
            evento.setTipoEvento(tipo);
            evento.setFecha(fecha);
            evento.setHora(hora);
            evento.setRelojLogico(nuevoReloj);
            evento.setIdOficina(oficinaActual);
            evento.setIdEmpleado(empleado);

            eventoController.create(evento);

            // Responder
            response.setContentType("application/json");
            JSONObject json = new JSONObject();
            json.put("status", "ok");
            json.put("mensaje", "Evento registrado correctamente.");
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
