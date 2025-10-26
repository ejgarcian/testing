/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package json;

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
    
    // --- Creación de Configuración por Defecto (usando arrays) ---
    private static Config createDefaultConfig() {
        int cycleDuration = 50; 
        
        // Se inicializa el array directamente
        PData[] defaultProcesses = new PData[] {
            // name, bound, instructions, ioCicles, satisfyCicles, deviceToUse, priority
            new PData("P_CPU_1", "CPU", 500, 0, 0, 0, 1),
            new PData("P_IO_1", "I/O", 300, 100, 50, 2, 2),
            new PData("P_IO_2", "I/O", 450, 200, 75, 1, 3),
            new PData("P_CPU_2", "CPU", 800, 0, 0, 0, 1)
        };
        
        return new Config(cycleDuration, defaultProcesses);
    }
}
