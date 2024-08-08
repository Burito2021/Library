package net.library.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getTestWithoutQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello GET METHOD!"));
    }

    @Test
    void getTestWithQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api")
                        .param("get", "GET"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello GET!"));
    }

    @Test
    void postTestWithoutQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello POST METHOD!"));
    }

    @Test
    void postTestWithQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api")
                        .param("post", "POST"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello POST!"));
    }

    @Test
    void putTestWithoutQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello PUT METHOD!"));
    }

    @Test
    void putTestWithQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api")
                        .param("put", "PUT"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello PUT!"));
    }

    @Test
    void deleteTestWithoutQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello DELETE METHOD!"));
    }

    @Test
    void deleteTestWithQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api")
                        .param("delete", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello DELETE!"));
    }

    @Test
    void patchWithoutQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/api"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello PATCH METHOD!"));
    }

    @Test
    void patchWithQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/api")
                        .param("patch", "PATCH"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello PATCH!"));
    }
}