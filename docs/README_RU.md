# OpenToday


Language: **[[English](https://github.com/FazziCLAY/OpenToday/blob/main/README.md) | Русский]**  
Android-приложение для организации жизни, многофункциональные
заметки и напоминалка

[![license](https://img.shields.io/github/license/fazziclay/opentoday?color=%2300bb00&style=plastic)](https://github.com/FazziCLAY/OpenToday/blob/main/LICENSE)


[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/fazziclay/opentoday?style=plastic)](https://github.com/FazziCLAY/OpenToday/releases)
[![IzzyOnDroid](https://img.shields.io/endpoint?style=plastic&color=%2300bb00&url=https://apt.izzysoft.de/fdroid/api/v1/shield/com.fazziclay.opentoday)](https://apt.izzysoft.de/fdroid/index/apk/com.fazziclay.opentoday)

# Readme is outdated
Используйте английскую версию для получения более актуальной информации

# Использование

Ваши главные друзья это айтемы Они бываю разных типов, и каждый вы
можете создать
* **Text** - можно использовать для простых текстовых заметок
* **CheckBox** - в связи с GroupItem используйте его как список покупок
  в магазине или список дел на сегодня
* **Group** - здесь нет никаких ограничений по глубине! Создайте свою
  собственную иерархию хранения элементов
* **Filter Group** - используйте его для указания текущих дней рождения,
  задач на сегодня, различных расписаний (например, школьных) или
  чего-то еще, вплоть до второго (очень полезная вещь).

и другие...


## Items tree

```css
Item - (minimal heigth, backgroud color)
|
| Text - (text, text color)
  |
  | LongText - (long text, long text color)
  | Group - (items)
  | FilterGroup - (items)
  | CycleList - (items)
  | Counter - (current value, step)
  | Checkbox - (is checked)
     |
     | DayRepeatableCheckbox - (start value for 'is checked' in Checkbox, latest regenerate date)
```

## Add items

В нижней части приложения есть панель инструментов, откройте вкладку
Элементы, нажмите рядом с нужным +

## Перемещение

Проведите пальцем по нужному элементу вправо, установите флажок
"Выбран", затем откройте нужное местоположение, откройте вкладку "Выбор"
на панели инструментов и выберите там нужное действие

## Import/Export

Share your items with your friends, to export you have to click on the
corresponding button in the 'selection' toolbar menu. All selected items
are exported to the clipboard

To import, use the received text in the File tab in the toolbar

# Screenshots

**Еще больше идей для использования может прийти вам в голову после
просмотра скриншотов**
|![1](https://user-images.githubusercontent.com/68351787/199270739-5e7491ed-f345-4347-ac8a-a6160090414e.jpg)
|
![1](https://user-images.githubusercontent.com/68351787/199270753-53d74768-63e6-4564-a889-e2025ed78d19.jpg)
|
![About app](https://user-images.githubusercontent.com/68351787/199270769-080177ea-5368-485a-aa23-3a75e87a0695.jpg)
|
![Calendar](https://user-images.githubusercontent.com/68351787/199270761-d21b86d9-9059-4578-ae0a-f3aacb73e1c9.jpg)
|:---:|:---:|:----:|:---:|
|![1](https://user-images.githubusercontent.com/68351787/199270781-a832ca3e-0da1-4480-b1ff-9134c9c41751.jpg)
|
![1](https://user-images.githubusercontent.com/68351787/199270788-c29d92ab-b585-440b-90b1-2e2c9bb001b5.jpg)


# Исследуйте приложение

# Для разработчиков

"/docs" folder

## Todo/Ideas:

* [ ] ItemsStorage for notification quick note
* [ ] Items transform (Text -> Group; Checkbox -> CheckboxDayRepeatable
      and etc...)
* [ ] Fix notifications in TickSession
* [ ] Toolbar -> Selection -> SelectALL & DeselectALL
* [ ] Settings -> minimize paddings (left, right, bottom, top)
* [ ] Replace checkboxItem to text item & add 'modules' to item and add
      Module 'checkbox'

Make a pull request -> you will be added to contributors.json and also I
will create the contributors screen in the application

## Save

Data saved in **item_data.json** and **item_data.gz**

Saving in other *Thread*

Data loaded from **.gz**, if the error is from **.json**

## Other files

* **color_history.json** - color history for ColorPickerDialogs in
  ItemEditor
* **instanceId** - ID of your application instance. Used for sending
  crash reports anonymously

## Import/Export

Structure

```js
--OPENTODAY-IMPORT-START--
<version>
<data>
--OPENTODAY-IMPORT-END--
```

* Version 0: <data> is a regular json converted to base64
* Version 1: <data> is a json converted to base64 but previously passed
  through GZip compression


## Tree of code (not full)

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
  | |
  | | callback - (callbacks)
  | | |
  | |
  | | notification - (item notifications)
  | | |
  | |
  | | tab - (tabs)
  | | |
  | |
  | | ItemManager - manager of items
  | | ItemsUtils - utils for item managment
  | | CurrentItemStorage - item storage for one item (CycleListItem...)
  | | ItemsStorage - items storage interface
  | | SimpleItemsStorage - simple implementation of ItemsStorage
  | | Selection - selection of item (contain item and item itemsStorage)
  | | ImportWrapper - for import/export
  | 
  | datafixer
  | | DataFixer - it is launched at the very beginning of the app to correct the data of the old version (if the application has been updated)
  |             used 'version' file in '.../Android/data/<...>/files/'
  | settings
  | | SettingsManager - manager of application settings (use in ui...SettingsFragment)
  |             used 'settings.json' file
  | updatechecker
  | | UpdateChecker - checking for app updates
                use api in 'https://fazziclay.github.io/api/project_3/...'
                cached result if update not-available for '...cache/latest_update_check' (file contain unix MILLISeconds)
| ui - ui logic
  | activity
  | |
  | | MainActivity - (see UI tree in README.md)
  |
  | UI - ui utils
  |
  |
| (the rest is for convenience and it doesn't matter)
```

# UI Tree

```css
| MainActivity - mainActivity (current date of top, notfications)
| |
| | MainRootFragment - container of fragments, ItemsTabIncludeFragment by default
| | |
| | | ItemsTabIncludeFragment - (contain Toolbar, Tabs+ViewPager2: ItemsEditorRootFragment)
| | | |
| | | | ItemsEditorRootFragment - Root for ItemsStorage tree
| | | | |
| | | | | ItemsEditorFragment - Contain ItemsStorage drawer
| | |
| | | AboutFragment - about this app
| | | SettingsFragment - settings of app (see app.settings.SettingsManager)
| | | ImportFragment - import from text
| | | DeleteItemsFragment - delete items (calls delete() for all provided items)
```

