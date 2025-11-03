package com.jsp.canteen_management_system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsp.canteen_management_system.controller.WorkerController;
import com.jsp.canteen_management_system.model.Worker;
import com.jsp.canteen_management_system.enums.WorkerRole;
import com.jsp.canteen_management_system.repository.WorkerRepository;
import com.jsp.canteen_management_system.service.CustomUserDetailsService;
import com.jsp.canteen_management_system.service.WorkerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(WorkerController.class)
@Import(com.jsp.canteen_management_system.config.SecurityConfig.class)
public class WorkerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkerService workerService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(worker)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe")));

        // Test GET request to list workers
        mockMvc.perform(get("/api/canteen/some-canteen-id/workers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("John Doe")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetWorkerById() throws Exception {
        Worker worker = new Worker();
        worker.setId("1");
        worker.setName("Jane Doe");
        worker.setRole(WorkerRole.LABOUR);
        worker.setCanteenId("some-canteen-id");

        given(workerService.findWorkerById(anyString(), anyString())).willReturn(Optional.of(worker));

        mockMvc.perform(get("/api/canteen/some-canteen-id/workers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jane Doe")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateWorker() throws Exception {
        Worker updatedWorker = new Worker();
        updatedWorker.setId("1");
        updatedWorker.setName("Updated Name");
        updatedWorker.setRole(WorkerRole.CASHIER);
        updatedWorker.setCanteenId("some-canteen-id");

        given(workerService.updateWorker(anyString(), anyString(), any(Worker.class))).willReturn(updatedWorker);

        mockMvc.perform(put("/api/canteen/some-canteen-id/workers/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedWorker)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.role", is("CASHIER")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteWorker() throws Exception {
        mockMvc.perform(delete("/api/canteen/some-canteen-id/workers/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
