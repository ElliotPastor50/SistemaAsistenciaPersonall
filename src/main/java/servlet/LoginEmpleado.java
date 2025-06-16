package servlet;

import dao.EmpleadoJpaController;
import dto.Empleado;
import java.io.BufferedReader;
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

        BufferedReader reader = req.getReader();
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }

        JSONObject jsonRequest = new JSONObject(jsonBuilder.toString());
        JSONObject json = new JSONObject();

        try {
            String correo = jsonRequest.getString("correo");
            String contrasena = jsonRequest.getString("contrasena");

            Empleado encontrado = empleadoJpa.validar(correo, contrasena);

            if (encontrado != null) {
                System.out.println("Empleado encontrado: " + encontrado.getNombre());
                HttpSession session = req.getSession(true);
                session.setAttribute("idEmpleado", encontrado.getIdEmpleado());
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
