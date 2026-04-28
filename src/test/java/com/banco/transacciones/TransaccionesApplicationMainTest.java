package com.banco.transacciones;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class TransaccionesApplicationMainTest {

    @Test
    void mainInvocaSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(
                            any(Class.class), any(String[].class)))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            TransaccionesApplication.main(new String[]{});

            mocked.verify(() -> SpringApplication.run(
                    TransaccionesApplication.class, new String[]{}));
        }
    }
}
