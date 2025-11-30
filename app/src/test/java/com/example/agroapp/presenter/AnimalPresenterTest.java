package com.example.agroapp.presenter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.agroapp.dao.AnimalDAO;

@RunWith(MockitoJUnitRunner.class)
public class AnimalPresenterTest {
    
    @Mock
    private AnimalDAO mockAnimalDAO;
    
    @Mock
    private AnimalPresenter.AnimalView mockView;
    
    private AnimalPresenter presenter;
    
    @Before
    public void setUp() {
        presenter = new AnimalPresenter(mockAnimalDAO, mockView);
    }
    
    // CP-REG-011: Validación de fechas coherentes
    @Test
    public void testValidarFechasCoherentes_NacimientoAnteriorAIngreso_RetornaTrue() {
        // Fecha de nacimiento anterior a fecha de ingreso - debería ser válido
        boolean resultado = presenter.validarFechasCoherentes("01/01/2020", "15/06/2023");
        assertTrue(resultado);
        verify(mockView, never()).mostrarError(anyString());
    }
    
    @Test
    public void testValidarFechasCoherentes_NacimientoPosteriorAIngreso_RetornaFalse() {
        // Fecha de nacimiento posterior a fecha de ingreso - debería ser inválido
        boolean resultado = presenter.validarFechasCoherentes("15/06/2023", "01/01/2020");
        assertFalse(resultado);
        verify(mockView).mostrarError("Fecha de nacimiento debe ser anterior a fecha de ingreso");
    }
    
    @Test
    public void testValidarFechasCoherentes_FechasIguales_RetornaTrue() {
        // Fechas iguales - debería ser válido
        boolean resultado = presenter.validarFechasCoherentes("15/06/2023", "15/06/2023");
        assertTrue(resultado);
        verify(mockView, never()).mostrarError(anyString());
    }
    
    // CP-REG-009: Validación de precios negativos
    @Test
    public void testValidarPrecio_PrecioPositivo_RetornaTrue() {
        boolean resultado = presenter.validarPrecio(100.50, "Precio");
        assertTrue(resultado);
        verify(mockView, never()).mostrarError(anyString());
    }
    
    @Test
    public void testValidarPrecio_PrecioCero_RetornaTrue() {
        boolean resultado = presenter.validarPrecio(0, "Precio");
        assertTrue(resultado);
        verify(mockView, never()).mostrarError(anyString());
    }
    
    @Test
    public void testValidarPrecio_PrecioNegativo_RetornaFalse() {
        boolean resultado = presenter.validarPrecio(-50.0, "Precio");
        assertFalse(resultado);
        verify(mockView).mostrarError("Precio debe ser 0 o positivo");
    }
    
    // Validaciones de arete
    @Test
    public void testValidarArete_AreteVacio_RetornaFalse() {
        boolean resultado = presenter.validarArete("");
        assertFalse(resultado);
        verify(mockView).mostrarError("El número de arete es obligatorio");
    }
    
    @Test
    public void testValidarArete_AreteNulo_RetornaFalse() {
        boolean resultado = presenter.validarArete(null);
        assertFalse(resultado);
        verify(mockView).mostrarError("El número de arete es obligatorio");
    }
    
    @Test
    public void testValidarArete_AreteMenorA10Digitos_RetornaFalse() {
        boolean resultado = presenter.validarArete("12345");
        assertFalse(resultado);
        verify(mockView).mostrarError("El número de arete debe tener exactamente 10 caracteres");
    }
    
    @Test
    public void testValidarArete_AreteMayorA10Digitos_RetornaFalse() {
        boolean resultado = presenter.validarArete("12345678901");
        assertFalse(resultado);
        verify(mockView).mostrarError("El número de arete debe tener exactamente 10 caracteres");
    }
    
    @Test
    public void testValidarArete_AreteConLetras_RetornaFalse() {
        boolean resultado = presenter.validarArete("123456789A");
        assertFalse(resultado);
        verify(mockView).mostrarError("El número de arete debe contener solo números (10 dígitos)");
    }
    
    @Test
    public void testValidarArete_AreteValido_RetornaTrue() {
        boolean resultado = presenter.validarArete("1234567890");
        assertTrue(resultado);
        verify(mockView, never()).mostrarError(anyString());
    }
    
    @Test
    public void testDestruir_ExecutorServiceShutdown() {
        presenter.destruir();
        // No exceptions should be thrown
    }
}
