package servlet;

import dao.EmpleadoJpaController;
import dto.Empleado;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;

@WebServlet("/datosUsuario")
public class DatosUsuarioServlet extends HttpServlet {

    private final EmpleadoJpaController empleadoController = new EmpleadoJpaController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession sesion = req.getSession(false);
        JSONObject obj = new JSONObject();

        if (sesion != null) {
            Object idEmpleadoObj = sesion.getAttribute("idEmpleado");
            if (idEmpleadoObj != null) {
                int idEmpleado = (int) idEmpleadoObj;

                Empleado empleado = empleadoController.findEmpleado(idEmpleado);

                if (empleado != null) {
                    obj.put("idEmpleado", empleado.getIdEmpleado());
                    obj.put("nombre", empleado.getNombre());
                    obj.put("correo", empleado.getCorreo());
                } else {
                    obj.put("error", "Empleado no encontrado.");
                }
            } else {
                obj.put("error", "ID de empleado no disponible en la sesión.");
            }
        } else {
            obj.put("error", "Sesión no iniciada.");
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(obj.toString());
    }
}
