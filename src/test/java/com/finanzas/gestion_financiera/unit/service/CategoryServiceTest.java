package com.finanzas.gestion_financiera.unit.service;

import com.finanzas.gestion_financiera.dto.CategoryRequest;
import com.finanzas.gestion_financiera.dto.CategoryResponse;
import com.finanzas.gestion_financiera.entity.Category;
import com.finanzas.gestion_financiera.entity.Category.TipoCategoria;
import com.finanzas.gestion_financiera.entity.User;
import com.finanzas.gestion_financiera.repository.CategoryRepository;
import com.finanzas.gestion_financiera.repository.UserRepository;
import com.finanzas.gestion_financiera.service.CategoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService - Unit Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@email.com");
        testUser.setPrimer_nombre("Test");
        testUser.setApellido("User");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@email.com")
                .password("{noop}")
                .authorities(List.of())
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        lenient().when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(testUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("Debe crear categoría de tipo INGRESO correctamente")
        void debeCrearCategoriaIngreso() {
            // Arrange
            CategoryRequest request = new CategoryRequest();
            request.setNombre("Bonificación");
            request.setTipo(TipoCategoria.INGRESO);

            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
                Category c = invocation.getArgument(0);
                c.setId(1L);
                return c;
            });

            // Act
            CategoryResponse response = categoryService.crear(request);

            // Assert
            assertNotNull(response);
            assertEquals("Bonificación", response.getNombre());
            assertEquals(TipoCategoria.INGRESO, response.getTipo());
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("Debe crear categoría de tipo GASTO correctamente")
        void debeCrearCategoriaGasto() {
            // Arrange
            CategoryRequest request = new CategoryRequest();
            request.setNombre("Restaurantes");
            request.setTipo(TipoCategoria.GASTO);

            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
                Category c = invocation.getArgument(0);
                c.setId(2L);
                return c;
            });

            // Act
            CategoryResponse response = categoryService.crear(request);

            // Assert
            assertEquals("Restaurantes", response.getNombre());
            assertEquals(TipoCategoria.GASTO, response.getTipo());
        }
    }

    @Nested
    @DisplayName("listar()")
    class Listar {

        @Test
        @DisplayName("Debe listar categorías del usuario autenticado")
        void debeListarCategoriasDelUsuario() {
            // Arrange
            Category cat1 = new Category();
            cat1.setId(1L);
            cat1.setNombre("Salario");
            cat1.setTipo(TipoCategoria.INGRESO);
            cat1.setUsuario(testUser);

            Category cat2 = new Category();
            cat2.setId(2L);
            cat2.setNombre("Alimentación");
            cat2.setTipo(TipoCategoria.GASTO);
            cat2.setUsuario(testUser);

            when(categoryRepository.findByUsuarioId(1L)).thenReturn(List.of(cat1, cat2));

            // Act
            List<CategoryResponse> result = categoryService.listar();

            // Assert
            assertEquals(2, result.size());
            assertEquals("Salario", result.get(0).getNombre());
            assertEquals("Alimentación", result.get(1).getNombre());
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no tiene categorías")
        void debeRetornarListaVacia() {
            // Arrange
            when(categoryRepository.findByUsuarioId(1L)).thenReturn(List.of());

            // Act
            List<CategoryResponse> result = categoryService.listar();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("obtener()")
    class Obtener {

        @Test
        @DisplayName("Debe obtener categoría por ID del usuario autenticado")
        void debeObtenerCategoriaPorId() {
            // Arrange
            Category category = new Category();
            category.setId(5L);
            category.setNombre("Transporte");
            category.setTipo(TipoCategoria.GASTO);
            category.setUsuario(testUser);

            when(categoryRepository.findByIdAndUsuarioId(5L, 1L)).thenReturn(Optional.of(category));

            // Act
            CategoryResponse response = categoryService.obtener(5L);

            // Assert
            assertEquals(5L, response.getId());
            assertEquals("Transporte", response.getNombre());
            assertEquals(TipoCategoria.GASTO, response.getTipo());
        }

        @Test
        @DisplayName("Debe lanzar excepción si la categoría no existe")
        void debeLanzarExcepcionSiNoExiste() {
            // Arrange
            when(categoryRepository.findByIdAndUsuarioId(99L, 1L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> categoryService.obtener(99L));
            assertEquals("Categoría no encontrada", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("actualizar()")
    class Actualizar {

        @Test
        @DisplayName("Debe actualizar nombre y tipo de categoría existente")
        void debeActualizarCategoria() {
            // Arrange
            Category existing = new Category();
            existing.setId(3L);
            existing.setNombre("Vieja");
            existing.setTipo(TipoCategoria.GASTO);
            existing.setUsuario(testUser);

            CategoryRequest request = new CategoryRequest();
            request.setNombre("Nueva");
            request.setTipo(TipoCategoria.INGRESO);

            when(categoryRepository.findByIdAndUsuarioId(3L, 1L)).thenReturn(Optional.of(existing));
            when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            CategoryResponse response = categoryService.actualizar(3L, request);

            // Assert
            assertEquals("Nueva", response.getNombre());
            assertEquals(TipoCategoria.INGRESO, response.getTipo());
            verify(categoryRepository).save(existing);
        }

        @Test
        @DisplayName("Debe lanzar excepción al actualizar categoría inexistente")
        void debeLanzarExcepcionAlActualizarInexistente() {
            // Arrange
            CategoryRequest request = new CategoryRequest();
            request.setNombre("Test");
            request.setTipo(TipoCategoria.GASTO);

            when(categoryRepository.findByIdAndUsuarioId(99L, 1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> categoryService.actualizar(99L, request));
        }
    }

    @Nested
    @DisplayName("eliminar()")
    class Eliminar {

        @Test
        @DisplayName("Debe eliminar categoría existente del usuario")
        void debeEliminarCategoria() {
            // Arrange
            Category category = new Category();
            category.setId(4L);
            category.setNombre("Temporal");
            category.setTipo(TipoCategoria.GASTO);
            category.setUsuario(testUser);

            when(categoryRepository.findByIdAndUsuarioId(4L, 1L)).thenReturn(Optional.of(category));

            // Act
            categoryService.eliminar(4L);

            // Assert
            verify(categoryRepository).delete(category);
        }

        @Test
        @DisplayName("Debe lanzar excepción al eliminar categoría inexistente")
        void debeLanzarExcepcionAlEliminarInexistente() {
            // Arrange
            when(categoryRepository.findByIdAndUsuarioId(99L, 1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> categoryService.eliminar(99L));
        }
    }
}
