package com.consultamedica.turno_consulta_medica.rest;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.camunda.client.CamundaClient;

@RestController
@RequestMapping("/api/proceso")
public class StartProcesoValidacionController {

   private static final Logger LOG = LoggerFactory.getLogger(StartProcesoValidacionController.class);
   private static final String PROCESS_ID = "registro_turnos"; // Asegúrate de que coincida con tu BPMN

   @Autowired
   private CamundaClient zeebe;

   @PostMapping("/start")
 public void startProcessInstance(@RequestBody Map<String, Object> variables) {

   LOG.info("Starting process `paymentProcess` with variables: " + variables);

   zeebe
       .newCreateInstanceCommand()
       .bpmnProcessId(PROCESS_ID)
       .latestVersion()
       .variables(variables)
       .send();
 }
}
