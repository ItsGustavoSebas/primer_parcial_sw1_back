package com.example.parcial_sw1.service;

import com.example.parcial_sw1.dto.AtributoDto;
import com.example.parcial_sw1.dto.ReqRes;
import com.example.parcial_sw1.entity.*;
import com.example.parcial_sw1.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProyectoService {

    @Autowired
    private ProyectoRepo proyectoRepo;

    @Autowired
    private UsersRepo ourUsersRepo;

    @Autowired
    private TablaRepo tablaRepo;

    @Autowired
    private AtributoRepo atributoRepo;

    @Autowired
    private TipoRepo tipoRepo;

    @Autowired
    private RelacionRepo relacionRepo;
    @Autowired
    private ScopeRepo scopeRepo;
    @Autowired
    private TipoDatoRepo tipoDatoRepo;
    @Autowired
    private ColaboradorRepo colaboradorRepo;
    @Autowired
    private InvitacionRepo invitacionRepo;
    @Autowired
    private EmailService emailService;

    public Proyecto getProyecto(int proyectoId) {
        return proyectoRepo.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto not found"));
    }

    public List<Proyecto> getProyectos() {
        return proyectoRepo.findAll();
    }

    public List<Proyecto> obtenerProyectosPorUsuario(String email) throws Exception {
        OurUsers usuario = ourUsersRepo.findByEmail(email)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
        List<Proyecto> proyectosCreador = proyectoRepo.findByCreador(usuario);
        List<Proyecto> proyectosColaborador = proyectoRepo.findByColaboradoresContains(usuario);
        proyectosCreador.addAll(proyectosColaborador);
        return proyectosCreador;
    }

    public Tabla guardarTabla(int proyectoId, ReqRes reqRes) throws Exception {
        Proyecto proyecto = proyectoRepo.findById(proyectoId)
                .orElseThrow(() -> new Exception("Proyecto no encontrado"));

        Tabla nuevaTabla = new Tabla();
        nuevaTabla.setName(reqRes.getName());
        nuevaTabla.setProyecto(proyecto);
        nuevaTabla.setPosicion_x(reqRes.getPosicion_x());
        nuevaTabla.setPosicion_y(reqRes.getPosicion_y());
        nuevaTabla.setTabcolor(reqRes.getTabcolor());
        return tablaRepo.save(nuevaTabla);
    }

    public Tabla editarPosicion(int tablaId, ReqRes reqRes) throws Exception {
        Tabla tabla = tablaRepo.findById(tablaId)
                .orElseThrow(() -> new Exception("Tabla no encontrada"));

        tabla.setPosicion_x(reqRes.getPosicion_x());
        tabla.setPosicion_y(reqRes.getPosicion_y());
        return tablaRepo.save(tabla);
    }

    public Relacion guardarRelacion(ReqRes reqRes) throws Exception {

        Tabla tablaSource = tablaRepo.findById(reqRes.getTablaSourceId())
                .orElseThrow(() -> new Exception("Tabla Source no encontrada"));

        Tabla tablaTarget = tablaRepo.findById(reqRes.getTablaTargetId())
                .orElseThrow(() -> new Exception("Tabla Target no encontrada"));

        Tipo tipoRelacion = tipoRepo.findById(reqRes.getTipoId())
                .orElseThrow(() -> new Exception("Tipo de relación no encontrado"));

        Relacion nuevaRelacion = new Relacion();
        nuevaRelacion.setDetalle(reqRes.getDetalle());
        nuevaRelacion.setMultsource(reqRes.getMultsource());
        nuevaRelacion.setMulttarget(reqRes.getMulttarget());
        nuevaRelacion.setTablaSource(tablaSource);
        nuevaRelacion.setTablaTarget(tablaTarget);
        nuevaRelacion.setSourceName(reqRes.getSourceName());
        nuevaRelacion.setTargetName(reqRes.getTargetName());
        nuevaRelacion.setSourceArgs(reqRes.getSourceArgs());
        nuevaRelacion.setTargetArgs(reqRes.getTargetArgs());
        nuevaRelacion.setTipo(tipoRelacion);

        relacionRepo.save(nuevaRelacion);

        return nuevaRelacion;
    }

    public Tabla guardarAtributos(int tablaId, ReqRes reqRes) throws Exception {
        // Obtener la tabla por su ID
        Tabla tabla = tablaRepo.findById(tablaId)
                .orElseThrow(() -> new Exception("Tabla no encontrada"));

        // Actualizar los campos de la tabla
        tabla.setName(reqRes.getName());
        tabla.setTabcolor(reqRes.getTabcolor());
        Tabla tablaGuardada = tablaRepo.save(tabla);

        // Obtener los nombres de los atributos que ya existen en la tabla
        List<String> nombresAtributosExistentes = tablaGuardada.getAtributos()
                .stream()
                .map(Atributo::getNombre)
                .collect(Collectors.toList());

        // Recorrer los nuevos atributos y solo guardar aquellos cuyo nombre no exista
        List<AtributoDto> atributosDTO = reqRes.getAtributos();
        for (AtributoDto atributoDTO : atributosDTO) {
            if (!nombresAtributosExistentes.contains(atributoDTO.getNombre())) {
                Atributo nuevoAtributo = new Atributo();
                Scope scope = scopeRepo.findByNombre(atributoDTO.getScope())
                        .orElseThrow(() -> new RuntimeException("Scope no encontrado: " + atributoDTO.getScope()));
                Tipo_Dato tipoDato = tipoDatoRepo.findByNombre(atributoDTO.getTipoDato())
                        .orElseThrow(() -> new RuntimeException("Tipo de dato no encontrado: " + atributoDTO.getTipoDato()));
                nuevoAtributo.setNombre(atributoDTO.getNombre());
                nuevoAtributo.setNulleable(atributoDTO.isNulleable());
                nuevoAtributo.setPk(atributoDTO.isPk());
                nuevoAtributo.setTabla(tablaGuardada);
                nuevoAtributo.setScope(scope);
                nuevoAtributo.setTipoDato(tipoDato);
                atributoRepo.save(nuevoAtributo);
            }
        }

        return tablaGuardada;
    }

    public Integer createProjectWithFile(MultipartFile file, String titulo, String descripcion, String email) throws Exception{
        OurUsers usuario = ourUsersRepo.findByEmail(email)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
        Proyecto nuevoProyecto = new Proyecto();
        nuevoProyecto.setTitulo(titulo);
        nuevoProyecto.setDescripcion(descripcion);
        nuevoProyecto.setCreador(usuario);
        nuevoProyecto.setUrlPreview("assets/imagenes/previews/preview1.jpg");

        Proyecto proyectoGuardado = proyectoRepo.save(nuevoProyecto);
        return proyectoGuardado.getId();
    }

    // Lógica para crear un proyecto sin archivo
    public Integer createProjectWithoutFile(String email, String titulo, String descripcion) throws Exception{
        OurUsers usuario = ourUsersRepo.findByEmail(email)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
        Proyecto nuevoProyecto = new Proyecto();
        nuevoProyecto.setTitulo(titulo);
        nuevoProyecto.setDescripcion(descripcion);
        nuevoProyecto.setCreador(usuario);
        nuevoProyecto.setUrlPreview("assets/imagenes/previews/preview1.jpg");
        Proyecto proyectoGuardado = proyectoRepo.save(nuevoProyecto);
        return proyectoGuardado.getId();
    }

    public ReqRes enviarInvitacion (ReqRes reqRes) {
        ReqRes response = new ReqRes();
        try {
            Optional<OurUsers> usuario = ourUsersRepo.findByEmail(reqRes.getEmail());
            Optional<Proyecto> proyecto = proyectoRepo.findById(reqRes.getProyectoId());
            if (usuario.isPresent() && proyecto.isPresent()) {
                Colaborador colaborador = new Colaborador();
                colaborador.setPermiso(reqRes.getPermiso());
                colaborador.setFecha(LocalDate.now());
                colaborador.setProyecto(proyecto.get());
                colaborador.setUsuario(usuario.get());
                colaboradorRepo.save(colaborador);
                response.setStatusCode(200);
                response.setMessage("Successful");
            } else if (proyecto.isPresent()) {
                Invitacion invitacion = new Invitacion();
                invitacion.setPermiso(reqRes.getPermiso());
                invitacion.setEmail(reqRes.getEmail());
                invitacion.setProyecto(proyecto.get());
                invitacionRepo.save(invitacion);
                response.setStatusCode(200);
                response.setMessage("Successful");
            } else {
                response.setStatusCode(404);
                response.setMessage("No proyecto found");
                return response;
            }
            System.out.println("enviando correo");
            String inviteUrl = "http://localhost:4200/proyecto/" + proyecto.get().getId(); // URL de registro o aceptación

            String emailContent = "<p>Has sido invitado a colaborar en el proyecto: "
                    + proyecto.get().getTitulo() + ".</p>"
                    + "<p>Haz clic en el botón a continuación para aceptar la invitación:</p>"
                    + "<a href=\"" + inviteUrl + "\" style=\"background-color: #4CAF50; color: white; padding: 10px 20px; text-align: center; text-decoration: none; display: inline-block;\">Aceptar Invitación</a>";

            // Enviar correo
            emailService.sendEmail(reqRes.getEmail(), "Invitación a colaborar en el proyecto", emailContent);
        }catch (Exception e) {
            response.setStatusCode(500);
            response.setError(e.getMessage());
    }
        return response;
    }

}
