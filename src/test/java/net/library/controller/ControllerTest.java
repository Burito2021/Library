package net.library.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static net.library.util.HttpUtil.GLOBAL_BASE_URI;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getTestWithoutQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello GET METHOD!"));
    }

    @Test
    void getTestWithQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI)
                        .param("get", "GET"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello GET!"));
    }

    @Test
    void postTestWithoutQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello POST METHOD!"));
    }

    @Test
    void postTestWithQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI)
                        .param("post", "POST"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello POST!"));
    }

    @Test
    void putTestWithoutQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(GLOBAL_BASE_URI))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello PUT METHOD!"));
    }

    @Test
    void putTestWithQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(GLOBAL_BASE_URI)
                        .param("put", "PUT"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello PUT!"));
    }

    @Test
    void deleteTestWithoutQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(GLOBAL_BASE_URI))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello DELETE METHOD!"));
    }

    @Test
    void deleteTestWithQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(GLOBAL_BASE_URI)
                        .param("delete", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello DELETE!"));
    }

    @Test
    void patchWithoutQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello PATCH METHOD!"));
    }

    @Test
    void patchWithQueryParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI)
                        .param("patch", "PATCH"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello PATCH!"));
    }
}