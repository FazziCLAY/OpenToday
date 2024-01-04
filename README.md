# Public archive
> **This project is no longer in development.**

.

.

.

.

.

.

.




# OpenToday
Language: **[English | [Русский](https://github.com/FazziCLAY/OpenToday/blob/main/docs/README_RU.md)]**

Android application for the organization of life, pro notes and reminder.

[![license](https://img.shields.io/github/license/fazziclay/opentoday?color=%2300bb00&style=plastic)](https://github.com/FazziCLAY/OpenToday/blob/main/LICENSE)


[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/fazziclay/opentoday?style=plastic)](https://github.com/FazziCLAY/OpenToday/releases)
[![IzzyOnDroid](https://img.shields.io/endpoint?style=plastic&color=%2300bb00&url=https://apt.izzysoft.de/fdroid/api/v1/shield/com.fazziclay.opentoday)](https://apt.izzysoft.de/fdroid/index/apk/com.fazziclay.opentoday)

# Using
Each tile in the app is called an Item.
There are different types of Items, some inherit others, adding new functionality. So for example, the 'Daily checkmark' is inherited from the 'Checkmark' item.
* **Text** - use it for simple text notes
* **CheckBox** - in connection with *Group*, use it as a grocery list to the store or a to-do list for today
* **Group** - there are no restrictions in depth! Create your own hierarchy of storing items
* **Filter Group** - use it to indicate current birthdays, tasks for today, various schedules (for example, school) or something more, up to the second (very useful thing)

**and others...**

# Screenshots
**Even more ideas for use can come to your mind after viewing the screenshots**

| ![1](https://raw.githubusercontent.com/FazziCLAY/OpenToday/main/fastlane/metadata/android/en-US/images/phoneScreenshots/01.jpg) | ![2](https://raw.githubusercontent.com/FazziCLAY/OpenToday/main/fastlane/metadata/android/en-US/images/phoneScreenshots/02.jpg) | ![About app](https://raw.githubusercontent.com/FazziCLAY/OpenToday/main/fastlane/metadata/android/en-US/images/phoneScreenshots/03.jpg) | ![Calendar](https://raw.githubusercontent.com/FazziCLAY/OpenToday/main/fastlane/metadata/android/en-US/images/phoneScreenshots/04.jpg) |
|:-----------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------------------------------:|
| ![1](https://raw.githubusercontent.com/FazziCLAY/OpenToday/main/fastlane/metadata/android/en-US/images/phoneScreenshots/05.jpg) | ![2](https://raw.githubusercontent.com/FazziCLAY/OpenToday/main/fastlane/metadata/android/en-US/images/phoneScreenshots/06.jpg) |                                                                                                                     |                                                                                                                    |

## Toolbar
Toolbar is an important element of the interface. It is located at the bottom of the screen

About important functions in brief:

**Add item** - **Toolbar->Items** click (+) next to the desired item type.

--

**Move items** Swipe the desired item to the right, click the 'selected' checkbox, then open the desired location, open the 'selection' tab in the toolbar and select the desired action there

--

**Import/Export** Share your items with your friends, to export you have to click on the corresponding button in the 'selection' toolbar menu. All selected items are exported to the clipboard

To import, use the received text in the File tab in the toolbar

--


# Technical information
Check more documentation in "/docs" directory

## Contribute
I will be glad if you make a Pull Request with a new feature or bug fix.

See "/docs/CONTRIBUTING.md"

## Kotlin or Java?
I know Java well, and I'm just learning Kotlin.

When developing, I act according to this logic:

If I write a Backend (working with items, etc.) then Java

If I write Frontend (GUI), then Kotlin is preferable

## Items tree (pedigree)
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

## Save
Data saved in **item_data.json**, and **item_data.gz** (bak file is stored in /data/data/item_data.gz.bak)

Saving in other *Thread* (TabsManager.SaveThread)

Data loaded from **.gz**, if the error is from **.json**

## Other files
* **color_history.json** - color history for ColorPickerDialog's in ItemEditor
* **instanceId** - UUID of your application instance. Used for sending crash reports anonymously (if telemetry enabled by user)
* **version** - contains information about the version of the data in this folder in JSON format. The most important value is "data_version". It is used by new versions when updating to run DataFixer
* **settings.json** - Contains the application settings

## Import/Export
Structure
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


## Tree of code (not full) (maybe outdated)
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
