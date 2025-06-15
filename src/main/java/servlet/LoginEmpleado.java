package servlet;

import dao.EmpleadoJpaController;
import dto.Empleado;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;

@WebServlet(name = "LoginEmpleado", urlPatterns = {"/login"})
public class LoginEmpleado extends HttpServlet {

    private EmpleadoJpaController empleadoJpa;

    @Override
    public void init() {
        empleadoJpa = new EmpleadoJpaController();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        JSONObject json = new JSONObject();

        try {
            String correo = req.getParameter("correo");
            String contrasena = req.getParameter("contrasena");

            Empleado encontrado = empleadoJpa.validar(correo, contrasena);

            if (encontrado != null) {
                HttpSession session = req.getSession(true);
                session.setAttribute("empleado", encontrado);
                json.put("exito", true);
                json.put("id", encontrado.getIdEmpleado());
                json.put("nombre", encontrado.getNombre());
            } else {
                json.put("exito", false);
                json.put("error", "Credenciales incorrectas");
            }

        } catch (Exception e) {
            json.put("exito", false);
            json.put("error", "Error en servidor: " + e.getMessage());
        }

        resp.getWriter().write(json.toString());
    }

}
