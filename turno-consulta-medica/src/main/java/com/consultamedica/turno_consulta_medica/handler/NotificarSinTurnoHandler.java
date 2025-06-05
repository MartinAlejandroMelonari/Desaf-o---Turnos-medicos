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
public class NotificarSinTurnoHandler {
    private static final Logger logger = LoggerFactory.getLogger(NotificarSinTurnoHandler.class);

    @JobWorker(type = "notificarSinTurno")
   public void handleVerificarCoberturaPaciente(final JobClient client,
                                                final ActivatedJob job,
                                                @Variable String mail,
                                                @Variable String num_socio) throws InterruptedException {
            try {
           logger.info("Notifiando al num_socio={} que no tiene turno, a traves de mail mail={}", num_socio, mail);

           String razonRechazo = null;

           // Simulación de reglas de negocio
           if (mail != null && !mail.trim().toLowerCase().endsWith("@gmail.com")){
               razonRechazo = "El mail ingresado no es valido, asi que no se puede enviar la notificación";
               logger.info("Resultado de la notificación:  mail={}, razonRechazo={}", mail, razonRechazo);
               throw new RuntimeException("El mail ingresado no es valido");
           }

           // Crear mapa de variables
           Map<String, Object> variables = new HashMap<>();
           variables.put("mail", mail);
           if (razonRechazo != null) {
               variables.put("razonRechazo", razonRechazo);
           }

           client.newCompleteCommand(job.getKey())
                   .variables(variables)
                   .send()
                   .join();

           logger.info("Resultado de la notificación:  mail={}, razonRechazo={}", mail, razonRechazo);
           
    } catch (Exception e) {
         
                logger.warn("Error debido a que el mail usado es invalido");
                client.newThrowErrorCommand(job)
                        .errorCode("mail_invalido_1")
                        .errorMessage("El mail usado es invalido")
                        .send()
                        .exceptionally(t -> {
                            throw new RuntimeException("No se pudo lanzar el error BPMN: " + t.getMessage(), t);
                        });
    }
}
}


