package com.consultamedica.turno_consulta_medica.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import io.camunda.spring.client.annotation.JobWorker;
import io.camunda.spring.client.annotation.Variable;

@Component
public class ConfirmarTurnoHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConfirmarTurnoHandler.class);

    @JobWorker(type = "confirmarTurno", autoComplete = false)
    public void handleNotificarSinTurno(final JobClient client,
                                        final ActivatedJob job,
                                        @Variable String num_socio,
                                        @Variable String fecha_turno) {
         try {
            logger.info("Confirmando el turno para el paciente num_socio={} para el dia fecha_turno={}", num_socio, fecha_turno);

            String razonRechazo = null;

            // Simulación de reglas de negocio
           if (num_socio.equals("335782") && fecha_turno.equals("2025-06-04T10:00:00")){
               razonRechazo = "El turno solicitado se encuentra duplicado";
               logger.info("Resultado de la confirmación del turno: num_socio={}, fecha_turno={}, razonRechazo={}", num_socio, fecha_turno, razonRechazo);
               throw new RuntimeException("El turno está duplicado");
           }
           if (fecha_turno.equals("2025-05-04T10:00:00")){
                razonRechazo = "El turno solicitado tiene una fecha anterior a la actual";
                logger.info("Resultado de la confirmación del turno: num_socio={}, fecha_turno={}, razonRechazo={}", num_socio, fecha_turno, razonRechazo);
                throw new RuntimeException("La fecha es inválida");
           }

           // Crear mapa de variables
           Map<String, Object> variables = new HashMap<>();
           variables.put("fecha_turno", fecha_turno);

           client.newCompleteCommand(job.getKey())
                   .variables(variables)
                   .send()
                   .join();

           logger.info("Resultado de la confirmación del turno: num_socio={}, fecha_turno={}, razonRechazo={}", num_socio, fecha_turno, razonRechazo);

            // Simulamos error en la API
            //throw new RuntimeException("No se pudo acceder a la API");

        } catch (Exception e) {

            logger.warn("Posible error técnico detectado al confirmar el turno");

            Integer retriesObj = job.getRetries();
            int remainingRetries = (retriesObj != null) ? retriesObj : 0;
    
            if (remainingRetries > 1) {
                logger.warn("Error técnico detectado (retries: {})", remainingRetries - 1);
                client.newFailCommand(job.getKey())
                        .retries(remainingRetries - 1)
                        .errorMessage("Error técnico: " + e.getMessage())
                        .send()
                        .join(); // importante: join para esperar la respuesta
                        
            } else if (fecha_turno.equals("2025-05-04T10:00:00")) {
                logger.warn("Retries agotados, lanzando error BPMN (fecha_invalida)");
                client.newThrowErrorCommand(job)
                        .errorCode("fecha_invalida")
                        .errorMessage("La fecha ingresada es inválida")
                        .send()
                        .exceptionally(t -> {
                            throw new RuntimeException("No se pudo lanzar el error BPMN: " + t.getMessage(), t);
                        });
            } else if (num_socio.equals("335782") && fecha_turno.equals("2025-06-04T10:00:00")) {
                logger.warn("Retries agotados, lanzando error BPMN (turno_duplicado)");
                client.newThrowErrorCommand(job)
                        .errorCode("turno_duplicado")
                        .errorMessage("El turno se encuentra duplicado")
                        .send()
                        .exceptionally(t -> {
                            throw new RuntimeException("No se pudo lanzar el error BPMN: " + t.getMessage(), t);
                        });
                    }
    }
}
}
