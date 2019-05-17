package de.glueckscrew.gluecksroulette.config;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class LuckyConfigTest {
    @Test
    public void configValueTypeTest() {
        LuckyConfig config = new LuckyConfig();

        assertThat(config.get(LuckyConfig.Key.WINDOW_HEIGHT).getValue(), instanceOf(Integer.class));
    }
}