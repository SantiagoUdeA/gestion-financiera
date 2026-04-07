package com.finanzas.gestion_financiera.unit.service;

import com.finanzas.gestion_financiera.entity.Category;
import com.finanzas.gestion_financiera.entity.Category.TipoCategoria;
import com.finanzas.gestion_financiera.entity.User;
import com.finanzas.gestion_financiera.repository.CategoryRepository;
import com.finanzas.gestion_financiera.service.CategoryInitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryInitService - Unit Tests")
class CategoryInitServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryInitService categoryInitService;

    @Captor
    private ArgumentCaptor<List<Category>> categoriesCaptor;

    @Test
    @DisplayName("Debe crear exactamente 10 categorías por defecto")
    void debeCrear10CategoriasPorDefecto() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@email.com");

        // Act
        categoryInitService.crearCategoriasPorDefecto(user);

        // Assert
        verify(categoryRepository).saveAll(categoriesCaptor.capture());
        List<Category> categorias = categoriesCaptor.getValue();
        assertEquals(10, categorias.size());
    }

    @Test
    @DisplayName("Debe crear 4 categorías de tipo INGRESO")
    void debeCrear4CategoriasIngreso() {
        // Arrange
        User user = new User();
        user.setId(1L);

        // Act
        categoryInitService.crearCategoriasPorDefecto(user);

        // Assert
        verify(categoryRepository).saveAll(categoriesCaptor.capture());
        long ingresos = categoriesCaptor.getValue().stream()
                .filter(c -> c.getTipo() == TipoCategoria.INGRESO)
                .count();
        assertEquals(4, ingresos);
    }

    @Test
    @DisplayName("Debe crear 6 categorías de tipo GASTO")
    void debeCrear6CategoriasGasto() {
        // Arrange
        User user = new User();
        user.setId(1L);

        // Act
        categoryInitService.crearCategoriasPorDefecto(user);

        // Assert
        verify(categoryRepository).saveAll(categoriesCaptor.capture());
        long gastos = categoriesCaptor.getValue().stream()
                .filter(c -> c.getTipo() == TipoCategoria.GASTO)
                .count();
        assertEquals(6, gastos);
    }

    @Test
    @DisplayName("Todas las categorías deben estar asociadas al usuario proporcionado")
    void todasDebenEstarAsociadasAlUsuario() {
        // Arrange
        User user = new User();
        user.setId(5L);
        user.setEmail("owner@email.com");

        // Act
        categoryInitService.crearCategoriasPorDefecto(user);

        // Assert
        verify(categoryRepository).saveAll(categoriesCaptor.capture());
        boolean allBelongToUser = categoriesCaptor.getValue().stream()
                .allMatch(c -> c.getUsuario().equals(user));
        assertTrue(allBelongToUser);
    }

    @Test
    @DisplayName("Debe incluir las categorías de ingreso esperadas")
    void debeIncluirCategoriasIngresoEsperadas() {
        // Arrange
        User user = new User();
        user.setId(1L);

        // Act
        categoryInitService.crearCategoriasPorDefecto(user);

        // Assert
        verify(categoryRepository).saveAll(categoriesCaptor.capture());
        List<String> nombres = categoriesCaptor.getValue().stream()
                .filter(c -> c.getTipo() == TipoCategoria.INGRESO)
                .map(Category::getNombre)
                .toList();
        assertTrue(nombres.contains("Salario"));
        assertTrue(nombres.contains("Freelance"));
        assertTrue(nombres.contains("Inversiones"));
        assertTrue(nombres.contains("Otros ingresos"));
    }

    @Test
    @DisplayName("Debe incluir las categorías de gasto esperadas")
    void debeIncluirCategoriasGastoEsperadas() {
        // Arrange
        User user = new User();
        user.setId(1L);

        // Act
        categoryInitService.crearCategoriasPorDefecto(user);

        // Assert
        verify(categoryRepository).saveAll(categoriesCaptor.capture());
        List<String> nombres = categoriesCaptor.getValue().stream()
                .filter(c -> c.getTipo() == TipoCategoria.GASTO)
                .map(Category::getNombre)
                .toList();
        assertTrue(nombres.contains("Alimentación"));
        assertTrue(nombres.contains("Transporte"));
        assertTrue(nombres.contains("Vivienda"));
        assertTrue(nombres.contains("Salud"));
        assertTrue(nombres.contains("Entretenimiento"));
        assertTrue(nombres.contains("Educación"));
    }
}
