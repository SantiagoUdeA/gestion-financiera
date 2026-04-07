package com.finanzas.gestion_financiera.feature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finanzas.gestion_financiera.config.JwtAuthFilter;
import com.finanzas.gestion_financiera.config.SecurityConfig;
import com.finanzas.gestion_financiera.controller.CategoryController;
import com.finanzas.gestion_financiera.dto.CategoryRequest;
import com.finanzas.gestion_financiera.dto.CategoryResponse;
import com.finanzas.gestion_financiera.entity.Category.TipoCategoria;
import com.finanzas.gestion_financiera.service.CategoryService;
import com.finanzas.gestion_financiera.service.JwtService;
import com.finanzas.gestion_financiera.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
@DisplayName("Category Feature - API /api/v1/categorias")
class CategoryControllerFeatureTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Nested
    @DisplayName("POST /api/v1/categorias")
    class CrearEndpoint {

        @Test
        @WithMockUser(username = "test@email.com")
        @DisplayName("Debe crear categoría y retornar 200")
        void debeCrearCategoriaExitosamente() throws Exception {
            // Arrange
            CategoryRequest request = new CategoryRequest();
            request.setNombre("Bonificación");
            request.setTipo(TipoCategoria.INGRESO);

            CategoryResponse response = new CategoryResponse(1L, "Bonificación", TipoCategoria.INGRESO);
            when(categoryService.crear(any(CategoryRequest.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/v1/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Bonificación"))
                    .andExpect(jsonPath("$.tipo").value("INGRESO"));
        }

        @Test
        @WithMockUser(username = "test@email.com")
        @DisplayName("Debe retornar 400 si el nombre está vacío")
        void debeRetornar400SiNombreVacio() throws Exception {
            // Arrange
            CategoryRequest request = new CategoryRequest();
            request.setNombre("");
            request.setTipo(TipoCategoria.GASTO);

            // Act & Assert
            mockMvc.perform(post("/api/v1/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "test@email.com")
        @DisplayName("Debe retornar 400 si falta el tipo")
        void debeRetornar400SiFaltaTipo() throws Exception {
            // Arrange
            String json = """
                    {"nombre": "Test"}
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/v1/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe retornar 401 si no está autenticado")
        void debeRetornar401SiNoAutenticado() throws Exception {
            // Arrange
            CategoryRequest request = new CategoryRequest();
            request.setNombre("Test");
            request.setTipo(TipoCategoria.GASTO);

            // Act & Assert
            mockMvc.perform(post("/api/v1/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categorias")
    class ListarEndpoint {

        @Test
        @WithMockUser(username = "test@email.com")
        @DisplayName("Debe listar categorías del usuario autenticado")
        void debeListarCategorias() throws Exception {
            // Arrange
            List<CategoryResponse> categorias = List.of(
                    new CategoryResponse(1L, "Salario", TipoCategoria.INGRESO),
                    new CategoryResponse(2L, "Alimentación", TipoCategoria.GASTO)
            );
            when(categoryService.listar()).thenReturn(categorias);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categorias"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].nombre").value("Salario"))
                    .andExpect(jsonPath("$[1].nombre").value("Alimentación"));
        }

        @Test
        @DisplayName("Debe retornar 401 si no está autenticado")
        void debeRetornar401SiNoAutenticado() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/categorias"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categorias/{id}")
    class ObtenerEndpoint {

        @Test
        @WithMockUser(username = "test@email.com")
        @DisplayName("Debe obtener categoría por ID")
        void debeObtenerCategoriaPorId() throws Exception {
            // Arrange
            CategoryResponse response = new CategoryResponse(5L, "Transporte", TipoCategoria.GASTO);
            when(categoryService.obtener(5L)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categorias/5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(5))
                    .andExpect(jsonPath("$.nombre").value("Transporte"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/categorias/{id}")
    class ActualizarEndpoint {

        @Test
        @WithMockUser(username = "test@email.com")
        @DisplayName("Debe actualizar categoría existente")
        void debeActualizarCategoria() throws Exception {
            // Arrange
            CategoryRequest request = new CategoryRequest();
            request.setNombre("Nombre Actualizado");
            request.setTipo(TipoCategoria.INGRESO);

            CategoryResponse response = new CategoryResponse(3L, "Nombre Actualizado", TipoCategoria.INGRESO);
            when(categoryService.actualizar(eq(3L), any(CategoryRequest.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(put("/api/v1/categorias/3")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Nombre Actualizado"));
        }

        @Test
        @WithMockUser(username = "test@email.com")
        @DisplayName("Debe retornar 400 si datos de actualización son inválidos")
        void debeRetornar400SiDatosInvalidos() throws Exception {
            // Arrange
            String json = """
                    {"nombre": "", "tipo": null}
                    """;

            // Act & Assert
            mockMvc.perform(put("/api/v1/categorias/3")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/categorias/{id}")
    class EliminarEndpoint {

        @Test
        @WithMockUser(username = "test@email.com")
        @DisplayName("Debe eliminar categoría y retornar 204")
        void debeEliminarCategoria() throws Exception {
            // Arrange
            doNothing().when(categoryService).eliminar(4L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/categorias/4"))
                    .andExpect(status().isNoContent());
            verify(categoryService).eliminar(4L);
        }

        @Test
        @DisplayName("Debe retornar 401 si no está autenticado")
        void debeRetornar401SiNoAutenticado() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/v1/categorias/4"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
