package com.jsp.canteen_management_system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsp.canteen_management_system.controller.WorkerController;
import com.jsp.canteen_management_system.model.Worker;
import com.jsp.canteen_management_system.enums.WorkerRole;
import com.jsp.canteen_management_system.repository.WorkerRepository;
import com.jsp.canteen_management_system.service.CustomUserDetailsService;
import com.jsp.canteen_management_system.service.WorkerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(MockitoExtension.class)
public class WorkerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkerService workerService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @InjectMocks
    private WorkerController workerController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(workerController).build();
    }

    @Test
    void testCreateAndListWorkers() throws Exception {
        Worker worker = new Worker();
        worker.setId("1");
        worker.setName("John Doe");
        worker.setRole(WorkerRole.CASHIER);
        worker.setCanteenId("some-canteen-id");

        given(workerService.saveWorker(anyString(), any(Worker.class))).willReturn(worker);
        given(workerService.findAllWorkersByCanteenId(anyString())).willReturn(Collections.singletonList(worker));

        // Test POST request to create a worker
    mockMvc.perform(post("/api/canteen/some-canteen-id/workers")
            .with(csrf())
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(worker)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe")));

        // Test GET request to list workers
    mockMvc.perform(get("/api/canteen/some-canteen-id/workers")
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("John Doe")));
    }

    @Test
    void testGetWorkerById() throws Exception {
        Worker worker = new Worker();
        worker.setId("1");
        worker.setName("Jane Doe");
        worker.setRole(WorkerRole.LABOUR);
        worker.setCanteenId("some-canteen-id");

        given(workerService.findWorkerById(anyString(), anyString())).willReturn(Optional.of(worker));

    mockMvc.perform(get("/api/canteen/some-canteen-id/workers/1")
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jane Doe")));
    }

    @Test
    void testUpdateWorker() throws Exception {
        Worker updatedWorker = new Worker();
        updatedWorker.setId("1");
        updatedWorker.setName("Updated Name");
        updatedWorker.setRole(WorkerRole.CASHIER);
        updatedWorker.setCanteenId("some-canteen-id");

        given(workerService.updateWorker(anyString(), anyString(), any(Worker.class))).willReturn(updatedWorker);

    mockMvc.perform(put("/api/canteen/some-canteen-id/workers/1")
            .with(csrf())
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedWorker)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.role", is("CASHIER")));
    }

    @Test
    void testDeleteWorker() throws Exception {
        mockMvc.perform(delete("/api/canteen/some-canteen-id/workers/1")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
