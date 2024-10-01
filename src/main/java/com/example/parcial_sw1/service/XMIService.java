package com.example.parcial_sw1.service;

import com.example.parcial_sw1.dto.AtributoDto;
import com.example.parcial_sw1.dto.ReqRes;
import com.example.parcial_sw1.entity.*;
import com.example.parcial_sw1.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Random;

@Service
public class XMIService {
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
    public Integer crearProyectoConTablasAtributos(String email, ReqRes reqRes) throws Exception{
        OurUsers usuario = ourUsersRepo.findByEmail(email)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
        Proyecto nuevoProyecto = new Proyecto();
        nuevoProyecto.setTitulo(reqRes.getTitulo());
        nuevoProyecto.setDescripcion(reqRes.getDescripcion());
        nuevoProyecto.setCreador(usuario);
        nuevoProyecto.setUrlPreview("assets/imagenes/previews/preview1.jpg");
        Proyecto proyectoGuardado = proyectoRepo.save(nuevoProyecto);
        for (ReqRes tabla : reqRes.getTablas()) {
            Tabla nuevaTabla = new Tabla();
            nuevaTabla.setName(tabla.getName());
            nuevaTabla.setProyecto(proyectoGuardado);
            nuevaTabla.setPosicion_x(tabla.getPosicion_x());
            nuevaTabla.setPosicion_y(tabla.getPosicion_y());
            String tabColor = String.format("#%06X", (int) (Math.random() * 0xFFFFFF));
            nuevaTabla.setTabcolor(tabColor);
            Tabla tablaGuardada = tablaRepo.save(nuevaTabla);
            for (AtributoDto atributoDTO : tabla.getAtributos()) {
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

        }
        for (ReqRes relacion : reqRes.getRelaciones()) {
            Relacion nuevaRelacion = new Relacion();
            Tabla tablaSource = tablaRepo.findByProyectoAndName(proyectoGuardado, relacion.getTablaSource())
                    .orElseThrow(() -> new RuntimeException("Tabla source no encontrada: " + relacion.getTablaSource()));

            Tabla tablaTarget = tablaRepo.findByProyectoAndName(proyectoGuardado, relacion.getTablaTarget())
                    .orElseThrow(() -> new RuntimeException("Tabla target no encontrada: " + relacion.getTablaTarget()));
            Tipo tipo = tipoRepo.findByNombre(relacion.getTipo())
                    .orElseThrow(() -> new RuntimeException("Tipo de dato no encontrado: " + relacion.getTipo()));
            String sourceName = "";
            String targetName = "";

            // Comparar posiciones en Y
            // Definir un umbral para considerar diferencias significativas
            final double UMBRAL = 100.0; // Cambia este valor según tus necesidades

            if (Math.abs(tablaSource.getPosicion_y() - tablaTarget.getPosicion_y()) > UMBRAL) {
                if (tablaSource.getPosicion_y() < tablaTarget.getPosicion_y()) {
                    sourceName = "bottom";  // Source está arriba
                    targetName = "top";     // Target está abajo
                } else {
                    sourceName = "top";     // Source está abajo
                    targetName = "bottom";  // Target está arriba
                }
            } else if (Math.abs(tablaSource.getPosicion_x() - tablaTarget.getPosicion_x()) > UMBRAL) {
                if (tablaSource.getPosicion_x() < tablaTarget.getPosicion_x()) {
                    sourceName = "right";   // Source está a la izquierda
                    targetName = "left";    // Target está a la derecha
                } else {
                    sourceName = "left";    // Source está a la derecha
                    targetName = "right";   // Target está a la izquierda
                }
            } else {
                // Si las posiciones son prácticamente iguales, podrías manejarlo aquí
                sourceName = "left";   // Por ejemplo, están alineados
                targetName = "left";    // Podrías establecer nombres por defecto
            }

            nuevaRelacion.setTablaSource(tablaSource);
            nuevaRelacion.setTablaTarget(tablaTarget);
            nuevaRelacion.setMultsource(relacion.getMultsource());
            nuevaRelacion.setMulttarget(relacion.getMulttarget());
            nuevaRelacion.setDetalle(relacion.getDetalle());
            nuevaRelacion.setTipo(tipo);
            nuevaRelacion.setSourceName(sourceName);
            nuevaRelacion.setTargetName(targetName);
            Random random = new Random();
            int targetValue = random.nextInt(100) - 50; // Valor aleatorio entre -50 y 49
            int sourceValue = random.nextInt(100) - 50;
            if ("top".equals(targetName) || "bottom".equals(targetName)) {
                nuevaRelacion.setTargetArgs("{ dx: " + targetValue + " }");
                nuevaRelacion.setSourceArgs("{ dx: " + sourceValue + " }");
            } else if ("left".equals(targetName) || "right".equals(targetName)) {
                nuevaRelacion.setTargetArgs("{ dy: " + targetValue + " }");
                nuevaRelacion.setSourceArgs("{ dy: " + sourceValue + " }");
            }
            relacionRepo.save(nuevaRelacion);
        }
        return proyectoGuardado.getId();
    }

    public String generarxmi(int proyectoId) throws Exception {
        Proyecto proyecto = proyectoRepo.findById(proyectoId)
                .orElseThrow(() -> new Exception("Proyecto no encontrado"));
        String fileName = "project_" + proyecto.getTitulo() + ".xmi";
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write("<?xml version=\"1.0\" encoding=\"windows-1252\"?>\n");
        writer.write("<XMI xmi.version=\"1.1\" xmlns:UML=\"omg.org/UML1.3\" timestamp=\"2024-09-24 10:38:58\">\n");
        writer.write("\t<XMI.header>\n");
        writer.write("\t\t<XMI.documentation>\n");
        writer.write("\t\t\t<XMI.exporter>Enterprise Architect</XMI.exporter>\n");
        writer.write("\t\t\t<XMI.exporterVersion>2.5</XMI.exporterVersion>\n");
        writer.write("\t\t</XMI.documentation>\n");
        writer.write("\t</XMI.header>\n");
        writer.write("\t<XMI.content>\n");
        writer.write("\t\t<UML:Model name=\"EA Model\" xmi.id=\"MX_EAID_227F7DA7_5744_4a57_A772_7B83F5B7F9EB\">\n");
        writer.write("\t\t\t<UML:Namespace.ownedElement>\n");
        writer.write("\t\t\t\t<UML:Class name=\"EARootClass\" xmi.id=\"EAID_11111111_5487_4080_A7F4_41526CB0AA00\" isRoot=\"true\" isLeaf=\"false\" isAbstract=\"false\"/>\n");
        writer.write("\t\t\t\t<UML:Package name=\"Domain Model\" xmi.id=\"EAPK_227F7DA7_5744_4a57_A772_7B83F5B7F9EB\" isRoot=\"false\" isLeaf=\"false\" isAbstract=\"false\" visibility=\"public\">\n");

        // Propiedades de Domain Model
        writer.write("\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"parent\" value=\"EAPK_A6C84077_EB1C_49d1_96CD_8F4109AA267A\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_package_id\" value=\"2\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"created\" value=\"2024-09-07 19:57:49\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"modified\" value=\"2024-09-07 19:57:49\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"iscontrolled\" value=\"FALSE\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"lastloaddate\" value=\"2024-09-07 19:57:49\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"lastsavedate\" value=\"2024-09-07 19:57:49\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"isprotected\" value=\"FALSE\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"usedtd\" value=\"FALSE\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"logxml\" value=\"FALSE\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"tpos\" value=\"4\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"packageFlags\" value=\"isModel=1;VICON=3;CRC=0;\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"batchsave\" value=\"0\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"batchload\" value=\"0\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"phase\" value=\"1.0\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"status\" value=\"Proposed\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"author\" value=\"" + proyecto.getCreador().getName() + "\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"complexity\" value=\"1\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_stype\" value=\"Public\"/>\n");
        writer.write("\t\t\t\t\t\t<UML:TaggedValue tag=\"tpos\" value=\"4\"/>\n");
        writer.write("\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
        writer.write("\t\t\t\t\t<UML:Namespace.ownedElement>\n");
        for (Tabla table : proyecto.getTablas()) {
            String tableId = "EAID_" + table.getId();
            writer.write("\t\t\t\t\t<UML:Class name=\"" + table.getName() + "\" xmi.id=\"" + tableId + "\" visibility=\"public\" namespace=\"EAPK_227F7DA7_5744_4a57_A772_7B83F5B7F9EB\" isRoot=\"false\" isLeaf=\"false\" isAbstract=\"false\" isActive=\"false\">\n");
            writer.write("\t\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"isSpecification\" value=\"false\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_stype\" value=\"Class\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_ntype\" value=\"0\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"version\" value=\"1.0\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"package\" value=\"EAPK_227F7DA7_5744_4a57_A772_7B83F5B7F9EB\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"date_created\" value=\"2024-09-23 22:12:30\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"date_modified\" value=\"2024-09-23 22:13:15\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"gentype\" value=\"Java\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"tagged\" value=\"0\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"package_name\" value=\"Domain Model\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"phase\" value=\"1.0\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"author\" value=\"" + proyecto.getCreador().getName() + "\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"complexity\" value=\"1\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"product_name\" value=\"Java\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"status\" value=\"Proposed\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"tpos\" value=\"0\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_localid\" value=\"52\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_eleType\" value=\"element\"/>\n");
            writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"style\" value=\"BackColor=-1;BorderColor=-1;BorderWidth=-1;FontColor=-1;VSwimLanes=1;HSwimLanes=1;BorderStyle=0;\"/>\n");
            writer.write("\t\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
            if(!table.getAtributos().isEmpty()) {
                writer.write("\t\t\t\t\t\t<UML:Classifier.feature>\n");
                for (Atributo attribute : table.getAtributos()) {
                    writer.write("\t\t\t<UML:Attribute name=\""+ attribute.getNombre() + "\" xmi.id=\"EAID_"+ generateUniqueId() +"\" visibility=\""+ attribute.getScope().getNombre() +"\" namespace=\"EAPK_227F7DA7_5744_4a57_A772_7B83F5B7F9EB\" isRoot=\"false\" isLeaf=\"false\" isAbstract=\"false\" isActive=\"false\">\n");
                    writer.write("\t\t\t\t\t\t\t<UML:Attribute.initialValue>\n");
                    writer.write("\t\t\t\t\t\t\t\t<UML:Expression/>\n");
                    writer.write("\t\t\t\t\t\t\t</UML:Attribute.initialValue>\n");
                    writer.write("\t\t\t\t\t\t\t<UML:StructuralFeature.type>\n");
                    writer.write("\t\t\t\t\t\t\t\t<UML:Classifier xmi.idref=\"eaxmiid0\"/>\n");
                    writer.write("\t\t\t\t\t\t\t</UML:StructuralFeature.type>\n");
                    writer.write("\t\t\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
                    writer.write("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"type\" value=\"" + attribute.getTipoDato().getNombre() + "\"/>\n");
                    writer.write("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"containment\" value=\"Not Specified\"/>\n");
                    writer.write("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ordered\" value=\"0\"/>\n");
                    writer.write("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"collection\" value=\"false\"/>\n");
                    writer.write("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"position\" value=\"0\"/>\n");
                    writer.write("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"lowerBound\" value=\"1\"/>\n");
                    writer.write("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"upperBound\" value=\"1\"/>\n");
                    writer.write("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"duplicates\" value=\"0\"/>\n");
                    writer.write("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_guid\" value=\"{"+ generateUniqueId() + "}\"/>\n");
                    writer.write("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_localid\" value=\"78\"/>\n");
                    writer.write("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"styleex\" value=\"volatile=0;\"/>\n");
                    writer.write("\t\t\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
                    writer.write("\t\t\t\t\t\t</UML:Attribute>\n");
                }
                writer.write("\t\t\t\t\t\t</UML:Classifier.feature>\n");
            }
            writer.write("\t\t\t\t\t</UML:Class>\n");
        }
        List<Relacion> relaciones = relacionRepo.findAllByProyectoId(proyecto.getId());
        for (Relacion relation : relaciones) {
            if(relation.getTipo().getNombre().equals("herencia")) {
                writer.write("\t\t\t\t\t<UML:Generalization subtype=\"EAID_" + relation.getTablaSource().getId() + "\" supertype=\"EAID_" + relation.getTablaTarget().getId() + "\" xmi.id=\"EAID_6D360846_831E_480a_A003_087EA0BC82AD\" visibility=\"public\">\n");
                writer.write("\t\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"style\" value=\"3\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_type\" value=\"Generalization\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"direction\" value=\"Source -&gt; Destination\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"linemode\" value=\"3\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"linecolor\" value=\"-1\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"linewidth\" value=\"0\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"seqno\" value=\"0\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"headStyle\" value=\"0\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"lineStyle\" value=\"0\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_localid\" value=\"47\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_sourceName\" value=\""+ relation.getTablaSource().getName() +"\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_targetName\" value=\""+ relation.getTablaTarget().getName() + "\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_sourceType\" value=\"Class\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_targetType\" value=\"Class\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_sourceID\" value=\"52\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_targetID\" value=\"55\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"src_visibility\" value=\"Public\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"src_aggregation\" value=\"0\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"src_isOrdered\" value=\"false\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"src_targetScope\" value=\"instance\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"src_changeable\" value=\"none\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"src_isNavigable\" value=\"false\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"src_containment\" value=\"Unspecified\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"src_style\" value=\"Union=0;Derived=0;AllowDuplicates=0;\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"dst_visibility\" value=\"Public\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"dst_aggregation\" value=\"0\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"dst_isOrdered\" value=\"false\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"dst_targetScope\" value=\"instance\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"dst_changeable\" value=\"none\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"dst_isNavigable\" value=\"true\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"dst_containment\" value=\"Unspecified\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"dst_style\" value=\"Union=0;Derived=0;AllowDuplicates=0;\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"virtualInheritance\" value=\"0\"/>\n");
                writer.write("\t\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
                writer.write("\t\t\t\t\t</UML:Generalization>\n");

            }else{
                String tipo = "none";
                if(relation.getTipo().getNombre().equals("composicion")) {
                    tipo = "composite";
                }else {
                    if (relation.getTipo().getNombre().equals("agregacion")) {
                        tipo = "shared";
                    }
                }
                StringBuilder associationLine = new StringBuilder();
                associationLine.append("\t\t\t\t\t<UML:Association xmi.id=\"" + generateUniqueId() + "\" visibility=\"public\" isRoot=\"false\" isLeaf=\"false\" isAbstract=\"false\"");

                if (relation.getDetalle() != null) {
                    associationLine.append(" name=\"" + relation.getDetalle() + "\"");
                }

                associationLine.append(">\n");
                writer.write(associationLine.toString());
                writer.write("\t\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"style\" value=\"3\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_type\" value=\"Association\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"direction\" value=\"Unspecified\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"linemode\" value=\"3\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"linecolor\" value=\"-1\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"linewidth\" value=\"0\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"seqno\" value=\"0\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"headStyle\" value=\"0\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"lineStyle\" value=\"0\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_localid\" value=\"43\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_sourceName\" value=\""+ relation.getTablaSource().getName()+ "\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_targetName\" value=\""+ relation.getTablaTarget().getName() + "\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_sourceType\" value=\"Class\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_targetType\" value=\"Class\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_sourceID\" value=\"52\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_targetID\" value=\"53\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"lb\" value=\"1\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"mt\" value=\"label\"/>\n");
                writer.write("\t\t\t\t\t\t\t<UML:TaggedValue tag=\"rb\" value=\"1..*\"/>\n");
                writer.write("\t\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
                writer.write("\t\t\t\t\t\t<UML:Association.connection>\n");
                StringBuilder associationEndLineSource = new StringBuilder();
                associationEndLineSource.append("\t\t\t\t\t\t\t<UML:AssociationEnd visibility=\"public\"");
                if (relation.getMultsource() != null) {
                    associationEndLineSource.append(" multiplicity=\"" + relation.getMultsource() + "\"");
                }
                associationEndLineSource.append(" aggregation=\"none\" isOrdered=\"false\" targetScope=\"instance\" changeable=\"none\" isNavigable=\"true\" type=\"EAID_" + relation.getTablaSource().getId() + "\">\n");
                writer.write(associationEndLineSource.toString());
                writer.write("\t\t\t\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
                writer.write("\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"containment\" value=\"Unspecified\"/>\n");
                writer.write("\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"sourcestyle\" value=\"Union=0;Derived=0;AllowDuplicates=0;Owned=0;Navigable=Unspecified;\"/>\n");
                writer.write("\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_end\" value=\"source\"/>\n");
                writer.write("\t\t\t\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
                writer.write("\t\t\t\t\t\t\t</UML:AssociationEnd>\n");
                StringBuilder associationEndLine = new StringBuilder();
                associationEndLine.append("\t\t\t\t\t\t\t<UML:AssociationEnd visibility=\"public\"");
                if (relation.getMulttarget() != null) {
                    associationEndLine.append(" multiplicity=\"" + relation.getMulttarget() + "\"");
                }
                associationEndLine.append(" aggregation=\"" + tipo + "\" isOrdered=\"false\" targetScope=\"instance\" changeable=\"none\" isNavigable=\"true\" type=\"EAID_" + relation.getTablaTarget().getId() + "\">\n");
                writer.write(associationEndLine.toString());
                writer.write("\t\t\t\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
                writer.write("\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"containment\" value=\"Unspecified\"/>\n");
                writer.write("\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"deststyle\" value=\"Union=0;Derived=0;AllowDuplicates=0;Owned=0;Navigable=Unspecified;\"/>\n");
                writer.write("\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_end\" value=\"target\"/>\n");
                writer.write("\t\t\t\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
                writer.write("\t\t\t\t\t\t\t</UML:AssociationEnd>\n");
                writer.write("\t\t\t\t\t\t</UML:Association.connection>\n");
                writer.write("\t\t\t\t\t</UML:Association>\n");
            }
        }

        writer.write("\t\t\t\t</UML:Namespace.ownedElement>\n");
        writer.write("\t\t\t\t</UML:Package>\n");
        writer.write("\t\t\t<UML:DataType xmi.id=\"eaxmiid0\" name=\"int\" visibility=\"private\" isRoot=\"false\" isLeaf=\"false\" isAbstract=\"false\"/>\n");
        writer.write("\t\t\t</UML:Namespace.ownedElement>\n");
        writer.write("\t\t</UML:Model>\n");
        writer.write("\t\t<UML:Diagram name=\"Domain Model\" xmi.id=\"EAID_5C5EB82A_470D_44b5_9CA1_9A914B2D522F\" diagramType=\"ClassDiagram\" owner=\"EAPK_227F7DA7_5744_4a57_A772_7B83F5B7F9EB\" toolName=\"Enterprise Architect 2.5\">\n");
        writer.write("\t\t\t<UML:ModelElement.taggedValue>\n");
        writer.write("\t\t\t\t<UML:TaggedValue tag=\"version\" value=\"1.0\"/>\n");
        writer.write("\t\t\t\t<UML:TaggedValue tag=\"author\" value=\"Djben\"/>\n");
        writer.write("\t\t\t\t<UML:TaggedValue tag=\"created_date\" value=\"2024-09-07 19:57:49\"/>\n");
        writer.write("\t\t\t\t<UML:TaggedValue tag=\"modified_date\" value=\"2024-09-24 10:38:39\"/>\n");
        writer.write("\t\t\t\t<UML:TaggedValue tag=\"package\" value=\"EAPK_227F7DA7_5744_4a57_A772_7B83F5B7F9EB\"/>\n");
        writer.write("\t\t\t\t<UML:TaggedValue tag=\"type\" value=\"Logical\"/>\n");
        writer.write("\t\t\t\t<UML:TaggedValue tag=\"swimlanes\" value=\"locked=false;orientation=0;width=0;inbar=false;names=false;color=0;bold=false;fcol=0;tcol=-1;ofCol=-1;ufCol=-1;hl=0;ufh=0;cls=0;SwimlaneFont=lfh:-10,lfw:0,lfi:0,lfu:0,lfs:0,lfface=Calibri,lfe=0,lfo=0,lfchar=1,lfop=0,lfcp=0,lfq=0,lfpf=0,lfWidth=0;\"/>\n");
        writer.write("\t\t\t\t<UML:TaggedValue tag=\"matrixitems\" value=\"locked=false;matrixactive=false;swimlanesactive=true;kanbanactive=false;width=1;clrLine=0;\"/>\n");
        writer.write("\t\t\t\t<UML:TaggedValue tag=\"ea_localid\" value=\"1\"/>\n");
        writer.write("\t\t\t\t<UML:TaggedValue tag=\"EAStyle\" value=\"ShowPrivate=1;ShowProtected=1;ShowPublic=1;HideRelationships=0;Locked=0;Border=0;HighlightForeign=0;PackageContents=1;SequenceNotes=0;ScalePrintImage=0;PPgs.cx=0;PPgs.cy=0;DocSize.cx=827;DocSize.cy=1169;ShowDetails=0;Orientation=P;Zoom=100;ShowTags=0;OpParams=1;VisibleAttributeDetail=0;ShowOpRetType=1;ShowIcons=1;CollabNums=0;HideProps=0;ShowReqs=0;ShowCons=0;PaperSize=9;HideParents=0;UseAlias=0;HideAtts=0;HideOps=0;HideStereo=0;HideElemStereo=0;ShowTests=0;ShowMaint=0;ConnectorNotation=UML 2.0;ExplicitNavigability=0;ShowShape=1;AdvancedElementProps=1;AdvancedFeatureProps=1;AdvancedConnectorProps=1;m_bElementClassifier=1;ShowNotes=0;SuppressBrackets=0;SuppConnectorLabels=0;PrintPageHeadFoot=0;ShowAsList=0;\"/>\n");
        writer.write("\t\t\t\t<UML:TaggedValue tag=\"styleex\" value=\"SaveTag=4ED844BD;ExcludeRTF=0;DocAll=0;HideQuals=0;AttPkg=1;ShowTests=0;ShowMaint=0;SuppressFOC=1;MatrixActive=0;SwimlanesActive=1;KanbanActive=0;MatrixLineWidth=1;MatrixLineClr=0;MatrixLocked=0;TConnectorNotation=UML 2.0;TExplicitNavigability=0;AdvancedElementProps=1;AdvancedFeatureProps=1;AdvancedConnectorProps=1;m_bElementClassifier=1;ProfileData=;MDGDgm=;STBLDgm=;ShowNotes=0;VisibleAttributeDetail=0;ShowOpRetType=1;SuppressBrackets=0;SuppConnectorLabels=0;PrintPageHeadFoot=0;ShowAsList=0;SuppressedCompartments=;Theme=:119;\"/>\n");
        writer.write("\t\t\t</UML:ModelElement.taggedValue>\n");
        writer.write("\t\t\t<UML:Diagram.element>\n");
        for (Tabla table : proyecto.getTablas()) {
            // Suponiendo que table.getPosicion_x() retorna un Double
            Double posicionXDouble = table.getPosicion_x() / 1.5; // Obtener como Double
            Double posicionYDouble = table.getPosicion_y() / 1.5; // Obtener como Double
            int posicionX = (posicionXDouble != null) ? posicionXDouble.intValue() : 0; // Convertir a int, asegurando que no sea null
            int posicionY = (posicionYDouble != null) ? posicionYDouble.intValue() : 0;
            int right = posicionX + 90;
            int bottom = posicionY + 70;

            writer.write("\t\t\t\t<UML:DiagramElement geometry=\"Left=" + posicionX + ";Top=" + posicionY + ";Right=" + right + ";Bottom=" + bottom + ";\" subject=\"EAID_" + table.getId() + "\" seqno=\"1\" style=\"DUID=8157A93C;\"/>\n");

        }
        writer.write("\t\t\t\t<UML:DiagramElement geometry=\"SX=0;SY=0;EX=0;EY=0;Path=;\" subject=\"EAID_3C422ED5_E7FB_493b_9BDC_0300D776BECE\" style=\";Hidden=0;\"/>\n");
        writer.write("\t\t\t</UML:Diagram.element>\n");
        writer.write("\t\t</UML:Diagram>\n");
        writer.write("\t</XMI.content>\n");
        writer.write("\t<XMI.difference/>\n");
        writer.write("\t<XMI.extensions xmi.extender=\"Enterprise Architect 2.5\">\n");
        writer.write("\t\t<EAModel.paramSub/>\n");
        writer.write("\t</XMI.extensions>\n");
        writer.write("</XMI>\n");
        writer.close();
        return fileName;
    }
    private String generateUniqueId() {
        return java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
