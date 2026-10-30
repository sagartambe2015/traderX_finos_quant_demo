package com.quantraksha.kite;

import static org.assertj.core.api.Assertions.assertThat;

import com.quantraksha.kite.api.LevelsController;
import com.quantraksha.kite.api.TradeCaptureController;
import com.quantraksha.kite.core.service.LevelCalculatorService;
import com.quantraksha.kite.core.service.TradeCaptureService;
import com.quantraksha.kite.core.session.KiteSessionService;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/** The whole app starts: config binds, JPA mappings are valid, every controller and service is wired. */
@SpringBootTest
@ActiveProfiles("it")
class ApplicationContextIT {

    @MockitoBean
    private KiteSessionService kite;

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context.getBean(TradeCaptureController.class)).isNotNull();
        assertThat(context.getBean(LevelsController.class)).isNotNull();
        assertThat(context.getBean(LevelCalculatorService.class)).isNotNull();
        assertThat(context.getBean(TradeCaptureService.class)).isNotNull();
    }

    @Test
    void jpaRegistersBothEntities() {
        EntityManagerFactory emf = context.getBean(EntityManagerFactory.class);
        assertThat(emf.getMetamodel().getEntities())
                .extracting(e -> e.getName())
                .contains("StrategyEntity", "TradeCaptureEntity");
    }
}
