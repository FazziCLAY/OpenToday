# OpenToday
Language: **[[English](https://github.com/FazziCLAY/OpenToday/blob/main/README.md) | Русский]**

Android приложение для организации жизни, многофункциональные заметки и напоминалка!

[![license](https://img.shields.io/github/license/fazziclay/opentoday?color=%2300bb00&style=plastic)](https://github.com/FazziCLAY/OpenToday/blob/main/LICENSE)


[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/fazziclay/opentoday?style=plastic)](https://github.com/FazziCLAY/OpenToday/releases)
[![IzzyOnDroid](https://img.shields.io/endpoint?style=plastic&color=%2300bb00&url=https://apt.izzysoft.de/fdroid/api/v1/shield/com.fazziclay.opentoday)](https://apt.izzysoft.de/fdroid/index/apk/com.fazziclay.opentoday)

# Использование
Каждая плитка в приложении называется Айтемом (Item, Айтем).
Существуют разные типы айтемов, некоторые наследуют другие, добавляя новую функциональность. Так, например, "Ежедневная галочка" наследуется от элемента "Галочка".
* **Text** - Текст. Используется для простых текстовых заметок.
* **CheckBox** - Галочка. В связи с айтемом *Group*, можно создать удобный список дел, покупок в магазин и план на сегодня.
* **Group** - Группа. Содержит в себе другие айтемы. Никаких ограничений по глубине нету, вы можете создать свою самую лучшую иерархию айтемов!
* **Filter Group** - Группа с фильтром. Используйте для напоминаний о днях рождениях, списков дел на сегодня, различных расписаний (школьное например) и всё, что придёт в голову. Настраивать фильтр вы можете вплоть до секунд.

**и другие...**

# Скриншоты
**Еще больше идей как это использововать может прийти вам в голову после просмотра скриншотов**

| ![1](https://raw.githubusercontent.com/FazziCLAY/OpenToday/main/fastlane/metadata/android/en-US/images/phoneScreenshots/01.jpg) | ![1](https://raw.githubusercontent.com/FazziCLAY/OpenToday/main/fastlane/metadata/android/en-US/images/phoneScreenshots/02.jpg) | ![About app](https://raw.githubusercontent.com/FazziCLAY/OpenToday/main/fastlane/metadata/android/en-US/images/phoneScreenshots/03.jpg) | ![Calendar](https://raw.githubusercontent.com/FazziCLAY/OpenToday/main/fastlane/metadata/android/en-US/images/phoneScreenshots/04.jpg) |
|:-----------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------------------------------:|
| ![1](https://raw.githubusercontent.com/FazziCLAY/OpenToday/main/fastlane/metadata/android/en-US/images/phoneScreenshots/05.jpg) | ![1](https://raw.githubusercontent.com/FazziCLAY/OpenToday/main/fastlane/metadata/android/en-US/images/phoneScreenshots/06.jpg) |                                                                                                                     |                                                                                                                    |
## Toolbar (Тулбар)
Панель инструментов является важным элементом интерфейса. Он расположен в нижней части экрана

Вкратце о важных функциях:

**Добавить айтем** - **Тулбар->Айтемы** нажмите (+) напротив нужного типа создаваемого айтема.

--

**Перемещение** Проведите пальцем по нужному элементу вправо, установите флажок "Выделен", затем откройте нужное местоположение, откройте вкладку "Выделение" на панели инструментов и выберите там нужное действие

--

**Импорт и Экспорт** Поделитесь своими айтемами с друзьями, для экспорта вам необходимо нажать на соответствующую кнопку в меню панели инструментов "Выделение". Все выбранные элементы экспортируются в буфер обмена

Для импорта используйте скопированный текст на вкладке "Файл" на панели инструментов

--


# Техническая информация
Ещё больше в директории "/docs"

## Contribute
Я буду рад, если вы сделаете Pull Request с новой функцией или исправлением ошибки.

cмотрите "docs/CONTRIBUTING.md"

## Kotlin или Java?
Я хорошо знаю Java, и я только изучаю Kotlin.

При разработке я действую по такой логике:

Если я пишу Backend (работа с айтемами и т.д.), то Java

Если я пишу Frontend (GUI), то Kotlin предпочтительнее

## Дерево айтемов (родословная) (Английский-only)
```css
Item (implements Unique) - (minimal height, background color)
|
| Text - (text, text color)
  |
  | DebugTickCounter - (debug item...)
  | LongText - (long text, long text color)
  | Group (implements ContainerItem, ItemsStorage) - (items)
  | FilterGroup (implements ContainerItem, ItemsStorage) - (items)
  | CycleList (implements ContainerItem, ItemsStorage) - (items)
  | Counter - (current value, step)
  | MathGame - (primitive operations (+-*/))
  | Checkbox - (is checked)
     |
     | DayRepeatableCheckbox - (start value for 'is checked' in Checkbox, latest regenerate date)
```

## Todo/Ideas:
* [ ] Settings: ItemsStorage to add quick notes from the notification exactly there
* [ ] Toolbar->Selection -> SelectALL & ~~DeselectALL~~
* [ ] Settings -> minimize paddings (left, right, bottom, top)
* [ ] Replace checkboxItem to text item & add 'modules' to item and add Module 'checkbox' (what?)

Make a pull request -> you will be added to contributors.json and also I will create the contributors screen in the application

## Сохранение
Данные сохраняются в **item_data.json** и **item_data.gz** (резевный файл хранится в /data/data/item_data.gz.bak)

Сохранение происходит в отдельном *потоке* (TabsManager.SaveThread)

Данные загружаются из файла **.gz** (или резервного, если требуется), если ошибка то из файла **.json**

## Другие файлы
* **color_history.json** - история цветов
* **instanceId** - UUID вашей установки приложения. Используется для анонимной отправки отчётов об ошибке (если телеметрия включена в настройках приложения)
* **version** - Содержит в формате JSON информацию о текущей версии данных. (рекомендуем ознакомится с английским README, там информация исчерпывающая)
* **settings.json** - Настройки приложения

## Import/Export
Структура
```js
--OPENTODAY-IMPORT-START--
<version>
<data>
--OPENTODAY-IMPORT-END--
```

* Version 0: <data> is a regular json converted to base64
* Version 1: <data> is a json converted to base64 but previously passed through GZip compression
* Version 2: <data> is a json converted to base64 but previously passed through GZip compression (added permissions)
* Version 3: <data> is a json converted to base64 but previously passed through GZip compression (added "dataVersion" for fixes in new versions by DataFixer)


## Tree of code (not full) (maybe outdated) (Английский)
```css
com.fazziclay.opentoday
|
| app - app logic
  | App - main application class (used by AndroidManifest.xml)
  |
  | items
  | |
  | | item - (items)
  | | |
  | | | ItemsRegistry - contain all items (Item.class, "Item", EmportExportTool, howToCreateEmpty, howToCopy, R.string.itemDisplayName)
  | | | Item - the father of all aitems (see items tree in README.md)
  | | | TextItem
  | | | CheckboxItem
  | | | DayRepeatableCheckboxItem
  | | | CounterItem
  | | | GroupItem
  | | | FilterGroupItem
  | | | CycleList
  | | | DebugTickCounterItem - item contain (int: counter) and add +1 every tick
  | | | ItemController - controller on item (set when attach to itemsStorage)
  | | | ItemsUtils - utils for item managment
  | |
  | | callback - (callbacks)
  | | |
  | |
  | | notification - (item notifications)
  | | |
  | |
  | | tab - (tabs)
  | | | TabsManager - manager of items
  | |
  | | selection - ...
  | | | SelectionManager
  | | | Selection - selection of item (contain item and item itemsStorage)
  | | 
  | | 
  | | CurrentItemStorage - item storage for one item (CycleListItem...)
  | | ItemsStorage - items storage interface
  | | SimpleItemsStorage - simple implementation of ItemsStorage
  | | ImportWrapper - for import/export
  | 
  | datafixer
  | | DataFixer - it is launched at the very beginning of the app to correct the data of the old version (if the application has been updated)
  |             used 'version' file in '.../Android/data/<...>/files/'
  | SettingsManager - manager of application settings (use in ui...SettingsFragment)
  |             used 'settings.json' file
  | UpdateChecker - checking for app updates
                use api in 'https://fazziclay.github.io/api/project_3/...'
                cached result if update not-available for '...cache/latest_update_check' (file contain unix MILLISeconds)
| gui - ui logic
  | activity
  | |
  | | MainActivity - (see UI tree in README.md)
  |
  | UI - ui utils
  |
| util - there are many different utilities...
| (the rest is for convenience and it doesn't matter)
```

# UI Tree
```css
| MainActivity - mainActivity (current date of top, notifications)
| |
| | MainRootFragment - container of fragments, ItemsTabIncludeFragment by default
| | |
| | | ItemsTabIncludeFragment - (contain Toolbar, Tabs+ViewPager2: ItemsEditorRootFragment)
| | | |
| | | | ItemsEditorRootFragment - Root for ItemsStorage tree
| | | | |
| | | | | ItemsEditorFragment - Contain ItemsStorage drawer
| | | | | | ItemTextEditorFragment - comfortable editor for text & text formatting
| | |
| | | AboutFragment - about this app
| | | | ChangelogFragment - CHANGELOG file viewer
| | | SettingsFragment - settings of app (see app.settings.SettingsManager)
| | | ImportFragment - import from text
| | | DeleteItemsFragment - delete items (calls delete() for all provided items)
```
