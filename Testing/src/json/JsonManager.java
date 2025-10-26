/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package json;

/**
 *
 * @author miche_ysmoa6e
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Eddy
 */
public class JsonManager {

    private static final String FILE_PATH = "config.json";
    
    // --- Lógica para GUARDAR el JSON ---
    public static void saveConfigToJson(Config config) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Usa el módulo de "pretty printing" para formatear el JSON con indentación
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), config);
            System.out.println("✅ Configuración guardada en: " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("❌ Error al guardar la configuración JSON: " + e.getMessage());
        }
    }

    // --- Lógica para CARGAR el JSON ---
    public static Config loadConfigFromJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                System.out.println("⚠️ Archivo de configuración no encontrado. Creando configuración por defecto...");
                Config defaultConfig = createDefaultConfig();
                saveConfigToJson(defaultConfig); 
                return defaultConfig;
            }
            
            Config config = mapper.readValue(file, Config.class);
            System.out.println("✅ Configuración cargada exitosamente desde: " + FILE_PATH);
            return config;
        } catch (IOException e) {
            System.err.println("❌ Error al cargar/parsear el archivo JSON: " + e.getMessage());
            return null;
        }
    }
    
    public void setProcessesFromArray(PData[] array) {
        
    }
    
    // --- Creación de Configuración por Defecto (usando arrays) ---
    private static Config createDefaultConfig() {
        int cycleDuration = 20;

        // Define default processes
        PData[] defaultProcesses = new PData[] {
            new PData("P_CPU_1", "CPU", 10, 0, 0, 0, 1),
            new PData("P_IO_1", "I/O", 25, 100, 50, 2, 2),
            new PData("P_IO_2", "I/O", 10, 200, 75, 1, 3),
            new PData("P_CPU_2", "CPU", 20, 0, 0, 0, 1)
        };

        /*
        // Create config with initial capacity
        Config config = new Config(cycleDuration, defaultProcesses);
        
        // Add each PData to the config's process list
        for (PData pd : defaultProcesses) {
            config.addProcess(pd);
        }
*/
        return null;
    }
}
