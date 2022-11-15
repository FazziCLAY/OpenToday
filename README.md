# OpenToday
Android application for the organization of life, pro notes, reminders

[![license](https://img.shields.io/github/license/fazziclay/opentoday?color=%2300bb00&style=plastic)](https://github.com/FazziCLAY/OpenToday/blob/main/LICENSE)


[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/fazziclay/opentoday?style=plastic) ](https://github.com/FazziCLAY/OpenToday/releases)
[![IzzyOnDroid](https://img.shields.io/endpoint?style=plastic&color=%2300bb00&url=https://apt.izzysoft.de/fdroid/api/v1/shield/com.fazziclay.opentoday)](https://apt.izzysoft.de/fdroid/index/apk/com.fazziclay.opentoday)
[![IzzyOnDroid-OLD](https://img.shields.io/endpoint?style=plastic&color=red&label=Old%20application&url=https://apt.izzysoft.de/fdroid/api/v1/shield/ru.fazziclay.opentoday)](https://apt.izzysoft.de/fdroid/index/apk/ru.fazziclay.opentoday)

# Using
Your main friends are Items
There are different types of items that you can add:
* **Text** - use it for simple text notes
* **CheckBox** - in connection with GroupItem, use it as a grocery list to the store or a to-do list for today
* **Group** - there are no restrictions in depth! Create your own hierarchy of storing items
* **Filter Group** - use it to indicate current birthdays, tasks for today, various schedules (for example, school) or something more, up to the second (very useful thing)

and others...


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
There is a toolbar at the bottom of the application, open the Items tab, click next to the desired one +

## Move items
Swipe the desired item to the right, click the 'selected' checkbox, then open the desired location, open the 'selection' tab in the toolbar and select the desired action there

## Import/Export
Share your items with your friends, to export you have to click on the corresponding button in the 'selection' toolbar menu. All selected items are exported to the clipboard

To import, use the received text in the File tab in the toolbar

# Screenshots
**Even more ideas for use can come to your mind after viewing the screenshots**
|![1](https://user-images.githubusercontent.com/68351787/199270739-5e7491ed-f345-4347-ac8a-a6160090414e.jpg) | ![1](https://user-images.githubusercontent.com/68351787/199270753-53d74768-63e6-4564-a889-e2025ed78d19.jpg) | ![About app](https://user-images.githubusercontent.com/68351787/199270769-080177ea-5368-485a-aa23-3a75e87a0695.jpg) | ![Calendar](https://user-images.githubusercontent.com/68351787/199270761-d21b86d9-9059-4578-ae0a-f3aacb73e1c9.jpg)
|:---:|:---:|:----:|:---:|
|![1](https://user-images.githubusercontent.com/68351787/199270781-a832ca3e-0da1-4480-b1ff-9134c9c41751.jpg) | ![1](https://user-images.githubusercontent.com/68351787/199270788-c29d92ab-b585-440b-90b1-2e2c9bb001b5.jpg)


# Explore app!

# For developers
"/docs" folder

## Todo/Ideas:
* [ ] ItemsStorage for notification quick note
* [ ] Items transform (Text -> Group; Checkbox -> CheckboxDayRepeatable and etc...)
* [ ] Fix notifications in TickSession
* [ ] Toolbar -> Selection -> SelectALL & DeselectALL
* [ ] Settings -> minimize paddings (left, right, bottom, top)
* [ ] Replace checkboxItem to text item & add 'modules' to item and add Module 'checkbox'

Make a pull request -> you will be added to contributors.json and also I will create the contributors screen in the application

## Save
Data saved in **item_data.json** and **item_data.gz**

Saving in other *Thread*

Data loaded from **.gz**, if the error is from **.json**

## Other files
* **color_history.json** - color history for ColorPickerDialogs in ItemEditor
* **instanceId** - ID of your application instance. Used for sending crash reports anonymously

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


## Tree of code (show & not fully)
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
