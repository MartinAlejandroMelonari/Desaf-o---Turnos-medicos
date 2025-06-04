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
public class RevisarAgendaHandler {
    private static final Logger logger = LoggerFactory.getLogger(RevisarAgendaHandler.class);

    @JobWorker(type = "revisarAgenda")
   public void handleVerificarCoberturaPaciente(final JobClient client,
                                                final ActivatedJob job,
                                                @Variable String especialidad,
                                                @Variable String motivo,
                                                @Variable String fecha_turno,
                                                @Variable Boolean turnoDisponible) throws InterruptedException {
       try {
           logger.info("Consultando turno para el día fecha_turno={} con la agenda (motivo={}, especialidad={})", fecha_turno);

           String razonRechazo = null;

           // Simulación de reglas de negocio
           if (Boolean.FALSE.equals(turnoDisponible)){
               razonRechazo = "El turno no está disponible";
           }

           // Crear mapa de variables
           Map<String, Object> variables = new HashMap<>();
           variables.put("turnoDisponible", turnoDisponible);
           if (razonRechazo != null) {
               variables.put("razonRechazo", razonRechazo);
           }

           client.newCompleteCommand(job.getKey())
                   .variables(variables)
                   .send()
                   .join();

           logger.info("Resultado de la consulta del turno: turnoDisponible={}, razonRechazo={}", turnoDisponible, razonRechazo);

       } catch (Exception e) {
           logger.error("Error técnico al revisar la agenda", e);
           client.newFailCommand(job.getKey())
                   .retries(job.getRetries() - 1)
                   .errorMessage("Error técnico: " + e.getMessage())
                   .send()
                   .join();

           throw new InterruptedException("Error técnico: " + e.getMessage());
       } 
   }
}
