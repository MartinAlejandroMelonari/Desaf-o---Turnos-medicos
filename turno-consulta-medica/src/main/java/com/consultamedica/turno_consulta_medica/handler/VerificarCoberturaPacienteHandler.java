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
public class VerificarCoberturaPacienteHandler {

   private static final Logger logger = LoggerFactory.getLogger(VerificarCoberturaPacienteHandler.class);

   @JobWorker(type = "verificarCoberturaPaciente")
   public void handleVerificarCoberturaPaciente(final JobClient client,
                                                final ActivatedJob job,
                                                @Variable String num_socio,
                                                @Variable Boolean apto) throws InterruptedException {

           logger.info("Verificando cobertura para num_socio={}", num_socio);

           String razonRechazo = null;

           // Simulación de reglas de negocio
           if (apto == false) {
               razonRechazo = "El paciente no está habilitado para solicitar turnos";
           }

           // Crear mapa de variables
           Map<String, Object> variables = new HashMap<>();
           variables.put("apto", apto);
           if (razonRechazo != null) {
               variables.put("razonRechazo", razonRechazo);
           }

           client.newCompleteCommand(job.getKey())
                   .variables(variables)
                   .send()
                   .join();

           logger.info("Resultado de cobertura: apto={}, razonRechazo={}", apto, razonRechazo);

       }
}


