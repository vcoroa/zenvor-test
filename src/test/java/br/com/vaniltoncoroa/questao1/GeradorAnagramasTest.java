package br.com.vaniltoncoroa.questao1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.util.List;

public class GeradorAnagramasTest {

    @Test
    public void testaComTresLetras() {
        char[] entrada = {'a', 'b', 'c'};
        List<String> resultado = GeradorAnagramas.gerarAnagramas(entrada);

        Assertions.assertEquals(6, resultado.size());
        Assertions.assertTrue(resultado.contains("abc"));
        Assertions.assertTrue(resultado.contains("cba"));
    }

    @Test
    public void testaComUmaLetra() {
        char[] entrada = {'x'};
        List<String> resultado = GeradorAnagramas.gerarAnagramas(entrada);

        Assertions.assertEquals(1, resultado.size());
        Assertions.assertEquals("x", resultado.getFirst());
    }

    @Test
    public void testaComDuasLetras() {
        char[] entrada = {'x', 'y'};
        List<String> resultado = GeradorAnagramas.gerarAnagramas(entrada);

        Assertions.assertEquals(2, resultado.size());
        Assertions.assertTrue(resultado.contains("xy"));
        Assertions.assertTrue(resultado.contains("yx"));
    }

    @Test
    public void testaEntradaVazia() {
        char[] entrada = {};

        Assertions.assertThrows(IllegalArgumentException.class, () -> GeradorAnagramas.gerarAnagramas(entrada));
    }

    @Test
    public void testaEntradaNula() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                GeradorAnagramas.gerarAnagramas(null));
    }

    @Test
    public void testaLetrasDuplicadas() {
        char[] entrada = {'a', 'a', 'b'};

        Assertions.assertThrows(IllegalArgumentException.class, () -> GeradorAnagramas.gerarAnagramas(entrada));
    }

    @Test
    public void testaCaracteresInvalidos() {
        char[] entrada = {'a', '1', 'b'};

        Assertions.assertThrows(IllegalArgumentException.class, () -> GeradorAnagramas.gerarAnagramas(entrada));
    }

    @Test
    public void testaLetrasMinusculasEMaiusculas() {
        char[] entrada = {'A', 'a'};

        Assertions.assertThrows(IllegalArgumentException.class, () -> GeradorAnagramas.gerarAnagramas(entrada));
    }
}
