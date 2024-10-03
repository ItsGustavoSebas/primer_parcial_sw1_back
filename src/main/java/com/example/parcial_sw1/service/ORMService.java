package com.example.parcial_sw1.service;

import com.example.parcial_sw1.entity.Atributo;
import com.example.parcial_sw1.entity.Proyecto;
import com.example.parcial_sw1.entity.Relacion;
import com.example.parcial_sw1.entity.Tabla;
import com.example.parcial_sw1.repository.ProyectoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.nio.file.*;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
@Service
public class ORMService {
    @Autowired
    private ProyectoRepo proyectoRepo;

    public String generarEntidadesEnCopia(int proyectoId) throws Exception {
        Proyecto proyecto = proyectoRepo.findById(proyectoId)
                .orElseThrow(() -> new Exception("Proyecto no encontrado"));
        String rutaOriginal = "/exportar/proyectonuevo";
        String rutaZip = "/exportar/zips";
        String rutaCopia = "/exportar/proyectonuevo_copia";
        copiarProyecto(rutaOriginal, rutaCopia);
        String rutaBaseEntity = rutaCopia + "/src/main/java/com/nuevo/proyectonuevo/entity";
        String rutaBaseRepository = rutaCopia + "/src/main/java/com/nuevo/proyectonuevo/repository";
        String rutaBaseService = rutaCopia + "/src/main/java/com/nuevo/proyectonuevo/service";
        String rutaBaseController = rutaCopia + "/src/main/java/com/nuevo/proyectonuevo/controller";
        crearDirectorioProyecto(rutaBaseEntity);
        crearDirectorioProyecto(rutaBaseRepository);
        crearDirectorioProyecto(rutaBaseService);
        crearDirectorioProyecto(rutaBaseController);
            List<Tabla> tablas = proyecto.getTablas();
        for (Tabla tabla : tablas) {
            String tableName = tabla.getName();
            String formattedTableName = tableName.substring(0, 1).toUpperCase() + tableName.substring(1).toLowerCase();
            generarEntidad(tabla, rutaBaseEntity, formattedTableName);
            generarRepository(rutaBaseRepository, formattedTableName);
            generarService(rutaBaseService, formattedTableName, tabla);
            generarController(rutaBaseController, formattedTableName);
        }
        comprimirDirectorio(rutaCopia, rutaZip);
        return rutaZip;
    }

    public void comprimirDirectorio(String rutaDirectorio, String rutaZip) throws IOException {
        Path dir = Paths.get(rutaDirectorio);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(rutaZip))) {
            Files.walk(dir).forEach(sourcePath -> {
                String zipEntryName = dir.relativize(sourcePath).toString();
                try {
                    if (Files.isDirectory(sourcePath)) {
                        if (!zipEntryName.isEmpty()) {
                            zos.putNextEntry(new ZipEntry(zipEntryName + "/"));
                        }
                    } else {
                        zos.putNextEntry(new ZipEntry(zipEntryName));
                        Files.copy(sourcePath, zos);
                    }
                    zos.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void copiarProyecto(String rutaOriginal, String rutaDestino) throws IOException {
        Path origen = Paths.get(rutaOriginal);
        Path destino = Paths.get(rutaDestino);
        if (Files.exists(destino)) {
            eliminarDirectorio(destino);
        }
        Files.createDirectories(destino);
        Files.walk(origen).forEach(sourcePath -> {
            try {
                Path destinoPath = destino.resolve(origen.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    if (!Files.exists(destinoPath)) {
                        Files.createDirectories(destinoPath);
                    }
                } else {
                    Files.copy(sourcePath, destinoPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void eliminarDirectorio(Path directorio) throws IOException {
        Files.walkFileTree(directorio, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void generarEntidad(Tabla tabla, String rutaBase, String formattedTableName) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.nuevo.proyectonuevo.entity;\n\n");
        sb.append("import jakarta.persistence.*;\n");
        sb.append("import com.fasterxml.jackson.annotation.JsonIgnore;\n");
        sb.append("import lombok.*;\n");
        sb.append("import java.util.*;\n");
        sb.append("@Setter\n");
        sb.append("@Getter\n");
        sb.append("@Entity\n");
        sb.append("@Table(name = \"").append(tabla.getName().toLowerCase()).append("\")\n");
        sb.append("public class ").append(formattedTableName).append(" {\n\n");
        sb.append("    @Id\n");
        sb.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
        sb.append("    private int id;\n");
        sb.append("\n");
        for (Atributo atributo : tabla.getAtributos()) {
            String tipoName = atributo.getTipoDato().getNombre();
            if (tipoName.equals("string") || tipoName.equals("date")) {
                tipoName = tipoName.substring(0, 1).toUpperCase() + tipoName.substring(1).toLowerCase();
            }
            sb.append("    ").append(atributo.getScope().getNombre().toLowerCase()).append(" ")
                    .append(tipoName).append(" ").append(atributo.getNombre().toLowerCase()).append(";\n\n");
        }

        sb.append("\n");

        for (Relacion relacion : tabla.getRelacionesTarget()) {
            if (esRelacionManyToOne(relacion)) {
                String tableSourceName = relacion.getTablaSource().getName().toLowerCase();
                sb.append("    @ManyToOne\n");
                sb.append("    @JoinColumn(name = \"").append(tableSourceName).append("_id\")\n");
                String formattedTableSourceName = tableSourceName.substring(0, 1).toUpperCase() + tableSourceName.substring(1);
                sb.append("    private ").append(formattedTableSourceName).append(" ").append(tableSourceName).append(";\n\n");

            } else if (esRelacionOneToMany(relacion)) {
                String tableSourceName = relacion.getTablaSource().getName().toLowerCase();
                sb.append("    @OneToMany(mappedBy = \"").append(tabla.getName().toLowerCase()).append("\", cascade = CascadeType.ALL, orphanRemoval = true)\n");
                String formattedTableSourceName = tableSourceName.substring(0, 1).toUpperCase() + tableSourceName.substring(1);
                sb.append("    @JsonIgnore");
                sb.append("    private List<").append(formattedTableSourceName).append("> ").append(tableSourceName).append("s;\n\n");
            }
        }

        for (Relacion relacion : tabla.getRelacionesSource()) {
            if (esRelacionOneToManySource(relacion)) {
                String tableTargetName = relacion.getTablaTarget().getName().toLowerCase();
                sb.append("    @OneToMany(mappedBy = \"").append(tabla.getName().toLowerCase()).append("\", cascade = CascadeType.ALL, orphanRemoval = true)\n");
                String formattedTableTargetName = tableTargetName.substring(0, 1).toUpperCase() + tableTargetName.substring(1);
                sb.append("    @JsonIgnore");
                sb.append("    private List<").append(formattedTableTargetName).append("> ").append(tableTargetName).append("s;\n\n");

            } else if (esRelacionManyToOneSource(relacion)) {
                String tableTargetName = relacion.getTablaTarget().getName().toLowerCase();
                sb.append("    @ManyToOne\n");
                sb.append("    @JoinColumn(name = \"").append(tableTargetName).append("_id\")\n");
                String formattedTableTargetName = tableTargetName.substring(0, 1).toUpperCase() + tableTargetName.substring(1);
                sb.append("    private ").append(formattedTableTargetName).append(" ").append(tableTargetName).append(";\n\n");

            } else if (esRelacionOneToOneSource(relacion)) {
                // Relación uno a uno
                String tableTargetName = relacion.getTablaTarget().getName().toLowerCase();
                sb.append("    @OneToOne\n");
                sb.append("    @JoinColumn(name = \"").append(tableTargetName).append("_id\")\n");  // Define la columna que actúa como FK
                String formattedTableTargetName = tableTargetName.substring(0, 1).toUpperCase() + tableTargetName.substring(1);
                sb.append("    private ").append(formattedTableTargetName).append(" ").append(tableTargetName).append(";\n\n");
            }
        }
        sb.append("}\n");
        String rutaArchivo = rutaBase + "/"+ formattedTableName  + ".java";
        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generarRepository(String rutaBase, String formattedTableName) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.nuevo.proyectonuevo.repository;\n\n");
        sb.append("import org.springframework.data.jpa.repository.JpaRepository;\n");
        sb.append("import com.nuevo.proyectonuevo.entity.").append(formattedTableName).append(";\n\n");
        sb.append("public interface ").append(formattedTableName).append("Repository extends JpaRepository<")
                .append(formattedTableName).append(", Integer> {\n\n");
        sb.append("}\n");

        String rutaArchivo = rutaBase + "/" + formattedTableName + "Repository.java";
        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            writer.write(sb.toString());
        }
    }

    private void generarService(String rutaBase, String formattedTableName, Tabla tabla) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.nuevo.proyectonuevo.service;\n\n");
        sb.append("import java.util.List;\n");
        sb.append("import java.util.Optional;\n");
        sb.append("import com.nuevo.proyectonuevo.entity.*;\n");
        sb.append("import com.nuevo.proyectonuevo.repository.*;\n");
        sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        sb.append("import org.springframework.stereotype.Service;\n\n");
        sb.append("@Service\n");
        sb.append("public class ").append(formattedTableName).append("Service {\n\n");
        sb.append("    @Autowired\n");
        sb.append("    private ").append(formattedTableName).append("Repository repository;\n\n");
        for (Relacion relacion : tabla.getRelacionesTarget()) {
            if (esRelacionManyToOne(relacion)) {
                String tableSourceName = relacion.getTablaSource().getName().toLowerCase();
                String formattedTableTargetName = tableSourceName.substring(0, 1).toUpperCase() + tableSourceName.substring(1);
                sb.append("    @Autowired\n");
                sb.append("    private ").append(formattedTableTargetName).append("Repository ").append(tableSourceName).append("Repository ").append(";\n\n");
            }
        }
        for (Relacion relacion : tabla.getRelacionesSource()) {
            if (esRelacionManyToOneSource(relacion) || esRelacionOneToOneSource(relacion)) {
                String tableTargetName = relacion.getTablaTarget().getName().toLowerCase();
                String formattedTableTargetName = tableTargetName.substring(0, 1).toUpperCase() + tableTargetName.substring(1);
                sb.append("    @Autowired\n");
                sb.append("    private ").append(formattedTableTargetName).append("Repository ").append(tableTargetName).append("Repository ").append(";\n\n");
            }
        }
        sb.append("    \n\n");
        sb.append("    public List<").append(formattedTableName).append("> findAll() {\n");
        sb.append("        return repository.findAll();\n");
        sb.append("    }\n\n");
        sb.append("    public Optional<").append(formattedTableName).append("> findById(int id) {\n");
        sb.append("        return repository.findById(id);\n");
        sb.append("    }\n\n");
        sb.append("    public ").append(formattedTableName).append(" save(").append(formattedTableName).append(" entity) {\n");
        sb.append("        ").append(formattedTableName).append(" nuevo").append(formattedTableName).append(" = new ").append(formattedTableName).append("();\n");
        for (Atributo atributo : tabla.getAtributos()) {
            String nombreAtributo = atributo.getNombre();
            String nombreFormateado = nombreAtributo.substring(0, 1).toUpperCase() + nombreAtributo.substring(1).toLowerCase();
            sb.append("        nuevo").append(formattedTableName).append(".set").append(nombreFormateado).append("(entity.get").append(nombreFormateado).append("());\n");

        }
        for (Relacion relacion : tabla.getRelacionesTarget()) {
            if (esRelacionManyToOne(relacion)) {
                String tableSourceName = relacion.getTablaSource().getName().toLowerCase();
                String formattedTableSourceName = tableSourceName.substring(0, 1).toUpperCase() + tableSourceName.substring(1);
                sb.append("        ").append(formattedTableSourceName).append(" ").append(tableSourceName).append(" = ")
                        .append(tableSourceName).append("Repository ").append(".findById(entity.get").append(formattedTableSourceName).append("().getId())\n");
                sb.append("                .orElseThrow(() -> new RuntimeException(\"").append(tableSourceName).append(" no encontrado: \" + entity.get").append(formattedTableSourceName).append("().getId()));\n");
                sb.append("        nuevo").append(formattedTableName).append(".set").append(formattedTableSourceName).append("(").append(tableSourceName).append(");\n");
            }
        }
        for (Relacion relacion : tabla.getRelacionesSource()) {
            if (esRelacionManyToOneSource(relacion) || esRelacionOneToOneSource(relacion)) {
                String tableTargetName = relacion.getTablaTarget().getName().toLowerCase();
                String formattedTableTargetName = tableTargetName.substring(0, 1).toUpperCase() + tableTargetName.substring(1);
                sb.append("        ").append(formattedTableTargetName).append(" ").append(tableTargetName).append(" = ")
                        .append(tableTargetName).append("Repository ").append(".findById(entity.get").append(formattedTableTargetName).append("().getId())\n");
                sb.append("                .orElseThrow(() -> new RuntimeException(\"").append(tableTargetName).append(" no encontrado: \" + entity.get").append(formattedTableTargetName).append("().getId()));\n");
                sb.append("        nuevo").append(formattedTableName).append(".set").append(formattedTableTargetName).append("(").append(tableTargetName).append(");\n");
            }
        }
        sb.append("        return repository.save(nuevo").append(formattedTableName).append(");\n");
        sb.append("    }\n\n");
        sb.append("    public ").append(formattedTableName).append(" edit(int id, ").append(formattedTableName).append(" entity) {\n");
        sb.append("        ").append(formattedTableName).append(" existente").append(formattedTableName).append(" = repository.findById(id)\n");
        sb.append("                .orElseThrow(() -> new RuntimeException(\"").append(formattedTableName).append(" no encontrado: \" + id));\n");
        for (Atributo atributo : tabla.getAtributos()) {
            String nombreAtributo = atributo.getNombre();
            String nombreFormateado = nombreAtributo.substring(0, 1).toUpperCase() + nombreAtributo.substring(1).toLowerCase();
            sb.append("        existente").append(formattedTableName)
                    .append(".set").append(nombreFormateado)
                    .append("(entity.get").append(nombreFormateado).append("());\n");

        }
        for (Relacion relacion : tabla.getRelacionesTarget()) {
            if (esRelacionManyToOne(relacion)) {
                String tableSourceName = relacion.getTablaSource().getName().toLowerCase();
                String formattedTableSourceName = tableSourceName.substring(0, 1).toUpperCase() + tableSourceName.substring(1);
                sb.append("        ").append(formattedTableSourceName).append(" ").append(tableSourceName).append(" = ")
                        .append(tableSourceName).append("Repository.findById(entity.get").append(formattedTableSourceName).append("().getId())\n");
                sb.append("                .orElseThrow(() -> new RuntimeException(\"").append(tableSourceName).append(" no encontrado: \" + entity.get").append(formattedTableSourceName).append("().getId()));\n");
                sb.append("        existente").append(formattedTableName).append(".set").append(formattedTableSourceName).append("(").append(tableSourceName).append(");\n");
            }
        }
        for (Relacion relacion : tabla.getRelacionesSource()) {
            if (esRelacionManyToOneSource(relacion) || esRelacionOneToOneSource(relacion)) {
                String tableTargetName = relacion.getTablaTarget().getName().toLowerCase();
                String formattedTableTargetName = tableTargetName.substring(0, 1).toUpperCase() + tableTargetName.substring(1);
                sb.append("        ").append(formattedTableTargetName).append(" ").append(tableTargetName).append(" = ")
                        .append(tableTargetName).append("Repository.findById(entity.get").append(formattedTableTargetName).append("().getId())\n");
                sb.append("                .orElseThrow(() -> new RuntimeException(\"").append(tableTargetName).append(" no encontrado: \" + entity.get").append(formattedTableTargetName).append("().getId()));\n");
                sb.append("        existente").append(formattedTableName).append(".set").append(formattedTableTargetName).append("(").append(tableTargetName).append(");\n");
            }
        }
        sb.append("        return repository.save(existente").append(formattedTableName).append(");\n");
        sb.append("    }\n\n");
        sb.append("    public void deleteById(int id) {\n");
        sb.append("        repository.deleteById(id);\n");
        sb.append("    }\n\n");
        sb.append("}\n");

        String rutaArchivo = rutaBase + "/" + formattedTableName + "Service.java";
        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            writer.write(sb.toString());
        }
    }

    private void generarController(String rutaBase, String formattedTableName) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.nuevo.proyectonuevo.controller;\n\n");
        sb.append("import java.util.List;\n");
        sb.append("import java.util.Optional;\n");
        sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        sb.append("import org.springframework.http.HttpStatus;\n");
        sb.append("import org.springframework.http.ResponseEntity;\n");
        sb.append("import org.springframework.web.bind.annotation.*;\n");
        sb.append("import com.nuevo.proyectonuevo.entity.*;\n");
        sb.append("import com.nuevo.proyectonuevo.service.*;\n\n");
        sb.append("@RestController\n");
        sb.append("@RequestMapping(\"/api/").append(formattedTableName.toLowerCase()).append("s\")\n");
        sb.append("public class ").append(formattedTableName).append("Controller {\n\n");

        sb.append("    @Autowired\n");
        sb.append("    private ").append(formattedTableName).append("Service ").append(formattedTableName.toLowerCase()).append("Service;\n\n");
        sb.append("    @GetMapping(\"/get\")\n");
        sb.append("    public ResponseEntity<List<").append(formattedTableName).append(">> getAll() {\n");
        sb.append("        List<").append(formattedTableName).append("> lista = ").append(formattedTableName.toLowerCase()).append("Service.findAll();\n");
        sb.append("        return ResponseEntity.ok(lista);\n");
        sb.append("    }\n\n");

        sb.append("    @GetMapping(\"/get/{id}\")\n");
        sb.append("    public ResponseEntity<").append(formattedTableName).append("> getById(@PathVariable int id) {\n");
        sb.append("        Optional<").append(formattedTableName).append("> result = ").append(formattedTableName.toLowerCase()).append("Service.findById(id);\n");
        sb.append("        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());\n");
        sb.append("    }\n\n");

        sb.append("    @PostMapping(\"/create\")\n");
        sb.append("    public ResponseEntity<").append(formattedTableName).append("> create(@RequestBody ").append(formattedTableName).append(" entity) {\n");
        sb.append("        ").append(formattedTableName).append(" nuevo = ").append(formattedTableName.toLowerCase()).append("Service.save(entity);\n");
        sb.append("        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);\n");
        sb.append("    }\n\n");

        sb.append("    @PutMapping(\"/edit/{id}\")\n");
        sb.append("    public ResponseEntity<").append(formattedTableName).append("> edit(@PathVariable int id, @RequestBody ").append(formattedTableName).append(" entity) {\n");
        sb.append("        try {\n");
        sb.append("            ").append(formattedTableName).append(" actualizado = ").append(formattedTableName.toLowerCase()).append("Service.edit(id, entity);\n");
        sb.append("            return ResponseEntity.ok(actualizado);\n");
        sb.append("        } catch (RuntimeException e) {\n");
        sb.append("            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();\n");
        sb.append("        }\n");
        sb.append("    }\n\n");

        sb.append("    @DeleteMapping(\"/delete/{id}\")\n");
        sb.append("    public ResponseEntity<Void> delete(@PathVariable int id) {\n");
        sb.append("        try {\n");
        sb.append("            ").append(formattedTableName.toLowerCase()).append("Service.deleteById(id);\n");
        sb.append("            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();\n");
        sb.append("        } catch (RuntimeException e) {\n");
        sb.append("            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();\n");
        sb.append("        }\n");
        sb.append("    }\n\n");

        sb.append("}\n");

        String rutaArchivo = rutaBase + "/" + formattedTableName + "Controller.java";
        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            writer.write(sb.toString());
        }
    }


    private boolean esRelacionOneToMany(Relacion relacion) {
        String multiplicidadSource = relacion.getMultsource(); // Ej: "1"
        String multiplicidadTarget = relacion.getMulttarget(); // Ej: "*"

        return multiplicidadSource.equals("1..*") && multiplicidadTarget.equals("1");
    }

    private boolean esRelacionManyToOne(Relacion relacion) {
        String multiplicidadSource = relacion.getMultsource(); // Ej: "1..*"
        String multiplicidadTarget = relacion.getMulttarget(); // Ej: "1"

        return multiplicidadSource.equals("1") && multiplicidadTarget.equals("1..*");
    }

    private boolean esRelacionOneToManySource(Relacion relacion) {
        String multiplicidadSource = relacion.getMultsource(); // Ej: "1"
        String multiplicidadTarget = relacion.getMulttarget(); // Ej: "*"

        return multiplicidadTarget.equals("1..*") && multiplicidadSource.equals("1");
    }

    private boolean esRelacionManyToOneSource(Relacion relacion) {
        String multiplicidadSource = relacion.getMultsource(); // Ej: "1..*"
        String multiplicidadTarget = relacion.getMulttarget(); // Ej: "1"

        return multiplicidadTarget.equals("1") && multiplicidadSource.equals("1..*");
    }

    private boolean esRelacionOneToOneSource(Relacion relacion) {
        String multiplicidadSource = relacion.getMultsource(); // Ej: "1"
        String multiplicidadTarget = relacion.getMulttarget(); // Ej: "1"

        return multiplicidadTarget.equals("1") && multiplicidadSource.equals("1");
    }

    public void crearDirectorioProyecto(String rutaDirectorio) {
        File directorio = new File(rutaDirectorio);
        if (!directorio.exists()) {
            directorio.mkdirs();  // Crea el directorio si no existe
            System.out.println("Directorio creado: " + rutaDirectorio);
        } else {
            System.out.println("Directorio ya existe: " + rutaDirectorio);
        }
    }
}
