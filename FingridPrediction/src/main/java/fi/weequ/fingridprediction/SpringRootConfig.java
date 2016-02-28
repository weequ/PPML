package fi.weequ.fingridprediction;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ComponentScan({ "fi.weequ.fingridprediction.service" , "fi.weequ.fingridprediction.schedule"})
public class SpringRootConfig {
}
