package squeezealicetest.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features",
        glue = "squeezealicetest.steps", // Правильный glue
        plugin = {"pretty", "html:target/cucumber-report.html"},
        tags = "@devices"
)
public class TestRunner {}