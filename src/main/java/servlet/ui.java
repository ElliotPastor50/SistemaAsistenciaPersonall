
package servlet;

import dao.EmpleadoJpaController;
import dto.Empleado;
import java.util.List;


public class ui {
    public static void main(String[] args) {
        EmpleadoJpaController empleadoDAO = new EmpleadoJpaController();
        List<Empleado> empleados = empleadoDAO.findEmpleadoEntities();
        
        for (Empleado empleado : empleados) {
            System.out.println("Empleado: " + empleado.getNombre() +": "+ empleado.getCorreo());
        }
    }
    
}
