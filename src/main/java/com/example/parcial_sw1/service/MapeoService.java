package com.example.parcial_sw1.service;

import com.example.parcial_sw1.dto.AtributoDto;
import com.example.parcial_sw1.dto.AtributoMapDto;
import com.example.parcial_sw1.dto.ReqRes;
import com.example.parcial_sw1.dto.TablaDto;
import com.example.parcial_sw1.entity.*;
import com.example.parcial_sw1.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class MapeoService {

    @Autowired
    private ProyectoRepo proyectoRepo;

    @Autowired
    private AtributoRepo atributoRepo;
    @Autowired
    private TablaRepo tablaRepo;
    @Autowired
    private ScopeRepo scopeRepo;
    @Autowired
    private TipoDatoRepo tipoDatoRepo;
    @Autowired
    private TipoRepo tipoRepo;
    @Autowired
    private RelacionRepo relacionRepo;

    public List<TablaDto> obtenerTablasMapeo(int proyectoId) throws Exception {
        Proyecto proyecto = proyectoRepo.findById(proyectoId)
                .orElseThrow(() -> new Exception("Proyecto no encontrado"));

        List<Tabla> tablas = proyecto.getTablas();

        List<TablaDto> tablaDtos = new ArrayList<>();

        for (Tabla tabla : tablas) {
            TablaDto tablaDto = new TablaDto();
            tablaDto.setId(tabla.getId());
            tablaDto.setNombre(tabla.getName());
            List<AtributoMapDto> atributoDtos = new ArrayList<>();
            AtributoMapDto atributoDto2 = new AtributoMapDto();
            atributoDto2.setId(100);
            atributoDto2.setNombre("id");
            atributoDto2.setFk(false);
            atributoDto2.setPk(true);
            atributoDto2.setTipoDato("int");
            atributoDtos.add(atributoDto2);
            for (Atributo atributo : tabla.getAtributos()) {
                AtributoMapDto atributoDto = new AtributoMapDto();
                atributoDto.setId(atributo.getId());
                atributoDto.setNombre(atributo.getNombre());
                atributoDto.setFk(false);
                atributoDto.setPk(false);
                atributoDto.setTipoDato(atributo.getTipoDato().getNombre());
                atributoDtos.add(atributoDto);
            }

            for (Relacion relacion : tabla.getRelacionesTarget()) {
                if (esRelacionConLlaveForaneaTarget(relacion)) {
                    AtributoMapDto atributoDto = new AtributoMapDto();
                    atributoDto.setId(relacion.getTablaSource().getId());
                    atributoDto.setNombre(relacion.getTablaSource().getName() + "_id");
                    atributoDto.setFk(true);
                    atributoDto.setPk(relacion.getTipo().getNombre().equals("herencia"));
                    atributoDto.setTipoDato("int");
                    atributoDtos.add(atributoDto);
                }
            }

            for (Relacion relacion : tabla.getRelacionesSource()) {
                if (esRelacionConLlaveForaneaSource(relacion)) {
                    AtributoMapDto atributoDto = new AtributoMapDto();
                    atributoDto.setId(relacion.getTablaTarget().getId());
                    atributoDto.setNombre(relacion.getTablaTarget().getName() + "_id");
                    atributoDto.setFk(true);
                    atributoDto.setPk(relacion.getTipo().getNombre().equals("agregacion"));
                    atributoDto.setTipoDato("int");
                    atributoDtos.add(atributoDto);
                }
            }
            tablaDto.setAtributos(atributoDtos);
            tablaDtos.add(tablaDto);
        }
        return tablaDtos;
    }

    private boolean esRelacionConLlaveForaneaTarget(Relacion relacion) {
        String tipoRelacion = relacion.getTipo().getNombre();
        String multiplicidad = relacion.getMulttarget();
        return (tipoRelacion.equals("asociacion") && multiplicidad.equals("1..*"))|| tipoRelacion.equals("herencia");
    }

    private boolean esRelacionConLlaveForaneaSource(Relacion relacion) {
        String tipoRelacion = relacion.getTipo().getNombre();
        String multiplicidad = relacion.getMultsource();
        String multiplicidadT = relacion.getMulttarget();
        return (tipoRelacion.equals("asociacion") && (multiplicidad.equals("1..*") ||
                multiplicidadT.equals("1") && multiplicidad.equals("1")))|| tipoRelacion.equals("agregacion")|| tipoRelacion.equals("composicion");
    }

    public ReqRes deleteAtributo(int atributoId) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<Atributo> atributoOpcional = atributoRepo.findById(atributoId);
            if (atributoOpcional.isPresent()) {
                    atributoRepo.deleteById(atributoId);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Atributo deleted successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("Atributo not found for deletion");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while deleting Atributo: " + e.getMessage());
        }
        return reqRes;
    }

    public ReqRes normalizar(ReqRes reqRes) {
        ReqRes response = new ReqRes();
        try {
            Optional<Atributo> atributoOpcional = atributoRepo.findById(reqRes.getAtributoId());
            if (atributoOpcional.isPresent()) {
                atributoRepo.deleteById(reqRes.getAtributoId());
                Tabla tabla = tablaRepo.findById(reqRes.getTablaId()).orElseThrow();
                Tabla newTable = new Tabla();
                newTable.setProyecto(tabla.getProyecto());
                newTable.setName(reqRes.getName());
                newTable.setPosicion_x(tabla.getPosicion_x() + 150);
                newTable.setPosicion_y(tabla.getPosicion_y() + 150);
                String tabColor = String.format("#%06X", (int) (Math.random() * 0xFFFFFF));
                newTable.setTabcolor(tabColor);
                Tabla tablaGuardada = tablaRepo.save(newTable);
                for (AtributoDto atributoDTO : reqRes.getAtributos()) {
                    Atributo newAtributo = new Atributo();
                    Scope scope = scopeRepo.findByNombre(atributoDTO.getScope())
                            .orElseThrow(() -> new RuntimeException("Scope no encontrado: " + atributoDTO.getScope()));
                    Tipo_Dato tipoDato = tipoDatoRepo.findByNombre(atributoDTO.getTipoDato())
                            .orElseThrow(() -> new RuntimeException("Tipo de dato no encontrado: " + atributoDTO.getTipoDato()));
                    newAtributo.setNombre(atributoDTO.getNombre());
                    newAtributo.setNulleable(true);
                    newAtributo.setPk(false);
                    newAtributo.setTabla(tablaGuardada);
                    newAtributo.setScope(scope);
                    newAtributo.setTipoDato(tipoDato);
                    atributoRepo.save(newAtributo);
                }
                Relacion relacion = new Relacion();
                Tipo tipo = tipoRepo.findByNombre("asociacion")
                        .orElseThrow(() -> new RuntimeException("Tipo de dato no encontrado: " + relacion.getTipo()));
                if(reqRes.getMultsource().equals("primera")) {
                    relacion.setMultsource("1");
                    relacion.setMulttarget("1..*");
                }else if (reqRes.getMultsource().equals("segunda")){
                    relacion.setMultsource("1");
                    relacion.setMulttarget("1");
                } else{
                    relacion.setMultsource("1..*");
                    relacion.setMulttarget("1");
                }
                    relacion.setTablaSource(tabla);
                    relacion.setTablaTarget(tablaGuardada);

                    relacion.setTipo(tipo);
                    String sourceName = "";
                    String targetName = "";
                    final double UMBRAL = 100.0;

                    if (Math.abs(tabla.getPosicion_y() - tablaGuardada.getPosicion_y()) > UMBRAL) {
                        if (tabla.getPosicion_y() < tablaGuardada.getPosicion_y()) {
                            sourceName = "bottom";
                            targetName = "top";
                        } else {
                            sourceName = "top";
                            targetName = "bottom";
                        }
                    } else if (Math.abs(tabla.getPosicion_x() - tablaGuardada.getPosicion_x()) > UMBRAL) {
                        if (tabla.getPosicion_x() < tablaGuardada.getPosicion_x()) {
                            sourceName = "right";
                            targetName = "left";
                        } else {
                            sourceName = "left";
                            targetName = "right";
                        }
                    } else {
                        sourceName = "left";
                        targetName = "left";
                    }
                    relacion.setSourceName(sourceName);
                    relacion.setTargetName(targetName);
                    Random random = new Random();
                    int targetValue = random.nextInt(100) - 50;
                    int sourceValue = random.nextInt(100) - 50;
                    if ("top".equals(targetName) || "bottom".equals(targetName)) {
                        relacion.setTargetArgs("{ dx: " + targetValue + " }");
                        relacion.setSourceArgs("{ dx: " + sourceValue + " }");
                    } else {
                        relacion.setTargetArgs("{ dy: " + targetValue + " }");
                        relacion.setSourceArgs("{ dy: " + sourceValue + " }");
                    }
                relacionRepo.save(relacion);
                response.setStatusCode(200);
                response.setMessage("Tabla creada correctamente");
            } else {
                response.setStatusCode(404);
                response.setMessage("Atributo not found for deletion");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while deleting Atributo: " + e.getMessage());
        }
        return response;
    }


}
