Feature: Тестирование устройств
  Проверка основных сценариев работы с устройствами

  Scenario Outline: Проверка включения плеера в гостиной с параметрами <power>, <volume>, <channel>
    Given Создано тестовое устройство "Гостиная" с параметрами:
      | power   | volume   | channel   |
      | <power> | <volume> | <channel> |
    When Запускается тест "TEST 11 один плеер включить"
    Then Проверяем корректность состояния устройств

    Examples:
      | power | volume | channel |
      | off   |        |         |
#      | on    | 9      | 6       |
#      |       |        | 8       |
#      |       | 5      | 3       |

  Scenario Outline: Проверка двух устройств с параметрами
    Given Создано тестовое устройство "Гостиная" с параметрами:
      | power     | volume     | channel     |
      | <power_1> | <volume_1> | <channel_1> |
    And Создано тестовое устройство "Душ" с параметрами:
      | power     | volume     | channel     |
      | <power_2> | <volume_2> | <channel_2> |
    When Запускается тест "TEST 12 три устройства"
    Then Проверяем корректность состояния устройств

    Examples:
      | power_1 | volume_1 | channel_1 | power_2 | volume_2 | channel_2 |
      | off     |          |           | off     |          |           |
#      | on      |          |           | on      |          |           |
#      |         | 2        |           |         | 3        |           |
#      |         |          | 6         |         |          | 6         |
#      |         | 6        | 5         |         | 7        | 5         |
#      | on      | 3        | 2         | on      | 4        | 2         |
#      | on      |          | 7         | on      |          | 7         |
#      | on      | 7        | 9         | on      | 8        | 9         |
#
#      | on      | 3        | 2         | on      | 5        | 1         |
#      |         |          |           | off     |          |           |
#      |         |          |           | on      |          |           |


#  Scenario Outline: Проверка трех устройств с параметрами
#    Given Создано тестовое устройство "Спальня" с параметрами:
#      | power | volume | channel |
#      | <power_1> | <volume_1> | <channel_1> |
#    And Создано тестовое устройство "Душ" с параметрами:
#      | power | volume | channel |
#      | <power_2> | <volume_2> | <channel_2> |
#    And Создано тестовое устройство "Веранда" с параметрами:
#      | power | volume | channel |
#      | <power_3> | <volume_3> | <channel_3> |
#    When Запускается тест "TEST 12 три устройства"
#    Then Проверяем корректность состояния устройств
#
#    Examples:
#      | power_1 | volume_1 | channel_1         | power_2 | volume_2 | channel_2       | power_3 | volume_3 | channel_3 |
#      | on      | 10       | 5                 | off     |          |                 | on      | 15       | 8         |
#      | off     |          |                   | on      | 5        | 3               | on      | 20       | 1         |