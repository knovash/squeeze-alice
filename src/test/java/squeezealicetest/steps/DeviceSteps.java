package squeezealicetest.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.knovash.squeezealice.Config;
import org.knovash.squeezealice.utils.ConfigLoader;
import squeezealicetest.utils.MainTest;
import squeezealicetest.utils.TestDevice;

import java.util.Map;

import static org.junit.Assert.assertTrue;

public class DeviceSteps {

    private static boolean initialized = false; // Статический флаг для контроля инициализации

    private static Config testConfig; // Конфиг для тестов

    private String getValueOrEmpty(Map<String, String> params, String key) {
        String value = params.getOrDefault(key, "");
        return (value == null || value.trim().isEmpty()) ? "" : value;
    }

    @Before
    public void beforeEachScenario() {
        if (!initialized) {
            MainTest.init();
            initialized = true;
        }
        TestDevice.resetTestState();
    }

    @Given("Создано тестовое устройство {string} с параметрами:")
    public void createTestDevice(String deviceName, DataTable dataTable) {
        Map<String, String> params = dataTable.transpose().asMap(String.class, String.class);

        TestDevice.addDevice(
                deviceName,
                getValueOrEmpty(params, "power"),
                getValueOrEmpty(params, "volume"),
                getValueOrEmpty(params, "channel")
        );
    }

    @When("Запускается тест {string}")
    public void runTest(String testName) {
        TestDevice.runTest(testName);
    }

    @Then("Проверяем корректность состояния устройств")
    public void verifyDevicesState() {
        assertTrue("Проверка состояний устройств не пройдена",
                TestDevice.getAllTestsResults()
                        .stream()
                        .allMatch(results -> results.stream().allMatch(Boolean::booleanValue)));
    }
}