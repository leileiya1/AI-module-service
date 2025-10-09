package com.example.aichatservice.service;

import com.example.aichatservice.entity.Persona;
import com.example.dto.AI.ClonePersonaRequest;
import com.example.dto.AI.PersonaDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface PersonaManagementService {
    Persona createPersona(PersonaDto personaDto, String userId);

    Persona updatePersona(String personaId, PersonaDto personaDto, String userId);

    void deletePersona(String personaId, String userId);

    List<Persona> listPersonasForUser(String userId);

    Persona getPersonaById(String personaId);

    Persona clonePersonaFromSource(ClonePersonaRequest request, String userId) throws JsonProcessingException, InterruptedException;
}
